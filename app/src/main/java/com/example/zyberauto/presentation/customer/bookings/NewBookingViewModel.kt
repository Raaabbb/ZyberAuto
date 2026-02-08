package com.example.zyberauto.presentation.customer.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.repository.VehiclesRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import com.example.zyberauto.domain.service.CostEstimateResult
import com.example.zyberauto.domain.service.CostEstimationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NewBookingViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager,
    private val costEstimationService: CostEstimationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewBookingUiState>(NewBookingUiState.Idle)
    val uiState: StateFlow<NewBookingUiState> = _uiState.asStateFlow()

    // Saved vehicles for the customer
    private val _savedVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val savedVehicles: StateFlow<List<Vehicle>> = _savedVehicles.asStateFlow()

    // Currently selected vehicle (null = manual entry)
    private val _selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val selectedVehicle: StateFlow<Vehicle?> = _selectedVehicle.asStateFlow()

    // Track if user is in manual entry mode
    private val _isManualEntry = MutableStateFlow(false)
    val isManualEntry: StateFlow<Boolean> = _isManualEntry.asStateFlow()

    // Flag to show save car dialog after successful booking
    private val _showSaveCarDialog = MutableStateFlow(false)
    val showSaveCarDialog: StateFlow<Boolean> = _showSaveCarDialog.asStateFlow()

    // Time slot availability state
    private val _isTimeSlotFullyBooked = MutableStateFlow(false)
    val isTimeSlotFullyBooked: StateFlow<Boolean> = _isTimeSlotFullyBooked.asStateFlow()

    // Vehicle active booking validation state
    private val _vehicleHasActiveBooking = MutableStateFlow(false)
    val vehicleHasActiveBooking: StateFlow<Boolean> = _vehicleHasActiveBooking.asStateFlow()

    // Cost estimation state
    private val _costEstimate = MutableStateFlow<CostEstimateResult?>(null)
    val costEstimate: StateFlow<CostEstimateResult?> = _costEstimate.asStateFlow()

    private val _isCalculatingCost = MutableStateFlow(false)
    val isCalculatingCost: StateFlow<Boolean> = _isCalculatingCost.asStateFlow()

    // Temp storage for manual entry vehicle details (for save dialog)
    private var pendingVehicleModel: String = ""
    private var pendingVehicleType: String = ""
    private var pendingPlateNumber: String = ""
    private var pendingVehicleYear: String = ""

    init {
        loadSavedVehicles()
    }

    private fun loadSavedVehicles() {
        val userId = sessionManager.getCurrentUserId() ?: return
        viewModelScope.launch {
            vehiclesRepository.getVehiclesForCustomer(userId)
                .catch { /* Ignore errors, just show empty list */ }
                .collect { vehicles ->
                    _savedVehicles.value = vehicles
                    // Default to first vehicle if available, else manual entry
                    if (vehicles.isNotEmpty() && _selectedVehicle.value == null && !_isManualEntry.value) {
                        _selectedVehicle.value = vehicles.first()
                        // Auto-check if the first vehicle has active booking
                        checkVehicleActiveBooking(vehicles.first().plateNumber)
                    } else if (vehicles.isEmpty()) {
                        _isManualEntry.value = true
                    }
                }
        }
    }

    fun selectVehicle(vehicle: Vehicle?) {
        _selectedVehicle.value = vehicle
        _isManualEntry.value = vehicle == null
        // Auto-check if selected vehicle has active booking
        vehicle?.let { checkVehicleActiveBooking(it.plateNumber) }
        if (vehicle == null) {
            _vehicleHasActiveBooking.value = false
        }
    }

    fun setManualEntry(manual: Boolean) {
        _isManualEntry.value = manual
        if (manual) {
            _selectedVehicle.value = null
            _vehicleHasActiveBooking.value = false
        } else if (_savedVehicles.value.isNotEmpty()) {
            _selectedVehicle.value = _savedVehicles.value.first()
            // Auto-check if the first vehicle has active booking
            checkVehicleActiveBooking(_savedVehicles.value.first().plateNumber)
        }
    }

    /**
     * Calculate cost estimate when service type or vehicle type changes.
     * Call this whenever either value changes in the UI.
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
                // On error, still show a basic estimate without inventory check
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
     * Check if a vehicle (by plate number) already has an active booking.
     * Call this when user selects a vehicle or enters a plate number.
     */
    fun checkVehicleActiveBooking(plateNumber: String) {
        if (plateNumber.isBlank()) {
            _vehicleHasActiveBooking.value = false
            return
        }
        viewModelScope.launch {
            try {
                val hasActive = repository.hasActiveBookingForVehicle(plateNumber)
                _vehicleHasActiveBooking.value = hasActive
            } catch (e: Exception) {
                _vehicleHasActiveBooking.value = false
            }
        }
    }

    /**
     * Check if a time slot is fully booked (all 3 mechanics busy).
     * Call this when user selects a date/time to show availability status.
     */
    fun checkTimeSlotAvailability(date: Date, timeSlot: String) {
        viewModelScope.launch {
            try {
                val isFullyBooked = repository.isTimeSlotFullyBooked(date, timeSlot)
                _isTimeSlotFullyBooked.value = isFullyBooked
            } catch (e: Exception) {
                _isTimeSlotFullyBooked.value = false // Assume available on error
            }
        }
    }

    fun submitBooking(
        vehicleModel: String,
        vehicleType: String,
        plateNumber: String,
        vehicleYear: String,
        serviceType: String,
        notes: String,
        scheduledDate: Date
    ) {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = NewBookingUiState.Error("You must be logged in.")
            return
        }

        // Validation
        if (vehicleModel.isBlank() || vehicleType.isBlank() || plateNumber.isBlank() || serviceType.isBlank()) {
            _uiState.value = NewBookingUiState.Error("Please fill in all required fields.")
            return
        }

        // Date validation: must be in the future
        val now = Date()
        if (scheduledDate.before(now)) {
            _uiState.value = NewBookingUiState.Error("Please select a future date and time.")
            return
        }

        // Store pending vehicle details for save dialog
        if (_isManualEntry.value) {
            pendingVehicleModel = vehicleModel
            pendingVehicleType = vehicleType
            pendingPlateNumber = plateNumber
            pendingVehicleYear = vehicleYear
        }

        viewModelScope.launch {
            _uiState.value = NewBookingUiState.Loading
            try {
                // NEW: Check if this vehicle already has an active booking
                val hasActiveBooking = repository.hasActiveBookingForVehicle(plateNumber)
                if (hasActiveBooking) {
                    _uiState.value = NewBookingUiState.Error(
                        "This vehicle already has an active booking. " +
                        "Please wait until your current service is completed before booking again."
                    )
                    return@launch
                }

                // Format time slot from date
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val timeSlot = timeFormat.format(scheduledDate)

                // Calculate service duration based on service type and vehicle type
                val durationMinutes = AppConstants.calculateServiceDuration(serviceType, vehicleType)

                // Calculate end time
                val endCalendar = Calendar.getInstance().apply {
                    time = scheduledDate
                    add(Calendar.MINUTE, durationMinutes)
                }
                val timeSlotEnd = timeFormat.format(endCalendar.time)

                // NEW: Check if the time slot is fully booked
                val isFullyBooked = repository.isTimeSlotFullyBooked(scheduledDate, timeSlot)
                if (isFullyBooked) {
                    _uiState.value = NewBookingUiState.Error(
                        "This time slot is fully booked. All mechanics are busy at this time. " +
                        "Please select a different date or time."
                    )
                    return@launch
                }

                // Fetch actual customer name from user profile
                val userProfile = userRepository.getUserProfile(userId)
                val customerName = userProfile?.name?.takeIf { it.isNotBlank() } ?: "Customer"

                // Get cost estimate (use cached value or calculate fresh)
                val estimate = _costEstimate.value ?: costEstimationService.calculateEstimate(serviceType, vehicleType)

                val newBooking = Appointment(
                    userId = userId,
                    customerName = customerName,
                    vehicleModel = vehicleModel,
                    vehicleType = vehicleType,
                    plateNumber = plateNumber,
                    serviceType = serviceType,
                    notes = notes,
                    status = "PENDING",
                    dateScheduled = scheduledDate,
                    timeSlot = timeSlot,
                    timeSlotEnd = timeSlotEnd,
                    estimatedDurationMinutes = durationMinutes,
                    createdAt = Date(),
                    // Cost estimation fields
                    estimatedLaborCost = estimate.laborCost,
                    estimatedPartsCost = estimate.partsCost,
                    estimatedTotalCost = estimate.totalEstimate,
                    hasInsufficientItems = estimate.hasInsufficientItems,
                    insufficientItemsList = estimate.insufficientItems,
                    partsToDeduct = estimate.partsToDeductAsMaps()
                )

                repository.createBooking(newBooking)

                // If manual entry, show save car dialog; otherwise just success
                if (_isManualEntry.value) {
                    _showSaveCarDialog.value = true
                    _uiState.value = NewBookingUiState.SuccessPendingSave
                } else {
                    _uiState.value = NewBookingUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = NewBookingUiState.Error(e.message ?: "Failed to create booking")
            }
        }
    }

    fun saveVehicleAfterBooking() {
        val userId = sessionManager.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                // Check if plate number already exists before saving
                val plateExists = vehiclesRepository.isPlateNumberRegistered(pendingPlateNumber)
                if (plateExists) {
                    // Vehicle already registered, just complete without saving again
                    _showSaveCarDialog.value = false
                    _uiState.value = NewBookingUiState.Success
                    return@launch
                }

                val vehicle = Vehicle(
                    customerId = userId,
                    model = pendingVehicleModel,
                    vehicleType = pendingVehicleType.ifBlank { "Sedan" },
                    plateNumber = pendingPlateNumber.trim().uppercase(),
                    year = pendingVehicleYear,
                    dateRegistered = System.currentTimeMillis()
                )
                vehiclesRepository.addVehicle(vehicle)
            } catch (e: Exception) {
                // Silently fail, car wasn't saved but booking is done
            }
            _showSaveCarDialog.value = false
            _uiState.value = NewBookingUiState.Success
        }
    }

    fun dismissSaveCarDialog() {
        _showSaveCarDialog.value = false
        _uiState.value = NewBookingUiState.Success
    }

    fun resetState() {
        _uiState.value = NewBookingUiState.Idle
        _showSaveCarDialog.value = false
        _isTimeSlotFullyBooked.value = false
        _vehicleHasActiveBooking.value = false
        _costEstimate.value = null
        _isCalculatingCost.value = false
    }
}

sealed class NewBookingUiState {
    object Idle : NewBookingUiState()
    object Loading : NewBookingUiState()
    object Success : NewBookingUiState()
    object SuccessPendingSave : NewBookingUiState() // Booking done, waiting for save car decision
    data class Error(val message: String) : NewBookingUiState()
}
