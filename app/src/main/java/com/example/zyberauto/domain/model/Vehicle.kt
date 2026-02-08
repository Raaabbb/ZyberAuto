package com.example.zyberauto.domain.model

/**
 * Vehicle data model representing a customer's registered car.
 * Stored in local JSON file: vehicles.json
 * 
 * @param id Unique identifier
 * @param customerId ID of the customer who owns this vehicle
 * @param model Vehicle model (e.g., "Toyota Vios", "Honda Civic")
 * @param vehicleType Type of vehicle (e.g., "Sedan", "SUV", "Van/Minivan")
 * @param plateNumber License plate number
 * @param year Manufacturing year
 * @param dateRegistered Timestamp when vehicle was registered
 */
data class Vehicle(
    val id: String = "",
    val customerId: String = "",
    val model: String = "",
    val vehicleType: String = "Sedan", // Default to Sedan for backward compatibility
    val plateNumber: String = "",
    val year: String = "",
    val dateRegistered: Long = System.currentTimeMillis()
)
