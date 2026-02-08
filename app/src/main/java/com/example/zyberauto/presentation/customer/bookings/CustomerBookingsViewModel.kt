package com.example.zyberauto.presentation.customer.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CustomerBookingsViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerBookingsUiState>(CustomerBookingsUiState.Loading)
    val uiState: StateFlow<CustomerBookingsUiState> = _uiState.asStateFlow()

    // Reschedule state
    private val _rescheduleState = MutableStateFlow<RescheduleState>(RescheduleState.Idle)
    val rescheduleState: StateFlow<RescheduleState> = _rescheduleState.asStateFlow()

    // Slot availability for reschedule
    private val _isSlotAvailable = MutableStateFlow(true)
    val isSlotAvailable: StateFlow<Boolean> = _isSlotAvailable.asStateFlow()

    init {
        loadBookings()
    }

    private fun loadBookings() {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = CustomerBookingsUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            repository.getBookingsForUser(userId)
                .catch { e ->
                    _uiState.value = CustomerBookingsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { bookings ->
                    _uiState.value = CustomerBookingsUiState.Success(bookings)
                }
        }
    }

    /**
     * Check slot availability for a given date and time.
     */
    fun checkSlotAvailability(date: Date, timeSlot: String) {
        viewModelScope.launch {
            try {
                val isFullyBooked = repository.isTimeSlotFullyBooked(date, timeSlot)
                _isSlotAvailable.value = !isFullyBooked
            } catch (e: Exception) {
                _isSlotAvailable.value = true // Assume available on error
            }
        }
    }

    /**
     * Validate and reschedule an appointment.
     * @return Error message if validation fails, null if successful
     */
    fun rescheduleAppointment(
        appointment: Appointment,
        newDateString: String, // "MM/dd/yyyy" format
        newTimeSlot: String
    ) {
        _rescheduleState.value = RescheduleState.Loading

        viewModelScope.launch {
            try {
                // 1. Parse and validate date
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val newDate: Date
                try {
                    newDate = dateFormat.parse(newDateString) ?: throw IllegalArgumentException("Invalid date format")
                } catch (e: Exception) {
                    _rescheduleState.value = RescheduleState.Error("Invalid date format. Use MM/DD/YYYY")
                    return@launch
                }

                // 2. Check if date is not in the past
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                if (newDate.before(today)) {
                    _rescheduleState.value = RescheduleState.Error("Cannot reschedule to a past date")
                    return@launch
                }

                // 3. Check if time slot is valid (within business hours)
                val validTimeSlots = AppConstants.TimeSlots
                if (newTimeSlot !in validTimeSlots) {
                    _rescheduleState.value = RescheduleState.Error("Invalid time slot. Please select a valid time.")
                    return@launch
                }

                // 4. Combine date with time slot to create full scheduled date
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeDate = timeFormat.parse(newTimeSlot)
                val scheduledCalendar = Calendar.getInstance().apply {
                    time = newDate
                    if (timeDate != null) {
                        val timeCalendar = Calendar.getInstance().apply { time = timeDate }
                        set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    }
                }
                val scheduledDate = scheduledCalendar.time

                // 5. Check if slot is available
                val isFullyBooked = repository.isTimeSlotFullyBooked(scheduledDate, newTimeSlot)
                if (isFullyBooked) {
                    _rescheduleState.value = RescheduleState.Error(
                        "This time slot is fully booked. All mechanics are busy. Please choose a different time."
                    )
                    return@launch
                }

                // 6. Calculate new end time based on service duration
                val durationMinutes = AppConstants.calculateServiceDuration(
                    appointment.serviceType,
                    appointment.vehicleType
                )
                val endCalendar = Calendar.getInstance().apply {
                    time = scheduledDate
                    add(Calendar.MINUTE, durationMinutes)
                }
                val newTimeSlotEnd = timeFormat.format(endCalendar.time)

                // 7. Perform the reschedule
                repository.rescheduleAppointment(
                    appointmentId = appointment.id,
                    newDate = scheduledDate,
                    newTimeSlot = newTimeSlot,
                    newTimeSlotEnd = newTimeSlotEnd,
                    estimatedDurationMinutes = durationMinutes
                )

                _rescheduleState.value = RescheduleState.Success
            } catch (e: Exception) {
                _rescheduleState.value = RescheduleState.Error(e.message ?: "Failed to reschedule")
            }
        }
    }

    fun resetRescheduleState() {
        _rescheduleState.value = RescheduleState.Idle
        _isSlotAvailable.value = true
    }
}

sealed class CustomerBookingsUiState {
    object Loading : CustomerBookingsUiState()
    data class Success(val bookings: List<Appointment>) : CustomerBookingsUiState()
    data class Error(val message: String) : CustomerBookingsUiState()
}

sealed class RescheduleState {
    object Idle : RescheduleState()
    object Loading : RescheduleState()
    object Success : RescheduleState()
    data class Error(val message: String) : RescheduleState()
}
