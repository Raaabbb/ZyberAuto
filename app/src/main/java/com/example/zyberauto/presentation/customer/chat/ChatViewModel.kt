package com.example.zyberauto.presentation.customer.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.data.repository.ImageUploadRepository
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.domain.repository.ChatRepository
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for chat screen.
 */
data class ChatUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSending: Boolean = false,
    val isUploadingImage: Boolean = false,
    val currentUserId: String = "" // Added for UI binding
)

/**
 * ViewModel for real-time chat screen.
 * Handles loading messages, sending new messages, and marking as seen.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val imageUploadRepository: ImageUploadRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Current user info (mirrored in uiState)
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var senderType: String = ChatMessage.SENDER_CUSTOMER

    init {
        if (conversationId.isNotEmpty()) {
            loadCurrentUser()
        }
    }

    private fun loadCurrentUser() {
        val uid = sessionManager.getCurrentUserId()
        if (uid != null) {
            currentUserId = uid
            _uiState.value = _uiState.value.copy(currentUserId = uid)
            
            // Determine sender type based on user role
            viewModelScope.launch {
                try {
                    val userData = userRepository.getUserProfile(uid)
                    senderType = if (userData?.role == "SECRETARY" || userData?.role == "ADMIN") {
                        ChatMessage.SENDER_STAFF
                    } else {
                        ChatMessage.SENDER_CUSTOMER
                    }
                    currentUserName = userData?.name ?: "User"
                } catch (e: Exception) {
                    currentUserName = "User"
                } finally {
                    // Load data after determining role
                    loadConversation()
                    loadMessages()
                }
            }
        }
    }

    private fun loadConversation() {
        viewModelScope.launch {
            chatRepository.getConversation(conversationId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { conversation ->
                    _uiState.value = _uiState.value.copy(conversation = conversation)
                }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(conversationId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                    // Mark messages as seen when loaded
                    markMessagesAsSeen()
                }
        }
    }

    /**
     * Send a text message.
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank() || conversationId.isEmpty()) return
        
        _uiState.value = _uiState.value.copy(isSending = true)

        val message = ChatMessage(
            conversationId = conversationId,
            senderId = currentUserId,
            senderType = senderType,
            senderName = currentUserName,
            message = messageText.trim(),
            timestamp = System.currentTimeMillis(),
            status = ChatMessage.STATUS_SENDING
        )

        viewModelScope.launch {
            chatRepository.sendMessage(message)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSending = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = e.message
                    )
                }
        }
    }

    fun sendImageMessage(imageUri: Uri) {
         // Feature DISABLED per user request
    }

    private fun markMessagesAsSeen() {
        viewModelScope.launch {
            chatRepository.markMessagesAsSeen(conversationId, senderType)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
