package com.example.zyberauto.domain.model

import java.util.Date

data class Inquiry(
    val id: String = "",
    val userId: String = "", // Link to Auth UID
    val customerName: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "NEW", // NEW, REPLIED, CLOSED
    val dateSubmitted: Long = System.currentTimeMillis(), // Use Long for RTDB timestamp
    val reply: String? = null,
    val replyDate: Long? = null
)
