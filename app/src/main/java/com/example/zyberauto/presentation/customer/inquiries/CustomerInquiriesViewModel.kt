package com.example.zyberauto.presentation.customer.inquiries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Inquiry
import com.example.zyberauto.domain.repository.InquiriesRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerInquiriesViewModel @Inject constructor(
    private val repository: InquiriesRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerInquiriesUiState>(CustomerInquiriesUiState.Loading)
    val uiState: StateFlow<CustomerInquiriesUiState> = _uiState.asStateFlow()

    init {
        loadInquiries()
    }

    private fun loadInquiries() {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = CustomerInquiriesUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            repository.getInquiriesForUser(userId)
                .catch { e ->
                    _uiState.value = CustomerInquiriesUiState.Error(e.message ?: "Unknown error")
                }
                .collect { inquiries ->
                    if (inquiries.isEmpty()) {
                        _uiState.value = CustomerInquiriesUiState.Empty
                    } else {
                        _uiState.value = CustomerInquiriesUiState.Success(inquiries)
                    }
                }
        }
    }
}

sealed class CustomerInquiriesUiState {
    object Loading : CustomerInquiriesUiState()
    object Empty : CustomerInquiriesUiState()
    data class Success(val inquiries: List<Inquiry>) : CustomerInquiriesUiState()
    data class Error(val message: String) : CustomerInquiriesUiState()
}
