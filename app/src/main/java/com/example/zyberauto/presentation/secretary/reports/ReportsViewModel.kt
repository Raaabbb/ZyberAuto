package com.example.zyberauto.presentation.secretary.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadRevenueData()
    }

    private fun loadRevenueData() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading
            repository.getAppointments()
                .catch { e ->
                    _uiState.value = ReportsUiState.Error(e.message ?: "Failed to load report data")
                }
                .collectLatest { appointments ->
                    val completedAppointments = appointments.filter { it.status == "COMPLETED" }
                    val totalRevenue = completedAppointments.sumOf { it.price }
                    
                    _uiState.value = ReportsUiState.Success(
                        totalRevenue = totalRevenue,
                        totalTransactions = completedAppointments.size,
                        transactions = completedAppointments.sortedByDescending { it.dateScheduled }
                    )
                }
        }
    }
}

sealed class ReportsUiState {
    object Loading : ReportsUiState()
    data class Success(
        val totalRevenue: Double,
        val totalTransactions: Int,
        val transactions: List<Appointment>
    ) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}
