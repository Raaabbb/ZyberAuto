package com.example.zyberauto.domain.model

/**
 * ChatMessage represents a single message in a conversation.
 * 
 * @param id Unique identifier (Firebase key)
 * @param conversationId ID of the parent conversation
 * @param senderId Firebase Auth UID of the sender
 * @param senderType Who sent: "CUSTOMER" or "STAFF"
 * @param senderName Display name of the sender
 * @param message The message content
 * @param timestamp When the message was sent
 * @param status Message delivery status: SENDING, SENT, DELIVERED, SEEN
 */
data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderType: String = "CUSTOMER",
    val senderName: String = "",
    val message: String = "",
    val imageUrl: String? = null,  // Optional image URL from Firebase Storage
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = STATUS_SENDING
) {
    companion object {
        const val SENDER_CUSTOMER = "CUSTOMER"
        const val SENDER_STAFF = "STAFF"
        
        // Message delivery status
        const val STATUS_SENDING = "SENDING"    // Uploading to Firebase
        const val STATUS_SENT = "SENT"          // Confirmed in database
        const val STATUS_DELIVERED = "DELIVERED" // Recipient's device synced
        const val STATUS_SEEN = "SEEN"          // Recipient opened chat
    }
    
    /**
     * Check if this message was sent by customer.
     */
    fun isFromCustomer(): Boolean = senderType == SENDER_CUSTOMER
    
    /**
     * Check if this message was sent by staff.
     */
    fun isFromStaff(): Boolean = senderType == SENDER_STAFF
}
