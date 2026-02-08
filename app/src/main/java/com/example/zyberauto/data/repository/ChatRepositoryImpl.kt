package com.example.zyberauto.data.repository

import com.example.zyberauto.data.local.LocalDataHelper
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val localDataHelper: LocalDataHelper
) : ChatRepository {

    private val conversationsFile = "conversations.json"
    private val messagesFile = "messages.json"

    // ========== CONVERSATIONS ==========

    override fun getConversationsForCustomer(customerId: String): Flow<List<ChatConversation>> = flow {
        val conversations = localDataHelper.readList(conversationsFile, ChatConversation::class.java)
        emit(conversations.filter { it.customerId == customerId }.sortedByDescending { it.lastMessageTime })
    }

    override fun getAllConversations(): Flow<List<ChatConversation>> = flow {
        val conversations = localDataHelper.readList(conversationsFile, ChatConversation::class.java)
        emit(conversations.sortedByDescending { it.lastMessageTime })
    }

    override fun getConversation(conversationId: String): Flow<ChatConversation?> = flow {
        val conversations = localDataHelper.readList(conversationsFile, ChatConversation::class.java)
        emit(conversations.find { it.id == conversationId })
    }

    override suspend fun createConversation(conversation: ChatConversation): Result<String> = try {
        val finalConversation = if (conversation.id.isBlank()) {
            conversation.copy(id = System.currentTimeMillis().toString())
        } else {
            conversation
        }
        localDataHelper.addItem(conversationsFile, finalConversation, ChatConversation::class.java)
        Result.success(finalConversation.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateConversationStatus(conversationId: String, status: String): Result<Unit> = try {
        localDataHelper.updateItem(
            conversationsFile,
            ChatConversation::class.java,
            predicate = { it.id == conversationId },
            update = { it.copy(status = status) }
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ========== MESSAGES ==========

    override fun getMessages(conversationId: String): Flow<List<ChatMessage>> = flow {
        val messages = localDataHelper.readList(messagesFile, ChatMessage::class.java)
        emit(messages.filter { it.conversationId == conversationId }.sortedBy { it.timestamp })
    }

    override suspend fun sendMessage(message: ChatMessage): Result<String> = try {
        val messageId = if (message.id.isBlank()) System.currentTimeMillis().toString() else message.id
        
        // 1. Save Message
        val finalMessage = message.copy(
            id = messageId,
            status = ChatMessage.STATUS_DELIVERED // Local is always delivered immediately
        )
        localDataHelper.addItem(messagesFile, finalMessage, ChatMessage::class.java)
        
        // 2. Update Conversation Last Message
        val lastMessagePreview = if (!message.imageUrl.isNullOrEmpty()) "📷 Photo" else message.message
        localDataHelper.updateItem(
            conversationsFile,
            ChatConversation::class.java,
            predicate = { it.id == message.conversationId },
            update = { 
                it.copy(
                    lastMessage = lastMessagePreview,
                    lastMessageTime = message.timestamp,
                    lastSenderType = message.senderType
                )
            }
        )
        
        Result.success(messageId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateMessageStatus(
        conversationId: String,
        messageId: String,
        status: String
    ): Result<Unit> = try {
        localDataHelper.updateItem(
            messagesFile,
            ChatMessage::class.java,
            predicate = { it.id == messageId && it.conversationId == conversationId },
            update = { it.copy(status = status) }
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markMessagesAsDelivered(
        conversationId: String,
        viewerType: String
    ): Result<Unit> = try {
        val oppositeType = if (viewerType == ChatMessage.SENDER_CUSTOMER) 
            ChatMessage.SENDER_STAFF else ChatMessage.SENDER_CUSTOMER
            
        // In local flow, we just mark them as SEEN usually when opening the chat
        // But for strict compliance with interface:
        val messages = localDataHelper.readList(messagesFile, ChatMessage::class.java).toMutableList()
        var updated = false
        
        messages.replaceAll { msg ->
            if (msg.conversationId == conversationId && msg.senderType == oppositeType && msg.status == ChatMessage.STATUS_SENT) {
                updated = true
                msg.copy(status = ChatMessage.STATUS_DELIVERED)
            } else {
                msg
            }
        }
        
        if (updated) {
            localDataHelper.writeList(messagesFile, messages)
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markMessagesAsSeen(
        conversationId: String,
        viewerType: String
    ): Result<Unit> = try {
        val oppositeType = if (viewerType == ChatMessage.SENDER_CUSTOMER) 
            ChatMessage.SENDER_STAFF else ChatMessage.SENDER_CUSTOMER

        val messages = localDataHelper.readList(messagesFile, ChatMessage::class.java).toMutableList()
        var updated = false
        
        messages.replaceAll { msg ->
            if (msg.conversationId == conversationId && msg.senderType == oppositeType && msg.status != ChatMessage.STATUS_SEEN) {
                updated = true
                msg.copy(status = ChatMessage.STATUS_SEEN)
            } else {
                msg
            }
        }
        
        if (updated) {
            localDataHelper.writeList(messagesFile, messages)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun findOrCreateConversation(customerId: String, customerName: String): Result<String> = try {
        val conversations = localDataHelper.readList(conversationsFile, ChatConversation::class.java)
        val existing = conversations.find { it.customerId == customerId && it.status == ChatConversation.STATUS_ACTIVE }
        
        if (existing != null) {
            Result.success(existing.id)
        } else {
            val newConversation = ChatConversation(
                id = System.currentTimeMillis().toString(),
                customerId = customerId,
                customerName = customerName,
                subject = "Direct Message",
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                lastSenderType = "STAFF",
                status = ChatConversation.STATUS_ACTIVE,
                createdAt = System.currentTimeMillis()
            )
            createConversation(newConversation)
            Result.success(newConversation.id)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
