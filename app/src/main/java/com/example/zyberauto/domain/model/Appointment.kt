package com.example.zyberauto.domain.model

import java.util.Date

data class Appointment(
    val id: String = "",
    val userId: String = "", // Link to Auth UID
    val customerName: String = "",
    val vehicleModel: String = "",
    val vehicleType: String = "Sedan", // Vehicle type for duration calculation
    val plateNumber: String = "",
    val serviceType: String = "",
    val notes: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED, COMPLETED
    val dateScheduled: Date = Date(),
    val timeSlot: String = "", // Start time (e.g., "11:00 AM")
    val timeSlotEnd: String = "", // End time (e.g., "12:00 PM")
    val estimatedDurationMinutes: Int = 60, // Duration in minutes
    val price: Double = 0.0,
    val createdAt: Date = Date(),
    val rejectionReason: String = "",
    val assignedMechanic: Int = 0, // 0 = unassigned, 1-3 = Mechanic 1, 2, 3
    
    // ========== COST ESTIMATION FIELDS ==========
    val estimatedLaborCost: Double = 0.0,
    val estimatedPartsCost: Double = 0.0,
    val estimatedTotalCost: Double = 0.0,
    
    // Inventory availability flag
    val hasInsufficientItems: Boolean = false,
    val insufficientItemsList: List<String> = emptyList(), // List of items that are low/out of stock
    
    // Parts to deduct from inventory when appointment is completed
    // Stored as list of maps for Firebase compatibility (kept for structure, but no longer strictly needed for FB)
    val partsToDeduct: List<Map<String, Any>> = emptyList()
) {
    /**
     * Helper to convert partsToDeduct maps back to PartDeduction objects.
     */
    fun getPartDeductions(): List<PartDeduction> {
        return partsToDeduct.map { map ->
            PartDeduction(
                itemId = map["itemId"] as? String ?: "",
                itemName = map["itemName"] as? String ?: "",
                quantityNeeded = (map["quantityNeeded"] as? Number)?.toDouble() ?: 0.0,
                unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
