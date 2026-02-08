package com.example.zyberauto.presentation.secretary.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecretaryChatDirectUiState(
    val isLoading: Boolean = false,
    val conversationId: String = "",
    val error: String? = null
)

/**
 * ViewModel for SecretaryChatDirectScreen.
 * Handles finding or creating a conversation with a customer.
 */
@HiltViewModel
class SecretaryChatDirectViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretaryChatDirectUiState())
    val uiState: StateFlow<SecretaryChatDirectUiState> = _uiState.asStateFlow()

    fun findOrCreateConversation(customerId: String, customerName: String) {
        if (customerId.isEmpty() || customerName.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Invalid customer information"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = chatRepository.findOrCreateConversation(customerId, customerName)
                
                if (result.isSuccess) {
                    val conversationId = result.getOrNull() ?: ""
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        conversationId = conversationId,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to create conversation"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}