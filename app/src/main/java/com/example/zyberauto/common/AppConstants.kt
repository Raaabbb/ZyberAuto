package com.example.zyberauto.common

/**
 * Part requirement for a service type.
 * Maps part name to quantity needed per service.
 */
data class PartRequirement(
    val partName: String,
    val quantityNeeded: Double,
    val isRequired: Boolean = true
)

object AppConstants {
    val ServiceTypes = listOf(
        "Oil Change",
        "Tire Rotation",
        "Brake Inspection",
        "General Service",
        "Diagnostic",
        "Battery Replacement",
        "Air Conditioning Service"
    )

    val VehicleTypes = listOf(
        "Sedan",
        "Hatchback",
        "SUV",
        "Crossover",
        "Van/Minivan",
        "Pickup Truck",
        "Motorcycle",
        "Sports Car"
    )

    // Base duration in MINUTES for each service type
    val ServiceBaseDurations = mapOf(
        "Oil Change" to 60,              // 1 hour base
        "Tire Rotation" to 45,           // 45 mins base
        "Brake Inspection" to 90,        // 1.5 hours base
        "General Service" to 120,        // 2 hours base
        "Diagnostic" to 60,              // 1 hour base
        "Battery Replacement" to 30,     // 30 mins base
        "Air Conditioning Service" to 90 // 1.5 hours base
    )

    // Multiplier based on vehicle type (larger vehicles = more time)
    val VehicleTypeMultipliers = mapOf(
        "Motorcycle" to 0.5,       // 50% of base time
        "Hatchback" to 0.9,        // 90% of base time
        "Sedan" to 1.0,            // Base time (reference)
        "Sports Car" to 1.1,       // 110% of base time
        "Crossover" to 1.2,        // 120% of base time
        "SUV" to 1.3,              // 130% of base time
        "Pickup Truck" to 1.4,     // 140% of base time
        "Van/Minivan" to 1.5       // 150% of base time
    )

    // ========== COST ESTIMATION ==========
    
    /**
     * Base labor prices (in Pesos) for each service type.
     * Final labor cost = Base Price × Vehicle Multiplier
     */
    val ServiceBasePrices = mapOf(
        "Oil Change" to 500.0,
        "Tire Rotation" to 300.0,
        "Brake Inspection" to 400.0,
        "General Service" to 800.0,
        "Diagnostic" to 350.0,
        "Battery Replacement" to 200.0,
        "Air Conditioning Service" to 600.0
    )

    /**
     * Required parts for each service type.
     * These parts will be checked against inventory and deducted on completion.
     */
    val ServiceRequiredParts: Map<String, List<PartRequirement>> = mapOf(
        "Oil Change" to listOf(
            PartRequirement("Oil Filter", 1.0),
            PartRequirement("Engine Oil", 4.0)  // 4 liters
        ),
        "Tire Rotation" to listOf(
            // No consumable parts, labor only
        ),
        "Brake Inspection" to listOf(
            PartRequirement("Brake Fluid", 0.5)  // 0.5 liters
        ),
        "General Service" to listOf(
            PartRequirement("Oil Filter", 1.0),
            PartRequirement("Engine Oil", 4.0),
            PartRequirement("Air Filter", 1.0)
        ),
        "Diagnostic" to listOf(
            // No consumable parts, equipment only
        ),
        "Battery Replacement" to listOf(
            PartRequirement("Car Battery", 1.0)
        ),
        "Air Conditioning Service" to listOf(
            PartRequirement("AC Refrigerant", 1.0),
            PartRequirement("AC Filter", 1.0)
        )
    )

    /**
     * Calculate labor cost based on service type and vehicle type.
     * @return Labor cost in Pesos
     */
    fun calculateLaborCost(serviceType: String, vehicleType: String): Double {
        val basePrice = ServiceBasePrices[serviceType] ?: 500.0
        val multiplier = VehicleTypeMultipliers[vehicleType] ?: 1.0
        return basePrice * multiplier
    }

    /**
     * Calculate service duration based on service type and vehicle type.
     * Rounds to nearest 15 minutes for scheduling convenience.
     * @return Duration in minutes
     */
    fun calculateServiceDuration(serviceType: String, vehicleType: String): Int {
        val baseDuration = ServiceBaseDurations[serviceType] ?: 60
        val multiplier = VehicleTypeMultipliers[vehicleType] ?: 1.0
        val rawDuration = (baseDuration * multiplier).toInt()
        // Round to nearest 15 minutes
        return ((rawDuration + 7) / 15) * 15
    }

    /**
     * Format duration in minutes to readable string.
     * @return e.g., "1h 30m" or "45m"
     */
    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    val TimeSlots = listOf(
        "08:00 AM - 10:00 AM",
        "10:00 AM - 12:00 PM",
        "01:00 PM - 03:00 PM",
        "03:00 PM - 05:00 PM"
    )

    val InventoryCategories = listOf(
        "Parts",
        "Fluids",
        "Tools",
        "Accessories",
        "Other"
    )
}
