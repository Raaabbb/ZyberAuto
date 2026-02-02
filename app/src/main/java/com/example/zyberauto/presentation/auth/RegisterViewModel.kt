package com.example.zyberauto.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                // 1. Create Auth User
                android.util.Log.d("REG_VM", "Creating user: $email")
                auth.createUserWithEmailAndPassword(email, password).await()
                val uid = auth.currentUser?.uid

                if (uid != null) {
                    // 2. Create Firestore Profile
                    val newUser = User(
                        uid = uid,
                        email = email,
                        name = username, // Use username as name for now
                        role = "CUSTOMER", // Default role
                        phoneNumber = "",
                        isVerified = true
                    )
                    android.util.Log.d("REG_VM", "Saving profile to Firestore: $newUser")
                    userRepository.createUserProfile(newUser)

                    _uiState.value = RegisterUiState.Success
                } else {
                    _uiState.value = RegisterUiState.Error("Registration failed: No UID returned")
                }
            } catch (e: Exception) {
                android.util.Log.e("REG_VM", "Registration Error", e)
                // improve error message
                val msg = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Email already in use."
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is com.google.firebase.FirebaseNetworkException -> "Network error. Check internet connection."
                    else -> e.message ?: "Registration failed"
                }
                _uiState.value = RegisterUiState.Error(msg)
            }
        }
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
