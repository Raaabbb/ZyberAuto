package com.example.zyberauto.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import kotlinx.coroutines.delay

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            // Local Migration: Check if we have a persisted session
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val shouldPersist = prefs.getBoolean("persist_session", false) 

            val savedEmail = prefs.getString("remember_email", null)

            // Simulate splash delay
            delay(1500)

            if (savedEmail != null && shouldPersist) {
                 // Check cached role first
                val role = prefs.getString("user_role", null)
                
                if (role != null) {
                    navigateBasedOnRole(role)
                } else {
                    // If no role cached but we have email, we could try to look up user.
                    // For now, we'll simpler logic: if role missing, go to login to be safe.
                    _destination.value = "login"
                }
            } else {
                // Not logged in
                _destination.value = "login"
            }
        }
    }

    private fun navigateBasedOnRole(role: String) {
        if (role == "SECRETARY") {
            _destination.value = "secretary_dashboard"
        } else {
            _destination.value = "customer_dashboard"
        }
    }
}
