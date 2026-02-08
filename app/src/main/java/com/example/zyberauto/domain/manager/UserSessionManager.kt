package com.example.zyberauto.domain.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var overrideUserId: String? = null

    fun setOverrideUser(uid: String?) {
        overrideUserId = uid
    }

    fun getCurrentUserId(): String? {
        if (overrideUserId != null) return overrideUserId
        
        // Local Migration: Provide the UID of the last logged-in user if persisted/remembered.
        // In a real app we'd decode this from a token or secure storage.
        // For this local json draft, we rely on what AuthViewModel/SplashViewModel saved.
        // However, they only saved 'email' and 'role'. 
        // We probably need to save 'uid' too to make this work effectively.
        
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("current_user_id", null)
    }
    
    fun isUserLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }

    // Helper to log in (save session) - called by AuthViewModel (if we update it)
    fun saveSession(uid: String, role: String) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("current_user_id", uid)
            .putString("user_role", role)
            .apply()
    }

    fun logout() {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("current_user_id").remove("user_role").apply()
        overrideUserId = null
    }
}
