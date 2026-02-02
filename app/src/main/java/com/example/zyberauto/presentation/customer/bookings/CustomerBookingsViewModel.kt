package com.example.zyberauto.presentation.customer.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.domain.repository.AppointmentsRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerBookingsViewModel @Inject constructor(
    private val repository: AppointmentsRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerBookingsUiState>(CustomerBookingsUiState.Loading)
    val uiState: StateFlow<CustomerBookingsUiState> = _uiState.asStateFlow()

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
}

sealed class CustomerBookingsUiState {
    object Loading : CustomerBookingsUiState()
    data class Success(val bookings: List<Appointment>) : CustomerBookingsUiState()
    data class Error(val message: String) : CustomerBookingsUiState()
}
