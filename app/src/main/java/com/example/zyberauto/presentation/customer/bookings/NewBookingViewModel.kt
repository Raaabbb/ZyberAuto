package com.example.zyberauto.presentation.customer.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NewBookingViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewBookingUiState>(NewBookingUiState.Idle)
    val uiState: StateFlow<NewBookingUiState> = _uiState.asStateFlow()

    fun submitBooking(
        vehicleModel: String,
        plateNumber: String,
        serviceType: String,
        notes: String,
        timeSlot: String
    ) {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = NewBookingUiState.Error("You must be logged in.")
            return
        }
        
        // Basic Validation
        if (vehicleModel.isBlank() || plateNumber.isBlank() || serviceType.isBlank() || timeSlot.isBlank()) {
            _uiState.value = NewBookingUiState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _uiState.value = NewBookingUiState.Loading
            try {
                val newBooking = Appointment(
                    userId = userId,
                    customerName = "Customer (Debug)", // We don't have email in session manager easily without auth, simplified for debug
                    vehicleModel = vehicleModel,
                    plateNumber = plateNumber,
                    serviceType = serviceType,
                    notes = notes,
                    status = "PENDING",
                    dateScheduled = Date(), // Defaults to now for simplicity in this draft
                    timeSlot = timeSlot,
                    createdAt = Date()
                )
                
                repository.createBooking(newBooking)
                _uiState.value = NewBookingUiState.Success
            } catch (e: Exception) {
                _uiState.value = NewBookingUiState.Error(e.message ?: "Failed to create booking")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = NewBookingUiState.Idle
    }
}

sealed class NewBookingUiState {
    object Idle : NewBookingUiState()
    object Loading : NewBookingUiState()
    object Success : NewBookingUiState()
    data class Error(val message: String) : NewBookingUiState()
}
