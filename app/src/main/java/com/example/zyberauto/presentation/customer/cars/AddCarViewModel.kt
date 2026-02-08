package com.example.zyberauto.presentation.customer.cars

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.domain.repository.VehiclesRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Add Car screen.
 */
sealed class AddCarUiState {
    object Idle : AddCarUiState()
    object Loading : AddCarUiState()
    object Success : AddCarUiState()
    data class Error(val message: String) : AddCarUiState()
}

/**
 * ViewModel for Add Car screen.
 * Handles adding new vehicles to the customer's account.
 */
@HiltViewModel
class AddCarViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCarUiState>(AddCarUiState.Idle)
    val uiState: StateFlow<AddCarUiState> = _uiState.asStateFlow()

    /**
     * Add a new vehicle for the current user.
     */
    fun addVehicle(model: String, vehicleType: String, plateNumber: String, year: String) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId == null) {
            _uiState.value = AddCarUiState.Error("Not logged in")
            return
        }

        // Validation
        if (model.isBlank()) {
            _uiState.value = AddCarUiState.Error("Please enter vehicle model")
            return
        }
        if (vehicleType.isBlank()) {
            _uiState.value = AddCarUiState.Error("Please select vehicle type")
            return
        }
        if (plateNumber.isBlank()) {
            _uiState.value = AddCarUiState.Error("Please enter plate number")
            return
        }

        _uiState.value = AddCarUiState.Loading

        viewModelScope.launch {
            // Check if plate number already exists in database
            val plateExists = vehiclesRepository.isPlateNumberRegistered(plateNumber)
            if (plateExists) {
                _uiState.value = AddCarUiState.Error("This plate number is already registered. Please check and try again.")
                return@launch
            }

            val vehicle = Vehicle(
                customerId = currentUserId,
                model = model.trim(),
                vehicleType = vehicleType,
                plateNumber = plateNumber.trim().uppercase(),
                year = year.trim(),
                dateRegistered = System.currentTimeMillis()
            )

            vehiclesRepository.addVehicle(vehicle)
                .onSuccess {
                    _uiState.value = AddCarUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = AddCarUiState.Error(e.message ?: "Failed to add vehicle")
                }
        }
    }

    /**
     * Reset state for next use.
     */
    fun resetState() {
        _uiState.value = AddCarUiState.Idle
    }
}
