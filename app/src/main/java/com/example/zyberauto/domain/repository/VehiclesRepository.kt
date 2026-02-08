package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Vehicle (customer cars) operations.
 */
interface VehiclesRepository {
    /**
     * Get all vehicles for a specific customer.
     */
    fun getVehiclesForCustomer(customerId: String): Flow<List<Vehicle>>
    
    /**
     * Add a new vehicle for the current customer.
     */
    suspend fun addVehicle(vehicle: Vehicle): Result<String>
    
    /**
     * Delete a vehicle by ID.
     */
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    
    /**
     * Update a vehicle.
     */
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit>
    
    /**
     * Check if a plate number already exists in the database.
     * Used to prevent duplicate vehicle registrations.
     * @param plateNumber The plate number to check (case-insensitive)
     * @return true if plate number exists, false otherwise
     */
    suspend fun isPlateNumberRegistered(plateNumber: String): Boolean
}
