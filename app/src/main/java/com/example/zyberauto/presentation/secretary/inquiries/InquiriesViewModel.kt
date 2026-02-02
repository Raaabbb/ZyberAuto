package com.example.zyberauto.presentation.secretary.inquiries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.domain.repository.ComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InquiriesViewModel @Inject constructor(
    private val repository: ComplaintsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InquiriesUiState>(InquiriesUiState.Loading)
    val uiState: StateFlow<InquiriesUiState> = _uiState.asStateFlow()

    init {
        loadComplaints()
    }

    private fun loadComplaints() {
        viewModelScope.launch {
            _uiState.value = InquiriesUiState.Loading
            repository.getAllComplaints()
                .catch { e ->
                    _uiState.value = InquiriesUiState.Error(e.message ?: "Failed to load inquiries")
                }
                .collectLatest { complaints ->
                    _uiState.value = InquiriesUiState.Success(complaints)
                }
        }
    }

    fun submitReply(complaintId: String, replyText: String) {
        viewModelScope.launch {
            try {
                repository.updateComplaintStatus(complaintId, "REPLIED", replyText)
                // The flow will automatically update the UI
            } catch (e: Exception) {
                // Handle error (optionally expose another state stream for one-off events)
            }
        }
    }
}

sealed class InquiriesUiState {
    object Loading : InquiriesUiState()
    data class Success(val complaints: List<Complaint>) : InquiriesUiState()
    data class Error(val message: String) : InquiriesUiState()
}
