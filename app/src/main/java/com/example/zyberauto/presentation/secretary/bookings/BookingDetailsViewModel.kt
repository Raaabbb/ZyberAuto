package com.example.zyberauto.presentation.secretary.bookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: String = checkNotNull(savedStateHandle["bookingId"])
    
    private val _uiState = MutableStateFlow<BookingDetailsUiState>(BookingDetailsUiState.Loading)
    val uiState: StateFlow<BookingDetailsUiState> = _uiState.asStateFlow()

    init {
        loadBooking()
    }

    fun loadBooking() {
        viewModelScope.launch {
            // Since repository returns Flow<List>, we need to filter client side or add getById to repo.
            // Detailed phasing didn't explicitly ask for getById in Step 8.2.C ("Load single booking by ID"),
            // but normally we'd add it to repo.
            // For now, to avoid modifying repo interface again if not strictly needed, I'll fetch all and find.
            // Optimally, I should have added getAppointmentById(id) to Repository.
            // Let's check AppointmentRepository interface again. 
            // It has getAppointments() -> Flow<List<Appointment>>.
            // I'll use that and map.
            
            repository.getAppointments().collect { list ->
                val match = list.find { it.id == bookingId }
                if (match != null) {
                    _uiState.value = BookingDetailsUiState.Success(match)
                } else {
                    _uiState.value = BookingDetailsUiState.Error("Booking not found")
                }
            }
        }
    }

    fun acceptBooking(mechanicId: Int) {
        val current = (_uiState.value as? BookingDetailsUiState.Success)?.booking ?: return
        viewModelScope.launch {
            repository.acceptAppointment(current.id, mechanicId)
            // Ideally reload booking or rely on flow update
        }
    }

    fun rejectBooking(reason: String) {
        val current = (_uiState.value as? BookingDetailsUiState.Success)?.booking ?: return
        viewModelScope.launch {
            repository.updateAppointmentStatus(current.id, "REJECTED")
        }
    }
}

sealed class BookingDetailsUiState {
    object Loading : BookingDetailsUiState()
    data class Success(val booking: Appointment) : BookingDetailsUiState()
    data class Error(val message: String) : BookingDetailsUiState()
}
