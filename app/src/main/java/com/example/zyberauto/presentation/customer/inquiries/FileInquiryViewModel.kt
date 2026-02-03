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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileInquiryViewModel @Inject constructor(
    private val repository: InquiriesRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileInquiryUiState>(FileInquiryUiState.Idle)
    val uiState: StateFlow<FileInquiryUiState> = _uiState.asStateFlow()

    fun submitInquiry(subject: String, message: String) {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = FileInquiryUiState.Error("You must be logged in.")
            return
        }

        if (subject.isBlank() || message.length < 20) {
            _uiState.value = FileInquiryUiState.Error("Subject required. Message must be at least 20 chars.")
            return
        }

        viewModelScope.launch {
            _uiState.value = FileInquiryUiState.Loading
            try {
                val newInquiry = Inquiry(
                    userId = userId,
                    customerName = "Customer (Debug)",
                    subject = subject,
                    message = message,
                    status = "NEW",
                    dateSubmitted = System.currentTimeMillis()
                )
                repository.createInquiry(newInquiry)
                _uiState.value = FileInquiryUiState.Success
            } catch (e: Exception) {
                _uiState.value = FileInquiryUiState.Error(e.message ?: "Failed to submit inquiry")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = FileInquiryUiState.Idle
    }
}

sealed class FileInquiryUiState {
    object Idle : FileInquiryUiState()
    object Loading : FileInquiryUiState()
    object Success : FileInquiryUiState()
    data class Error(val message: String) : FileInquiryUiState()
}
