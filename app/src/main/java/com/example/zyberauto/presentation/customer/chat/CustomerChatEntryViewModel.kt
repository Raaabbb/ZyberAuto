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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerChatEntryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

    init {
        resolveChat()
    }

    private fun resolveChat() {
        val currentUserId = sessionManager.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                // Get most recent conversation
                val conversations = chatRepository.getConversationsForCustomer(currentUserId).first()
                
                // Prioritize finding an ACTIVE conversation
                val activeConversation = conversations.find { it.status == ChatConversation.STATUS_ACTIVE }
                
                if (activeConversation != null) {
                    _navigationEvent.value = activeConversation.id
                } else {
                    // No active conversation found (even if archived ones exist, we want a new active one)
                    // This matches Secretary logic which creates new if no active found
                    createNewConversation(currentUserId)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun createNewConversation(userId: String) {
        val userData = userRepository.getUserProfile(userId)
        val customerName = userData?.name ?: "Customer"

        val subject = "General Inquiry"

        val conversation = ChatConversation(
            customerId = userId,
            customerName = customerName,
            subject = subject,
            lastMessage = "", // No initial message - user will type their own
            lastMessageTime = System.currentTimeMillis(),
            lastSenderType = ChatMessage.SENDER_CUSTOMER,
            status = ChatConversation.STATUS_ACTIVE,
            createdAt = System.currentTimeMillis()
        )

        chatRepository.createConversation(conversation)
            .onSuccess { conversationId ->
                // Navigate directly without sending an initial message
                // User will type their own first message
                _navigationEvent.value = conversationId
            }
            .onFailure {
                // Handle failure (retry or show error)
            }
    }
}
