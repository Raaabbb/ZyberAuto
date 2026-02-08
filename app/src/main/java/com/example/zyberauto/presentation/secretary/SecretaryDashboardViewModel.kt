package com.example.zyberauto.presentation.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class SecretaryDashboardViewModel @Inject constructor(
    private val appointmentsRepository: AppointmentsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Use Asia/Manila timezone for all date calculations
    private val manilaTimeZone = TimeZone.getTimeZone("Asia/Manila")

    private val _appointments = appointmentsRepository.getAppointments()
    private val _customers = userRepository.getAllCustomers()
    
    // Ticker that emits every 5 seconds to refresh time-based calculations
    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(5_000L) // Refresh every 5 seconds
        }
    }

    // Combined UI State with real-time data + auto-refresh
    val uiState: StateFlow<DashboardUiState> = combine(
        _appointments,
        _customers,
        ticker
    ) { appointments, customers, _ ->
        
        val today = Calendar.getInstance(manilaTimeZone)
        val todayYear = today.get(Calendar.YEAR)
        val todayDay = today.get(Calendar.DAY_OF_YEAR)

        // Active Operations: ACCEPTED or IN_PROGRESS
        val activeOperations = appointments.count { 
            it.status == "ACCEPTED" || it.status == "IN_PROGRESS" 
        }

        // Pending Requests
        val pendingRequests = appointments.count { it.status == "PENDING" }

        // === THIS WEEK EFFICIENCY ===
        val thisWeekStart = Calendar.getInstance(manilaTimeZone).apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val thisWeekAppointments = appointments.filter { 
            it.dateScheduled.time >= thisWeekStart.timeInMillis 
        }
        val thisWeekCompleted = thisWeekAppointments.count { it.status == "COMPLETED" }
        val thisWeekTotal = thisWeekAppointments.count { 
            it.status in listOf("COMPLETED", "ACCEPTED", "IN_PROGRESS", "DECLINED") 
        }
        val thisWeekEfficiency = if (thisWeekTotal > 0) {
            thisWeekCompleted.toFloat() / thisWeekTotal.toFloat()
        } else 0f

        // === LAST WEEK EFFICIENCY ===
        val lastWeekEnd = thisWeekStart.clone() as Calendar
        lastWeekEnd.add(Calendar.MILLISECOND, -1)
        val lastWeekStart = lastWeekEnd.clone() as Calendar
        lastWeekStart.add(Calendar.DAY_OF_YEAR, -7)
        
        val lastWeekAppointments = appointments.filter { 
            it.dateScheduled.time >= lastWeekStart.timeInMillis &&
            it.dateScheduled.time <= lastWeekEnd.timeInMillis
        }
        val lastWeekCompleted = lastWeekAppointments.count { it.status == "COMPLETED" }
        val lastWeekTotal = lastWeekAppointments.count { 
            it.status in listOf("COMPLETED", "ACCEPTED", "IN_PROGRESS", "DECLINED") 
        }
        val lastWeekEfficiency = if (lastWeekTotal > 0) {
            lastWeekCompleted.toFloat() / lastWeekTotal.toFloat()
        } else 0f

        // Efficiency change compared to last week
        val efficiencyChange = thisWeekEfficiency - lastWeekEfficiency

        // Chart Data: Last 7 days appointment counts
        val chartData = (0..6).map { daysAgo ->
            val targetDay = Calendar.getInstance(manilaTimeZone).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val targetYear = targetDay.get(Calendar.YEAR)
            val targetDayOfYear = targetDay.get(Calendar.DAY_OF_YEAR)
            
            appointments.count { appt ->
                val apptDate = Calendar.getInstance(manilaTimeZone).apply { time = appt.dateScheduled }
                apptDate.get(Calendar.YEAR) == targetYear && 
                apptDate.get(Calendar.DAY_OF_YEAR) == targetDayOfYear
            }
        }.reversed() // Oldest to newest

        // Today's accepted appointments for timeline
        val acceptedAppointmentsToday = appointments.filter { 
            val apptDate = Calendar.getInstance(manilaTimeZone).apply { time = it.dateScheduled }
            apptDate.get(Calendar.YEAR) == todayYear && 
            apptDate.get(Calendar.DAY_OF_YEAR) == todayDay &&
            (it.status == "ACCEPTED" || it.status == "IN_PROGRESS" || it.status == "COMPLETED")
        }.sortedBy { it.timeSlot }
        
        // Current hour for timeline indicator (Manila time)
        val currentHour = today.get(Calendar.HOUR_OF_DAY)

        DashboardUiState.Success(
            stats = DashboardStats(
                activeOperations = activeOperations,
                pendingRequests = pendingRequests,
                efficiencyRate = thisWeekEfficiency,
                efficiencyChange = efficiencyChange,
                chartData = chartData,
                totalCustomers = customers.size
            ),
            todaySchedule = acceptedAppointmentsToday,
            currentHour = currentHour
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val stats: DashboardStats,
        val todaySchedule: List<Appointment>,
        val currentHour: Int = 0
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class DashboardStats(
    val activeOperations: Int,
    val pendingRequests: Int,
    val efficiencyRate: Float,
    val efficiencyChange: Float, // Positive = improvement over last week
    val chartData: List<Int>,
    val totalCustomers: Int
)
