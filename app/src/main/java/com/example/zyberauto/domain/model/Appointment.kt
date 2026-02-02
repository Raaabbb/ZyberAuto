package com.example.zyberauto.domain.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Appointment(
    @DocumentId val id: String = "",
    val userId: String = "", // Link to Auth UID
    val customerName: String = "",
    val vehicleModel: String = "",
    val plateNumber: String = "",
    val serviceType: String = "",
    val notes: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED, COMPLETED
    val dateScheduled: Date = Date(),
    val timeSlot: String = "",
    val price: Double = 0.0,
    val createdAt: Date = Date(),
    val rejectionReason: String = ""
)
