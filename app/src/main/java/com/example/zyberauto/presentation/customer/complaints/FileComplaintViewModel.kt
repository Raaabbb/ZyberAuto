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
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FileComplaintViewModel @Inject constructor(
    private val repository: ComplaintsRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileComplaintUiState>(FileComplaintUiState.Idle)
    val uiState: StateFlow<FileComplaintUiState> = _uiState.asStateFlow()

    fun submitComplaint(subject: String, message: String) {
        val userId = sessionManager.getCurrentUserId()
        if (userId == null) {
            _uiState.value = FileComplaintUiState.Error("You must be logged in.")
            return
        }

        if (subject.isBlank() || message.length < 20) {
            _uiState.value = FileComplaintUiState.Error("Subject required. Message must be at least 20 chars.")
            return
        }

        viewModelScope.launch {
            _uiState.value = FileComplaintUiState.Loading
            try {
                val newComplaint = Complaint(
                    userId = userId,
                    customerName = "Customer (Debug)",
                    subject = subject,
                    message = message,
                    status = "NEW",
                    dateSubmitted = Date()
                )
                repository.createComplaint(newComplaint)
                _uiState.value = FileComplaintUiState.Success
            } catch (e: Exception) {
                _uiState.value = FileComplaintUiState.Error(e.message ?: "Failed to submit complaint")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = FileComplaintUiState.Idle
    }
}

sealed class FileComplaintUiState {
    object Idle : FileComplaintUiState()
    object Loading : FileComplaintUiState()
    object Success : FileComplaintUiState()
    data class Error(val message: String) : FileComplaintUiState()
}
