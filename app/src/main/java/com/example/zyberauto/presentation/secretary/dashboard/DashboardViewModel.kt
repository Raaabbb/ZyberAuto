package com.example.zyberauto.presentation.secretary.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardMetrics(
    val todaysAppointments: Int = 0,
    val totalCustomers: Int = 0,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: List<Float> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appointmentsRepository: AppointmentsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            combine(
                appointmentsRepository.getAppointments(),
                userRepository.getAllCustomers()
            ) { appointments, customers ->
                
                // Calculate Today's Appointments
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                
                val todaysCount = appointments.count {
                    calendar.time = it.dateScheduled
                    calendar.get(Calendar.DAY_OF_YEAR) == today && calendar.get(Calendar.YEAR) == year
                }

                // Calculate Revenue
                val completedAppointments = appointments.filter { it.status == "COMPLETED" }
                val totalRevenue = completedAppointments.sumOf { it.price }

                // Calculate Monthly Revenue (Simplified for current year)
                val monthlyRev = MutableList(12) { 0f }
                completedAppointments.forEach { 
                    calendar.time = it.dateScheduled
                    val month = calendar.get(Calendar.MONTH) // 0-11
                    monthlyRev[month] += it.price.toFloat()
                }

                DashboardMetrics(
                    todaysAppointments = todaysCount,
                    totalCustomers = customers.size,
                    totalRevenue = totalRevenue,
                    monthlyRevenue = monthlyRev
                )
            }
            .catch { e ->
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard")
            }
            .collect { metrics ->
                _uiState.value = DashboardUiState.Success(metrics)
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val metrics: DashboardMetrics) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
