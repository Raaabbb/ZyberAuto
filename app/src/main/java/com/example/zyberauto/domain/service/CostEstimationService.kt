package com.example.zyberauto.domain.service

import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.domain.model.PartDeduction
import com.example.zyberauto.domain.repository.InventoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of cost estimation including labor, parts, and inventory availability.
 */
data class CostEstimateResult(
    val laborCost: Double = 0.0,
    val partsCost: Double = 0.0,
    val totalEstimate: Double = 0.0,
    val partsToDeduct: List<PartDeduction> = emptyList(),
    val hasInsufficientItems: Boolean = false,
    val insufficientItems: List<String> = emptyList()
) {
    /**
     * Convert partsToDeduct to a list of maps for Firebase storage.
     */
    fun partsToDeductAsMaps(): List<Map<String, Any>> {
        return partsToDeduct.map { part ->
            mapOf(
                "itemId" to part.itemId,
                "itemName" to part.itemName,
                "quantityNeeded" to part.quantityNeeded,
                "unitPrice" to part.unitPrice
            )
        }
    }
}

/**
 * Service to calculate estimated costs for appointments.
 * Checks inventory availability and calculates labor + parts costs.
 */
@Singleton
class CostEstimationService @Inject constructor(
    private val inventoryRepository: InventoryRepository
) {
    /**
     * Calculate the estimated cost for a service.
     * 
     * @param serviceType The type of service (e.g., "Oil Change")
     * @param vehicleType The type of vehicle (e.g., "Sedan", "SUV")
     * @return CostEstimateResult containing labor cost, parts cost, total, and availability info
     */
    suspend fun calculateEstimate(
        serviceType: String,
        vehicleType: String
    ): CostEstimateResult {
        // 1. Calculate labor cost
        val laborCost = AppConstants.calculateLaborCost(serviceType, vehicleType)
        
        // 2. Get required parts for this service
        val requiredParts = AppConstants.ServiceRequiredParts[serviceType] ?: emptyList()
        
        // If no parts required, return labor-only estimate
        if (requiredParts.isEmpty()) {
            return CostEstimateResult(
                laborCost = laborCost,
                partsCost = 0.0,
                totalEstimate = laborCost,
                partsToDeduct = emptyList(),
                hasInsufficientItems = false,
                insufficientItems = emptyList()
            )
        }
        
        // 3. Calculate parts cost & check availability
        var partsCost = 0.0
        val partsToDeduct = mutableListOf<PartDeduction>()
        val insufficientItems = mutableListOf<String>()
        
        for (partReq in requiredParts) {
            val inventoryItem = inventoryRepository.getItemByName(partReq.partName)
            
            if (inventoryItem != null) {
                val subtotal = inventoryItem.price * partReq.quantityNeeded
                partsCost += subtotal
                
                partsToDeduct.add(PartDeduction(
                    itemId = inventoryItem.id,
                    itemName = inventoryItem.name,
                    quantityNeeded = partReq.quantityNeeded,
                    unitPrice = inventoryItem.price
                ))
                
                // Check if enough stock
                if (inventoryItem.quantity < partReq.quantityNeeded) {
                    val shortage = if (inventoryItem.quantity == 0) {
                        "${partReq.partName} (out of stock)"
                    } else {
                        "${partReq.partName} (need ${partReq.quantityNeeded.toInt()}, have ${inventoryItem.quantity})"
                    }
                    insufficientItems.add(shortage)
                }
            } else {
                // Item not in inventory at all
                insufficientItems.add("${partReq.partName} (not in inventory)")
            }
        }
        
        return CostEstimateResult(
            laborCost = laborCost,
            partsCost = partsCost,
            totalEstimate = laborCost + partsCost,
            partsToDeduct = partsToDeduct,
            hasInsufficientItems = insufficientItems.isNotEmpty(),
            insufficientItems = insufficientItems
        )
    }
    
    /**
     * Quick check for insufficient items without full cost calculation.
     * Useful for displaying warnings on existing appointments.
     */
    suspend fun checkInsufficientItems(serviceType: String): List<String> {
        val requiredParts = AppConstants.ServiceRequiredParts[serviceType] ?: return emptyList()
        val insufficientItems = mutableListOf<String>()
        
        for (partReq in requiredParts) {
            val inventoryItem = inventoryRepository.getItemByName(partReq.partName)
            
            if (inventoryItem == null) {
                insufficientItems.add("${partReq.partName} (not in inventory)")
            } else if (inventoryItem.quantity < partReq.quantityNeeded) {
                if (inventoryItem.quantity == 0) {
                    insufficientItems.add("${partReq.partName} (out of stock)")
                } else {
                    insufficientItems.add("${partReq.partName} (need ${partReq.quantityNeeded.toInt()}, have ${inventoryItem.quantity})")
                }
            }
        }
        
        return insufficientItems
    }
}
