package com.example.zyberauto.domain.manager

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    private var overrideUserId: String? = null

    fun setOverrideUser(uid: String?) {
        overrideUserId = uid
    }

    fun getCurrentUserId(): String? {
        return overrideUserId ?: auth.currentUser?.uid
    }
    
    fun isUserLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }
}
