package com.example.zyberauto.presentation.secretary.appointments

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScheduleAppointmentViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentsRepository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleAppointmentUiState>(ScheduleAppointmentUiState.Loading)
    val uiState: StateFlow<ScheduleAppointmentUiState> = _uiState.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<User?>(null)
    val selectedCustomer: StateFlow<User?> = _selectedCustomer.asStateFlow()

    // Derived list of vehicles from previous bookings of the selected customer
    private val _customerVehicles = MutableStateFlow<List<String>>(emptyList())
    val customerVehicles: StateFlow<List<String>> = _customerVehicles.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            userRepository.getAllCustomers()
                .catch { e ->
                    // Handle error (maybe simplified for now)
                     _uiState.value = ScheduleAppointmentUiState.Error(e.message ?: "Failed to load customers")
                }
                .collect { customers ->
                    _uiState.value = ScheduleAppointmentUiState.Success(customers)
                }
        }
    }

    fun onCustomerSelected(customerName: String) {
        // Find customer by name - crude but effective for Dropdown "Name (Email)" format or just Name check
        // Ideally we pass objects, but DropdownField gives String.
        // We will match by exact name for now
        val currentCustomers = (_uiState.value as? ScheduleAppointmentUiState.Success)?.customers ?: return
        
        val customer = currentCustomers.find { it.name == customerName }
        _selectedCustomer.value = customer

        if (customer != null) {
            loadVehiclesForCustomer(customer.uid)
        } else {
            _customerVehicles.value = emptyList()
        }
    }

    private fun loadVehiclesForCustomer(userId: String) {
        viewModelScope.launch {
            // Fetch past bookings to find vehicles
            // Assuming appointmentsRepository.getBookingsForUser returns a Flow
            try {
                val bookings = appointmentsRepository.getBookingsForUser(userId).firstOrNull() ?: emptyList()
                val distinctVehicles = bookings.map { "${it.vehicleModel} (${it.plateNumber})" }.distinct()
                _customerVehicles.value = distinctVehicles
            } catch (e: Exception) {
                // Ignore or handle
                 _customerVehicles.value = emptyList()
            }
        }
    }

    fun submitAppointment(
        serviceType: String,
        dateScheduled: Long, // timestamp
        timeSlot: String,
        notes: String,
        vehicleInfo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val customer = _selectedCustomer.value
        if (customer == null) {
            onError("Please select a customer")
            return
        }

        /* Parse vehicle info "Model (Plate)" */
        val vehicleModel: String
        val plateNumber: String
        if (vehicleInfo.contains("(") && vehicleInfo.contains(")")) {
            vehicleModel = vehicleInfo.substringBefore(" (").trim()
            plateNumber = vehicleInfo.substringAfter("(").substringBefore(")").trim()
        } else {
            vehicleModel = vehicleInfo
            plateNumber = "Unknown" 
        }

        val newAppointment = Appointment(
            id = UUID.randomUUID().toString(),
            userId = customer.uid,
            customerName = customer.name,
            vehicleModel = vehicleModel,
            plateNumber = plateNumber,
            serviceType = serviceType,
            dateScheduled = java.util.Date(dateScheduled),
            timeSlot = timeSlot,
            status = "ACCEPTED", // Secretary scheduled -> Accepted immediately usually
            notes = notes, 
            createdAt = java.util.Date()
        )

        viewModelScope.launch {
            try {
                appointmentsRepository.createBooking(newAppointment)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create appointment")
            }
        }
    }
}

sealed class ScheduleAppointmentUiState {
    object Loading : ScheduleAppointmentUiState()
    data class Success(val customers: List<User>) : ScheduleAppointmentUiState()
    data class Error(val message: String) : ScheduleAppointmentUiState()
}
