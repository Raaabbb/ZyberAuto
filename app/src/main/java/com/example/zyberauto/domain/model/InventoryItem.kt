package com.example.zyberauto.domain.model

import java.util.Date

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val category: String = "", // e.g., "Parts", "Fluids", "Tools"
    val quantity: Int = 0,
    val price: Double = 0.0,
    val unit: String = "piece", // "piece", "liter", "set", "pair"
    val reorderLevel: Int = 5, // Alert when stock falls below this
    val lastUpdated: Date = Date()
)

/**
 * Represents a part/item to be deducted from inventory when appointment is completed.
 * Stored as part of the Appointment to track which items are needed.
 */
data class PartDeduction(
    val itemId: String = "",
    val itemName: String = "",
    val quantityNeeded: Double = 0.0,
    val unitPrice: Double = 0.0
) {
    val subtotal: Double get() = quantityNeeded * unitPrice
}
