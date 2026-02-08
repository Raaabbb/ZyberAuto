package com.example.zyberauto.presentation.secretary.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.model.PartDeduction
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppointmentsUiState>(AppointmentsUiState.Loading)
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    // Available mechanics for current appointment being processed
    private val _availableMechanics = MutableStateFlow<List<Int>>(listOf(1, 2, 3))
    val availableMechanics: StateFlow<List<Int>> = _availableMechanics.asStateFlow()

    // Random mechanic suggestion
    private val _randomMechanicSuggestion = MutableStateFlow<Int?>(null)
    val randomMechanicSuggestion: StateFlow<Int?> = _randomMechanicSuggestion.asStateFlow()

    init {
        loadAppointments()
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            repository.getAppointments()
                .catch { e ->
                    _uiState.value = AppointmentsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { appointments ->
                    _uiState.value = AppointmentsUiState.Success(appointments)
                }
        }
    }

    /**
     * Load available mechanics for a specific appointment's date/time slot.
     * Should be called before showing the accept dialog.
     */
    fun loadAvailableMechanicsForAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                val available = repository.getAvailableMechanicsForSlot(
                    appointment.dateScheduled, 
                    appointment.timeSlot
                )
                _availableMechanics.value = available.ifEmpty { listOf(1, 2, 3) }
                
                // Clear previous random suggestion
                _randomMechanicSuggestion.value = null
            } catch (e: Exception) {
                // Default to all mechanics on error
                _availableMechanics.value = listOf(1, 2, 3)
            }
        }
    }

    /**
     * Randomly select a mechanic from available ones (no bias).
     * Returns the selected mechanic number.
     */
    fun selectRandomMechanic(): Int {
        val available = _availableMechanics.value
        if (available.isEmpty()) return 1
        
        val selected = available[Random.nextInt(available.size)]
        _randomMechanicSuggestion.value = selected
        return selected
    }

    fun updateStatus(appointmentId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                // If marking as COMPLETED, also deduct inventory
                if (newStatus == "COMPLETED") {
                    completeAppointmentWithInventoryDeduction(appointmentId)
                } else {
                    repository.updateAppointmentStatus(appointmentId, newStatus)
                }
            } catch (e: Exception) {
                // Ideally show a transient error event (snackbar)
            }
        }
    }

    /**
     * Complete an appointment and deduct the required parts from inventory.
     */
    private suspend fun completeAppointmentWithInventoryDeduction(appointmentId: String) {
        // Get current appointments to find the one being completed
        val currentState = _uiState.value
        if (currentState !is AppointmentsUiState.Success) {
            repository.updateAppointmentStatus(appointmentId, "COMPLETED")
            return
        }
        
        val appointment = currentState.appointments.find { it.id == appointmentId }
        if (appointment == null) {
            repository.updateAppointmentStatus(appointmentId, "COMPLETED")
            return
        }
        
        // Deduct inventory if there are parts to deduct
        val partsToDeduct = appointment.getPartDeductions()
        if (partsToDeduct.isNotEmpty()) {
            val result = inventoryRepository.batchDeductStock(partsToDeduct)
            result.onFailure { e ->
                // Log the error but don't block completion
                // Inventory might need manual adjustment
                android.util.Log.e("AppointmentsVM", "Inventory deduction failed: ${e.message}")
            }
        }
        
        // Update status to COMPLETED
        repository.updateAppointmentStatus(appointmentId, "COMPLETED")
    }

    fun acceptAppointment(appointmentId: String, mechanicNumber: Int) {
        viewModelScope.launch {
            try {
                repository.acceptAppointment(appointmentId, mechanicNumber)
            } catch (e: Exception) {
                // Ideally show a transient error event (snackbar)
            }
        }
    }

    fun rejectBooking(appointmentId: String, reason: String) {
        viewModelScope.launch {
            try {
                repository.rejectBooking(appointmentId, reason)
            } catch (e: Exception) {
                // Ideally show a transient error event (snackbar)
            }
        }
    }
}

sealed class AppointmentsUiState {
    object Loading : AppointmentsUiState()
    data class Success(val appointments: List<Appointment>) : AppointmentsUiState()
    data class Error(val message: String) : AppointmentsUiState()
}
