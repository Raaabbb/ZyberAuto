package com.example.zyberauto.domain.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Complaint(
    @DocumentId val id: String = "",
    val userId: String = "", // Link to Auth UID
    val customerName: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "NEW", // NEW, REPLIED, CLOSED
    val dateSubmitted: Date = Date(),
    val reply: String? = null,
    val replyDate: Date? = null
)
