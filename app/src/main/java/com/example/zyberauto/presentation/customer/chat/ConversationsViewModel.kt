package com.example.zyberauto.presentation.customer.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.repository.ChatRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for conversations list.
 */
sealed class ConversationsUiState {
    object Loading : ConversationsUiState()
    data class Success(val conversations: List<ChatConversation>) : ConversationsUiState()
    data class Error(val message: String) : ConversationsUiState()
}

/**
 * ViewModel for customer's conversation list.
 * Loads all conversations for current user.
 */
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId == null) {
            _uiState.value = ConversationsUiState.Error("Not logged in")
            return
        }

        viewModelScope.launch {
            chatRepository.getConversationsForCustomer(currentUserId)
                .catch { e ->
                    _uiState.value = ConversationsUiState.Error(e.message ?: "Failed to load")
                }
                .collect { conversations ->
                    _uiState.value = ConversationsUiState.Success(conversations)
                }
        }
    }
}
