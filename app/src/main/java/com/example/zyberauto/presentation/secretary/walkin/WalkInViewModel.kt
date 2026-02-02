package com.example.zyberauto.presentation.secretary.walkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class WalkInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentsRepository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalkInUiState>(WalkInUiState.Idle)
    val uiState: StateFlow<WalkInUiState> = _uiState.asStateFlow()

    fun submitWalkIn(
        fullName: String,
        phoneNumber: String,
        email: String,
        vehicleBrand: String,
        vehicleModel: String,
        plateNumber: String,
        serviceType: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank() || vehicleBrand.isBlank() || vehicleModel.isBlank() || plateNumber.isBlank() || serviceType.isBlank()) {
            _uiState.value = WalkInUiState.Error("Please fill in all required fields.")
            return
        }

        _uiState.value = WalkInUiState.Loading

        viewModelScope.launch {
            try {
                // 1. Create new Customer User
                // We generate a random UID for walk-in customers since they don't have an Auth UID
                val newUserId = UUID.randomUUID().toString()
                
                val newUser = User(
                    uid = newUserId,
                    email = email.ifBlank { "walkin_$newUserId@zyberauto.com" }, // Placeholder email if empty
                    name = fullName,
                    phoneNumber = phoneNumber,
                    role = "CUSTOMER",
                    isVerified = true // Walk-ins verified by staff presence
                )
                
                userRepository.createUserProfile(newUser)

                // 2. Create Appointment
                val newAppointment = Appointment(
                    id = UUID.randomUUID().toString(),
                    userId = newUserId,
                    customerName = fullName,
                    vehicleModel = "$vehicleBrand $vehicleModel",
                    plateNumber = plateNumber,
                    serviceType = serviceType,
                    dateScheduled = Date(), // Walk-in is effectively NOW
                    timeSlot = "WALK-IN",
                    status = "ACCEPTED", // or IN_PROGRESS
                    notes = notes,
                    createdAt = Date()
                )

                appointmentsRepository.createBooking(newAppointment)

                _uiState.value = WalkInUiState.Success
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = WalkInUiState.Error(e.message ?: "Failed to process walk-in")
            }
        }
    }
}

sealed class WalkInUiState {
    object Idle : WalkInUiState()
    object Loading : WalkInUiState()
    object Success : WalkInUiState()
    data class Error(val message: String) : WalkInUiState()
}
