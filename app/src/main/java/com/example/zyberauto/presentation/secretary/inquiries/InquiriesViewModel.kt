package com.example.zyberauto.presentation.secretary.inquiries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.Inquiry
import com.example.zyberauto.domain.repository.InquiriesRepository
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
    private val repository: InquiriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InquiriesUiState>(InquiriesUiState.Loading)
    val uiState: StateFlow<InquiriesUiState> = _uiState.asStateFlow()

    init {
        loadInquiries()
    }

    private fun loadInquiries() {
        viewModelScope.launch {
            _uiState.value = InquiriesUiState.Loading
            repository.getAllInquiries()
                .catch { e ->
                    _uiState.value = InquiriesUiState.Error(e.message ?: "Failed to load inquiries")
                }
                .collectLatest { inquiries ->
                    _uiState.value = InquiriesUiState.Success(inquiries)
                }
        }
    }

    fun submitReply(inquiryId: String, replyText: String) {
        viewModelScope.launch {
            try {
                repository.updateInquiryStatus(inquiryId, "REPLIED", replyText)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed class InquiriesUiState {
    object Loading : InquiriesUiState()
    data class Success(val inquiries: List<Inquiry>) : InquiriesUiState()
    data class Error(val message: String) : InquiriesUiState()
}
