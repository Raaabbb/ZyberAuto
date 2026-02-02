package com.example.zyberauto.data.repository

import com.example.zyberauto.domain.model.InventoryItem
import com.example.zyberauto.domain.repository.InventoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InventoryRepository {

    override fun getInventory(): Flow<List<InventoryItem>> = callbackFlow {
        val listener = firestore.collection("inventory")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.toObjects(InventoryItem::class.java) ?: emptyList()
                trySend(items)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun addItem(item: InventoryItem) {
        // ID is auto-generated if empty, or we can let Firestore generate it by using .add()
        // If the item passed has no ID (which it shouldn't for new items usually), we use .add()
        // But to be consistent with update, we might want to manually set IDs or just use .add()
        // Since @DocumentId is used, typically we just .add(item) and Firestore handles it.
        firestore.collection("inventory").add(item).await()
    }

    override suspend fun updateItem(item: InventoryItem) {
        if (item.id.isNotBlank()) {
            firestore.collection("inventory").document(item.id).set(item).await()
        }
    }

    override suspend fun deleteItem(itemId: String) {
        firestore.collection("inventory").document(itemId).delete().await()
    }
}
