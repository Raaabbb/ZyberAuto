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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for My Cars screen.
 */
sealed class MyCarsUiState {
    object Loading : MyCarsUiState()
    data class Success(val vehicles: List<Vehicle>) : MyCarsUiState()
    data class Error(val message: String) : MyCarsUiState()
}

/**
 * ViewModel for My Cars screen.
 * Handles loading and displaying customer's registered vehicles.
 */
@HiltViewModel
class MyCarsViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyCarsUiState>(MyCarsUiState.Loading)
    val uiState: StateFlow<MyCarsUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    /**
     * Load vehicles for the current user.
     */
    private fun loadVehicles() {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId == null) {
            _uiState.value = MyCarsUiState.Error("Not logged in")
            return
        }

        viewModelScope.launch {
            vehiclesRepository.getVehiclesForCustomer(currentUserId)
                .catch { e ->
                    _uiState.value = MyCarsUiState.Error(e.message ?: "Failed to load vehicles")
                }
                .collect { vehicles ->
                    _uiState.value = MyCarsUiState.Success(vehicles.sortedByDescending { it.dateRegistered })
                }
        }
    }

    /**
     * Delete a vehicle by ID.
     */
    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            vehiclesRepository.deleteVehicle(vehicleId)
            // The Flow will automatically update the UI
        }
    }
}
