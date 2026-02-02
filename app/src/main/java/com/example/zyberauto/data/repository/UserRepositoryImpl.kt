package com.example.zyberauto.data.repository

import com.example.zyberauto.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

import com.example.zyberauto.domain.model.User


class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUserRole(uid: String): String? {
        val document = firestore.collection("users").document(uid).get().await()
        return document.getString("role")
    }

    override suspend fun createUserProfile(user: User) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getUserProfile(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateUserProfile(user: User) {
        try {
            // Merge update to avoid overwriting unrelated fields if any
            val updates = mapOf(
                "name" to user.name,
                "phoneNumber" to user.phoneNumber,
                "email" to user.email // In case they changed it, though Auth handles email separately usually
            )
            firestore.collection("users").document(user.uid).update(updates).await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun updateUserVerificationStatus(uid: String, isVerified: Boolean) {
        try {
            firestore.collection("users").document(uid).update("isVerified", isVerified).await()
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    override fun getAllCustomers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("role", "CUSTOMER")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }
}
