package com.example.zyberauto.presentation.customer.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.domain.repository.ChatRepository
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for starting a new conversation.
 */
sealed class NewConversationUiState {
    object Idle : NewConversationUiState()
    object Loading : NewConversationUiState()
    data class Success(val conversationId: String) : NewConversationUiState()
    data class Error(val message: String) : NewConversationUiState()
}

/**
 * ViewModel for creating a new conversation.
 */
@HiltViewModel
class NewConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewConversationUiState>(NewConversationUiState.Idle)
    val uiState: StateFlow<NewConversationUiState> = _uiState.asStateFlow()

    /**
     * Create a new conversation with an initial message.
     */
    fun createConversation(subject: String, initialMessage: String) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId == null) {
             _uiState.value = NewConversationUiState.Error("Not logged in")
            return
        }

        if (subject.isBlank()) {
            _uiState.value = NewConversationUiState.Error("Please enter a subject")
            return
        }

        if (initialMessage.isBlank()) {
            _uiState.value = NewConversationUiState.Error("Please enter a message")
            return
        }

        _uiState.value = NewConversationUiState.Loading

        viewModelScope.launch {
            try {
                // Get user's name
                val userData = userRepository.getUserProfile(currentUserId)
                val customerName = userData?.name ?: "Customer"

                // Create conversation
                val conversation = ChatConversation(
                    customerId = currentUserId,
                    customerName = customerName,
                    subject = subject.trim(),
                    lastMessage = initialMessage.trim(),
                    lastMessageTime = System.currentTimeMillis(),
                    lastSenderType = ChatMessage.SENDER_CUSTOMER,
                    status = ChatConversation.STATUS_ACTIVE,
                    createdAt = System.currentTimeMillis()
                )

                val conversationResult = chatRepository.createConversation(conversation)
                
                conversationResult.onSuccess { conversationId ->
                    // Send initial message
                    val message = ChatMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        senderType = ChatMessage.SENDER_CUSTOMER,
                        senderName = customerName,
                        message = initialMessage.trim(),
                        timestamp = System.currentTimeMillis(),
                        status = ChatMessage.STATUS_SENDING
                    )
                    
                    chatRepository.sendMessage(message)
                    _uiState.value = NewConversationUiState.Success(conversationId)
                }.onFailure { e ->
                    _uiState.value = NewConversationUiState.Error(e.message ?: "Failed to create")
                }
            } catch (e: Exception) {
                _uiState.value = NewConversationUiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = NewConversationUiState.Idle
    }
}
