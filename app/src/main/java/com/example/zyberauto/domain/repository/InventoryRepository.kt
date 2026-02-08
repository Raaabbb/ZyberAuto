package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.model.PartDeduction
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    /**
     * Get all inventory items as a real-time Flow.
     */
    fun getInventory(): Flow<List<InventoryItem>>
    
    /**
     * Add a new inventory item.
     */
    suspend fun addItem(item: InventoryItem)
    
    /**
     * Update an existing inventory item.
     */
    suspend fun updateItem(item: InventoryItem)
    
    /**
     * Delete an inventory item by ID.
     */
    suspend fun deleteItem(itemId: String)
    
    // ========== NEW: Cost Estimation Methods ==========
    
    /**
     * Get an inventory item by its name (case-insensitive).
     * Returns null if not found.
     */
    suspend fun getItemByName(name: String): InventoryItem?
    
    /**
     * Check if sufficient quantity of an item is available.
     * @param partName The name of the part/item
     * @param quantityNeeded The quantity required
     * @return true if available (quantity >= needed), false otherwise
     */
    suspend fun checkAvailability(partName: String, quantityNeeded: Double): Boolean
    
    /**
     * Deduct stock for a single item.
     * @param itemId The ID of the inventory item
     * @param amount The quantity to deduct
     * @return Result indicating success or failure
     */
    suspend fun deductStock(itemId: String, amount: Double): Result<Unit>
    
    /**
     * Batch deduct stock for multiple items (used when completing appointments).
     * Uses a transaction to ensure atomicity - all deductions succeed or none do.
     * @param deductions List of PartDeduction objects specifying what to deduct
     * @return Result indicating success or failure
     */
    suspend fun batchDeductStock(deductions: List<PartDeduction>): Result<Unit>
}
