package com.example.zyberauto.data.repository

import com.example.zyberauto.data.local.LocalDataHelper
import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.model.PartDeduction
import com.example.zyberauto.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val localDataHelper: LocalDataHelper
) : InventoryRepository {

    private val inventoryFile = "inventory.json"

    override fun getInventory(): Flow<List<InventoryItem>> = flow {
        val items = localDataHelper.readList(inventoryFile, InventoryItem::class.java)
        // Sort by name ascending
        emit(items.sortedBy { it.name })
    }

    override suspend fun addItem(item: InventoryItem) {
        // If ID is missing, generate one (basic UUID or Timestamp)
        val finalItem = if (item.id.isBlank()) {
            item.copy(id = System.currentTimeMillis().toString(), lastUpdated = Date())
        } else {
            item.copy(lastUpdated = Date())
        }
        localDataHelper.addItem(inventoryFile, finalItem, InventoryItem::class.java)
    }

    override suspend fun updateItem(item: InventoryItem) {
        if (item.id.isNotBlank()) {
            val itemWithTimestamp = item.copy(lastUpdated = Date())
            localDataHelper.updateItem(
                inventoryFile,
                InventoryItem::class.java,
                predicate = { it.id == item.id },
                update = { itemWithTimestamp }
            )
        }
    }

    override suspend fun deleteItem(itemId: String) {
        localDataHelper.removeItem(inventoryFile, InventoryItem::class.java) { it.id == itemId }
    }

    override suspend fun getItemByName(name: String): InventoryItem? {
        val items = localDataHelper.readList(inventoryFile, InventoryItem::class.java)
        return items.find { it.name.equals(name, ignoreCase = true) }
    }

    override suspend fun checkAvailability(partName: String, quantityNeeded: Double): Boolean {
        val item = getItemByName(partName) ?: return false
        return item.quantity >= quantityNeeded
    }

    override suspend fun deductStock(itemId: String, amount: Double): Result<Unit> {
        return try {
            localDataHelper.updateItem(
                inventoryFile,
                InventoryItem::class.java,
                predicate = { it.id == itemId },
                update = { currentItem ->
                    val newQuantity = (currentItem.quantity - amount.toInt()).coerceAtLeast(0)
                    currentItem.copy(quantity = newQuantity, lastUpdated = Date())
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun batchDeductStock(deductions: List<PartDeduction>): Result<Unit> {
        return try {
            if (deductions.isEmpty()) return Result.success(Unit)
            
            // In a real DB we'd use a transaction. Here we just loop sequentially.
            // If one fails mid-way, we are in an inconsistent state, but acceptable for this local draft.
            val items = localDataHelper.readList(inventoryFile, InventoryItem::class.java).toMutableList()
            
            deductions.forEach { deduction ->
                if (deduction.itemId.isNotBlank()) {
                    val index = items.indexOfFirst { it.id == deduction.itemId }
                    if (index != -1) {
                        val currentItem = items[index]
                        val newQuantity = (currentItem.quantity - deduction.quantityNeeded.toInt()).coerceAtLeast(0)
                        items[index] = currentItem.copy(quantity = newQuantity, lastUpdated = Date())
                    }
                }
            }
            localDataHelper.writeList(inventoryFile, items)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
