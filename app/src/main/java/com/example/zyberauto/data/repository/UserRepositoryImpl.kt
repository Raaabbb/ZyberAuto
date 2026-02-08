package com.example.zyberauto.data.repository

import com.example.zyberauto.data.local.LocalDataHelper
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataHelper: LocalDataHelper
) : UserRepository {

    private val userFile = "users.json"

    override suspend fun getUserRole(uid: String): String? {
        val user = getUserProfile(uid)
        return user?.role
    }

    override suspend fun createUserProfile(user: User) {
        val users = localDataHelper.readList(userFile, User::class.java).toMutableList()
        // Check if user exists to avoid duplicates
        val index = users.indexOfFirst { it.uid == user.uid }
        if (index != -1) {
            users[index] = user
            localDataHelper.writeList(userFile, users)
        } else {
            localDataHelper.addItem(userFile, user, User::class.java)
        }
    }

    override suspend fun getUserProfile(uid: String): User? {
        val users = localDataHelper.readList(userFile, User::class.java)
        return users.find { it.uid == uid }
    }

    override suspend fun updateUserProfile(user: User) {
        localDataHelper.updateItem(
            userFile,
            User::class.java,
            predicate = { it.uid == user.uid },
            update = { user }
        )
    }
    
    override suspend fun updateUserVerificationStatus(uid: String, isVerified: Boolean) {
        localDataHelper.updateItem(
            userFile,
            User::class.java,
            predicate = { it.uid == uid },
            update = { it.copy(isVerified = isVerified) }
        )
    }

    override suspend fun getUserEmailByPhone(phoneNumber: String): String? {
        val users = localDataHelper.readList(userFile, User::class.java)
        return users.find { it.phoneNumber == phoneNumber }?.email
    }

    override fun getAllCustomers(): Flow<List<User>> = flow {
        val users = localDataHelper.readList(userFile, User::class.java)
        emit(users.filter { it.role == "CUSTOMER" })
    }
    
    // Additional method for AuthViewModel to Validate Login locally
    suspend fun validateUser(email: String, password: String): User? {
        val users = localDataHelper.readList(userFile, User::class.java)
        return users.find { 
            it.email.equals(email, ignoreCase = true) && it.password == password 
        }
    }
}
