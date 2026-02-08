package com.example.zyberauto.data.repository

import com.example.zyberauto.data.local.LocalDataHelper
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.domain.repository.VehiclesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehiclesRepositoryImpl @Inject constructor(
    private val localDataHelper: LocalDataHelper
) : VehiclesRepository {

    private val vehiclesFile = "vehicles.json"

    override fun getVehiclesForCustomer(customerId: String): Flow<List<Vehicle>> = flow {
        val vehicles = localDataHelper.readList(vehiclesFile, Vehicle::class.java)
        emit(vehicles.filter { it.customerId == customerId }.sortedByDescending { it.dateRegistered })
    }

    override suspend fun addVehicle(vehicle: Vehicle): Result<String> {
        return try {
            val docRef = if (vehicle.id.isBlank()) System.currentTimeMillis().toString() else vehicle.id
            val finalVehicle = vehicle.copy(id = docRef)
            localDataHelper.addItem(vehiclesFile, finalVehicle, Vehicle::class.java)
            Result.success(docRef)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> {
        return try {
            localDataHelper.removeItem(vehiclesFile, Vehicle::class.java) { it.id == vehicleId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> {
        return try {
            if (vehicle.id.isBlank()) {
                return Result.failure(IllegalArgumentException("Vehicle ID cannot be blank"))
            }
            localDataHelper.updateItem(
                vehiclesFile,
                Vehicle::class.java,
                predicate = { it.id == vehicle.id },
                update = { vehicle }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isPlateNumberRegistered(plateNumber: String): Boolean {
        val normalizedPlate = plateNumber.trim().uppercase()
        val vehicles = localDataHelper.readList(vehiclesFile, Vehicle::class.java)
        return vehicles.any { it.plateNumber.trim().uppercase() == normalizedPlate }
    }
}
