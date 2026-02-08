package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for real-time chat operations.
 */
interface ChatRepository {
    
    // ========== CONVERSATIONS ==========
    
    /**
     * Get all conversations for a specific customer (real-time).
     */
    fun getConversationsForCustomer(customerId: String): Flow<List<ChatConversation>>
    
    /**
     * Get all active conversations for staff view (real-time).
     */
    fun getAllConversations(): Flow<List<ChatConversation>>
    
    /**
     * Get a single conversation by ID (real-time).
     */
    fun getConversation(conversationId: String): Flow<ChatConversation?>
    
    /**
     * Create a new conversation and return its ID.
     */
    suspend fun createConversation(conversation: ChatConversation): Result<String>
    
    /**
     * Update conversation status (ACTIVE/INACTIVE).
     */
    suspend fun updateConversationStatus(conversationId: String, status: String): Result<Unit>
    
    // ========== MESSAGES ==========
    
    /**
     * Get all messages for a conversation (real-time).
     */
    fun getMessages(conversationId: String): Flow<List<ChatMessage>>
    
    /**
     * Send a message. Returns message ID on success.
     * Message starts with SENDING status, updates to SENT on completion.
     */
    suspend fun sendMessage(message: ChatMessage): Result<String>
    
    /**
     * Update message status (SENT, DELIVERED, SEEN).
     */
    suspend fun updateMessageStatus(conversationId: String, messageId: String, status: String): Result<Unit>
    
    /**
     * Mark all messages in a conversation as DELIVERED for a specific viewer.
     */
    suspend fun markMessagesAsDelivered(conversationId: String, viewerType: String): Result<Unit>
    
    /**
     * Mark all messages in a conversation as SEEN for a specific viewer.
     */
    suspend fun markMessagesAsSeen(conversationId: String, viewerType: String): Result<Unit>
    
    /**
     * Find an existing active conversation with a customer or create a new one.
     * Returns the conversation ID.
     */
    suspend fun findOrCreateConversation(customerId: String, customerName: String): Result<String>
}
