package com.example.zyberauto.presentation.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SecretaryDashboardViewModel @Inject constructor(
    private val appointmentsRepository: AppointmentsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _appointments = appointmentsRepository.getAppointments()
    private val _customers = userRepository.getAllCustomers()

    // Combined UI State
    val uiState: StateFlow<DashboardUiState> = combine(
        _appointments,
        _customers
    ) { appointments, customers ->
        
        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayDay = today.get(Calendar.DAY_OF_YEAR)

        // Stats
        val totalCustomers = customers.size
        val pendingRequests = appointments.count { it.status == "PENDING" }
        
        val appointmentsToday = appointments.filter { 
            val apptDate = Calendar.getInstance().apply { time = it.dateScheduled }
            apptDate.get(Calendar.YEAR) == todayYear && 
            apptDate.get(Calendar.DAY_OF_YEAR) == todayDay &&
            (it.status == "ACCEPTED" || it.status == "IN_PROGRESS" || it.status == "COMPLETED")
        }.sortedBy { it.timeSlot } // Simple string sort for now, ideally parse time

        val todayStatsCount = appointmentsToday.size
        
        DashboardUiState.Success(
            stats = DashboardStats(
                appointmentsToday = todayStatsCount,
                pendingRequests = pendingRequests,
                totalCustomers = totalCustomers
            ),
            todaySchedule = appointmentsToday
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
        val todaySchedule: List<Appointment>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

data class DashboardStats(
    val appointmentsToday: Int,
    val pendingRequests: Int,
    val totalCustomers: Int
)
