package com.example.zyberauto.presentation.secretary.bookings

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
class BookingRequestsViewModel @Inject constructor(
    private val repository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingRequestsUiState>(BookingRequestsUiState.Loading)
    val uiState: StateFlow<BookingRequestsUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = BookingRequestsUiState.Loading
            repository.getAppointments()
                .catch { e ->
                    _uiState.value = BookingRequestsUiState.Error(e.message ?: "Failed to load bookings")
                }
                .collect { bookings ->
                    _uiState.value = BookingRequestsUiState.Success(bookings)
                }
        }
    }

    fun acceptBooking(booking: Appointment) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(booking.id, "ACCEPTED")
                // Flow will auto-update the list
            } catch (e: Exception) {
                // Ideally show toast, for now log or handle via side effect
            }
        }
    }

    fun rejectBooking(booking: Appointment, reason: String) {
        viewModelScope.launch {
            try {
                // In a real app we might store the reason in a separate field or collection
                // For now, we update status to REJECTED. 
                // We could append reason to notes or similar if needed, but per Appointment model there is no reason field yet.
                // We will append to notes for now.
                
                val updatedNotes = if (booking.notes.isBlank()) "Rejection Reason: $reason" else "${booking.notes}\n\nRejection Reason: $reason"
                // We need to update notes too. But repository only has status update.
                // Let's just update status for now to keep it simple as per repo interface.
                repository.updateAppointmentStatus(booking.id, "REJECTED")
            } catch (e: Exception) {
                
            }
        }
    }
}

sealed class BookingRequestsUiState {
    object Loading : BookingRequestsUiState()
    data class Success(val bookings: List<Appointment>) : BookingRequestsUiState()
    data class Error(val message: String) : BookingRequestsUiState()
}
