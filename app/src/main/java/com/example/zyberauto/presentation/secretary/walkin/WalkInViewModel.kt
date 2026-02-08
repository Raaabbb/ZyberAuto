package com.example.zyberauto.presentation.secretary.walkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.repository.VehiclesRepository
import com.example.zyberauto.domain.service.CostEstimateResult
import com.example.zyberauto.domain.service.CostEstimationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Date
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WalkInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentsRepository: AppointmentsRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val costEstimationService: CostEstimationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalkInUiState>(WalkInUiState.Idle)
    val uiState: StateFlow<WalkInUiState> = _uiState.asStateFlow()

    // Available mechanics for current time (walk-in is NOW)
    private val _availableMechanics = MutableStateFlow<List<Int>>(listOf(1, 2, 3))
    val availableMechanics: StateFlow<List<Int>> = _availableMechanics.asStateFlow()

    // Random mechanic suggestion
    private val _randomMechanicSuggestion = MutableStateFlow<Int?>(null)
    val randomMechanicSuggestion: StateFlow<Int?> = _randomMechanicSuggestion.asStateFlow()

    // Existing customers list
    private val _existingCustomers = MutableStateFlow<List<User>>(emptyList())
    val existingCustomers: StateFlow<List<User>> = _existingCustomers.asStateFlow()

    // Selected existing customer
    private val _selectedCustomer = MutableStateFlow<User?>(null)
    val selectedCustomer: StateFlow<User?> = _selectedCustomer.asStateFlow()

    // Vehicles for selected customer
    private val _customerVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val customerVehicles: StateFlow<List<Vehicle>> = _customerVehicles.asStateFlow()

    // Cost estimation state
    private val _costEstimate = MutableStateFlow<CostEstimateResult?>(null)
    val costEstimate: StateFlow<CostEstimateResult?> = _costEstimate.asStateFlow()

    private val _isCalculatingCost = MutableStateFlow(false)
    val isCalculatingCost: StateFlow<Boolean> = _isCalculatingCost.asStateFlow()

    init {
        loadAvailableMechanicsForNow()
        loadExistingCustomers()
    }

    /**
     * Load all existing customers for selection.
     */
    private fun loadExistingCustomers() {
        viewModelScope.launch {
            userRepository.getAllCustomers().collect { customers ->
                _existingCustomers.value = customers
            }
        }
    }

    /**
     * Select an existing customer and load their vehicles.
     */
    fun selectExistingCustomer(customer: User?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            loadVehiclesForCustomer(customer.uid)
        } else {
            _customerVehicles.value = emptyList()
        }
    }

    /**
     * Load vehicles for a specific customer.
     */
    private fun loadVehiclesForCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                val vehicles = vehiclesRepository.getVehiclesForCustomer(customerId).first()
                _customerVehicles.value = vehicles
            } catch (e: Exception) {
                _customerVehicles.value = emptyList()
            }
        }
    }

    /**
     * Calculate cost estimate when service type or vehicle type changes.
     */
    fun calculateCostEstimate(serviceType: String, vehicleType: String) {
        if (serviceType.isBlank() || vehicleType.isBlank()) {
            _costEstimate.value = null
            return
        }
        
        viewModelScope.launch {
            _isCalculatingCost.value = true
            try {
                val estimate = costEstimationService.calculateEstimate(serviceType, vehicleType)
                _costEstimate.value = estimate
            } catch (e: Exception) {
                val laborCost = AppConstants.calculateLaborCost(serviceType, vehicleType)
                _costEstimate.value = CostEstimateResult(
                    laborCost = laborCost,
                    partsCost = 0.0,
                    totalEstimate = laborCost,
                    partsToDeduct = emptyList(),
                    hasInsufficientItems = false,
                    insufficientItems = listOf("Unable to check inventory")
                )
            } finally {
                _isCalculatingCost.value = false
            }
        }
    }

    /**
     * Load available mechanics for right now (walk-in time).
     */
    private fun loadAvailableMechanicsForNow() {
        viewModelScope.launch {
            try {
                val now = Date()
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val currentTimeSlot = timeFormat.format(now)
                
                val available = appointmentsRepository.getAvailableMechanicsForSlot(now, "WALK-IN")
                _availableMechanics.value = available.ifEmpty { listOf(1, 2, 3) }
            } catch (e: Exception) {
                _availableMechanics.value = listOf(1, 2, 3)
            }
        }
    }

    /**
     * Randomly select a mechanic from available ones (no bias).
     */
    fun selectRandomMechanic(): Int {
        val available = _availableMechanics.value
        if (available.isEmpty()) return 1
        
        val selected = available[Random.nextInt(available.size)]
        _randomMechanicSuggestion.value = selected
        return selected
    }

    fun submitWalkIn(
        fullName: String,
        phoneNumber: String,
        email: String,
        vehicleBrand: String,
        vehicleModel: String,
        plateNumber: String,
        vehicleType: String,
        serviceType: String,
        notes: String,
        assignedMechanic: Int,
        existingCustomerId: String?, // null = new customer, non-null = use existing
        onSuccess: () -> Unit
    ) {
        // For existing vehicles, brand may be empty (model contains full name)
        // For new entries, either brand+model or just model should be filled
        val hasValidVehicle = vehicleModel.isNotBlank() && plateNumber.isNotBlank()
        if (fullName.isBlank() || !hasValidVehicle || serviceType.isBlank()) {
            _uiState.value = WalkInUiState.Error("Please fill in all required fields.")
            return
        }

        _uiState.value = WalkInUiState.Loading

        viewModelScope.launch {
            try {
                // Check if this vehicle already has an active booking
                val hasActiveBooking = appointmentsRepository.hasActiveBookingForVehicle(plateNumber)
                if (hasActiveBooking) {
                    _uiState.value = WalkInUiState.Error(
                        "This vehicle already has an active booking. " +
                        "Please complete or cancel the existing service first."
                    )
                    return@launch
                }

                // Determine user ID - use existing or create new
                val userId: String
                if (existingCustomerId != null) {
                    // Use existing customer
                    userId = existingCustomerId
                } else {
                    // Create new Customer User
                    val newUserId = UUID.randomUUID().toString()
                    
                    val newUser = User(
                        uid = newUserId,
                        email = email.ifBlank { "walkin_$newUserId@zyberauto.com" },
                        name = fullName,
                        phoneNumber = phoneNumber,
                        role = "CUSTOMER",
                        isVerified = true
                    )
                    
                    userRepository.createUserProfile(newUser)
                    userId = newUserId
                }

                // 2. Calculate service duration based on service type and vehicle type
                val durationMinutes = AppConstants.calculateServiceDuration(serviceType, vehicleType)
                
                // For walk-ins, calculate end time from now
                val now = Date()
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val currentTimeSlot = timeFormat.format(now)
                
                val calendar = Calendar.getInstance()
                calendar.time = now
                calendar.add(Calendar.MINUTE, durationMinutes)
                val endTime = calendar.time
                val timeSlotEnd = timeFormat.format(endTime)

                // 3. Get cost estimate (use cached value or calculate fresh)
                val estimate = _costEstimate.value ?: costEstimationService.calculateEstimate(serviceType, vehicleType)

                // 4. Create Appointment with assigned mechanic, duration, and cost estimate
                // Combine brand and model, handling empty brand for existing vehicles
                val fullVehicleModel = if (vehicleBrand.isBlank()) vehicleModel else "$vehicleBrand $vehicleModel"
                
                val newAppointment = Appointment(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    customerName = fullName,
                    vehicleModel = fullVehicleModel,
                    vehicleType = vehicleType,
                    plateNumber = plateNumber,
                    serviceType = serviceType,
                    dateScheduled = now, // Walk-in is effectively NOW
                    timeSlot = currentTimeSlot,
                    timeSlotEnd = timeSlotEnd,
                    estimatedDurationMinutes = durationMinutes,
                    status = "ACCEPTED", // Walk-ins are immediately accepted
                    notes = notes,
                    createdAt = Date(),
                    assignedMechanic = assignedMechanic, // Assign the selected mechanic
                    // Cost estimation fields
                    estimatedLaborCost = estimate.laborCost,
                    estimatedPartsCost = estimate.partsCost,
                    estimatedTotalCost = estimate.totalEstimate,
                    hasInsufficientItems = estimate.hasInsufficientItems,
                    insufficientItemsList = estimate.insufficientItems,
                    partsToDeduct = estimate.partsToDeductAsMaps()
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
