package com.example.zyberauto.presentation.secretary.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val repository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppointmentsUiState>(AppointmentsUiState.Loading)
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

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

    fun updateStatus(appointmentId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(appointmentId, newStatus)
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
