package com.example.zyberauto.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Simple email-based registration (phone is just a field, not verified)
    fun register(email: String, password: String, username: String, phoneNumber: String = "") {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                // Email is required
                if (email.isBlank()) {
                    throw Exception("Email is required.")
                }

                // Simulate network delay
                delay(1000)

                // Generate a random UID locally
                val newUid = "user_" + System.currentTimeMillis()
                
                // Save to Local JSON
                saveUserToLocal(
                    uid = newUid,
                    email = email,
                    username = username,
                    phoneNumber = phoneNumber,
                    isVerified = true // Auto verify local users
                )
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private suspend fun saveUserToLocal(uid: String, email: String, username: String, phoneNumber: String, isVerified: Boolean) {
        val newUser = User(
            uid = uid,
            email = email,
            name = username,
            role = "CUSTOMER",
            phoneNumber = phoneNumber,
            isVerified = isVerified
        )
        userRepository.createUserProfile(newUser)
        _uiState.value = RegisterUiState.Success
    }

    fun resendVerificationEmail() {
        // No-op for local
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
