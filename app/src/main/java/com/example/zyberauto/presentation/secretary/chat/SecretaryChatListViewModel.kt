package com.example.zyberauto.presentation.secretary.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for secretary's chat list.
 */
sealed class SecretaryChatListUiState {
    object Loading : SecretaryChatListUiState()
    data class Success(val conversations: List<ChatConversation>) : SecretaryChatListUiState()
    data class Error(val message: String) : SecretaryChatListUiState()
}

/**
 * ViewModel for secretary's conversation list.
 * Shows ALL conversations from all customers.
 */
@HiltViewModel
class SecretaryChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SecretaryChatListUiState>(SecretaryChatListUiState.Loading)
    val uiState: StateFlow<SecretaryChatListUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            chatRepository.getAllConversations()
                .catch { e ->
                    _uiState.value = SecretaryChatListUiState.Error(e.message ?: "Failed to load")
                }
                .collect { conversations ->
                    _uiState.value = SecretaryChatListUiState.Success(conversations)
                }
        }
    }

    /**
     * Update conversation status (Active/Inactive).
     */
    fun updateConversationStatus(conversationId: String, status: String) {
        viewModelScope.launch {
            chatRepository.updateConversationStatus(conversationId, status)
        }
    }
}
