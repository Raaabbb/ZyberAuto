package com.example.zyberauto.domain.model

/**
 * ChatConversation represents a chat thread between a customer and staff.
 * 
 * @param id Unique identifier (Firebase key)
 * @param customerId Customer's Firebase Auth UID
 * @param customerName Customer's display name
 * @param subject Topic of the conversation
 * @param lastMessage Preview of the most recent message
 * @param lastMessageTime Timestamp of the last message
 * @param lastSenderType Who sent the last message ("CUSTOMER" or "STAFF")
 * @param status Conversation status: "ACTIVE" or "INACTIVE"
 * @param createdAt Timestamp when conversation was created
 */
data class ChatConversation(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val subject: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastSenderType: String = "CUSTOMER",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
    }
}
