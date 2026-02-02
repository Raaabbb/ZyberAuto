package com.example.zyberauto.presentation.customer.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.domain.repository.ComplaintsRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerComplaintsViewModel @Inject constructor(
    private val repository: ComplaintsRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerComplaintsUiState>(CustomerComplaintsUiState.Loading)
    val uiState: StateFlow<CustomerComplaintsUiState> = _uiState.asStateFlow()

    init {
        loadComplaints()
    }

    private fun loadComplaints() {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = CustomerComplaintsUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            repository.getComplaintsForUser(userId)
                .catch { e ->
                    _uiState.value = CustomerComplaintsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { complaints ->
                    if (complaints.isEmpty()) {
                        _uiState.value = CustomerComplaintsUiState.Empty
                    } else {
                        _uiState.value = CustomerComplaintsUiState.Success(complaints)
                    }
                }
        }
    }
}

sealed class CustomerComplaintsUiState {
    object Loading : CustomerComplaintsUiState()
    object Empty : CustomerComplaintsUiState()
    data class Success(val complaints: List<Complaint>) : CustomerComplaintsUiState()
    data class Error(val message: String) : CustomerComplaintsUiState()
}
