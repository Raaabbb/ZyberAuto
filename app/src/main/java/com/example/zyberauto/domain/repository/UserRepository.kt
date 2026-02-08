package com.example.zyberauto.domain.repository

import com.example.zyberauto.domain.model.User

interface UserRepository {
    suspend fun getUserRole(uid: String): String?
    suspend fun createUserProfile(user: User)
    suspend fun getUserProfile(uid: String): User?
    suspend fun updateUserProfile(user: User)
    suspend fun updateUserVerificationStatus(uid: String, isVerified: Boolean)
    suspend fun getUserEmailByPhone(phoneNumber: String): String?
    fun getAllCustomers(): kotlinx.coroutines.flow.Flow<List<User>>
}
