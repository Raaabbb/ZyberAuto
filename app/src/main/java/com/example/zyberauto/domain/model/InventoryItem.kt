package com.example.zyberauto.domain.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class InventoryItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "", // e.g., "Parts", "Fluids", "Tools"
    val quantity: Int = 0,
    val price: Double = 0.0,
    val lastUpdated: Date = Date()
)
