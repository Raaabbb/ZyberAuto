package com.example.zyberauto.presentation.customer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CustomerSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            
            // Force refresh of Auth User to get latest email status
            try {
                auth.currentUser?.reload()?.await()
                android.util.Log.d("SettingsVM", "Auth user reloaded via loadProfile")
            } catch (e: Exception) {
                android.util.Log.e("SettingsVM", "Failed to reload auth user", e)
            }
            
            val uid = auth.currentUser?.uid
            val authEmail = auth.currentUser?.email
            
            android.util.Log.d("SettingsVM", "Auth Email after reload: $authEmail")
            
            if (uid != null) {
                var user = userRepository.getUserProfile(uid)
                
                if (user != null) {
                    // Self-healing: Update Firestore if Auth Email changed (verified)
                    if (!authEmail.isNullOrBlank() && user.email != authEmail) {
                        android.util.Log.d("SettingsVM", "Syncing email: ${user.email} -> $authEmail")
                        val syncedUser = user.copy(email = authEmail)
                        userRepository.updateUserProfile(syncedUser)
                        user = syncedUser
                    }
                    
                    currentUser = user
                    _uiState.value = SettingsUiState.Success(user)
                } else {
                    _uiState.value = SettingsUiState.Error("User profile not found.")
                }
            } else {
                _uiState.value = SettingsUiState.Error("Not logged in.")
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            // Optimistic / Loading state not strictly needed for small updates but good practice
            // We keep showing Success state but could add a "Saving" flag if we wanted complex UI
            
            try {
                // Create updated user object
                // We don't change email here for now as that requires Auth re-verification usually
                val updatedUser = currentUser?.copy(
                    name = name,
                    phoneNumber = phone
                ) ?: User(uid = uid, name = name, phoneNumber = phone) // Fallback if currentUser was null

                userRepository.updateUserProfile(updatedUser)
                
                // Refresh local state
                currentUser = updatedUser
                _uiState.value = SettingsUiState.Success(updatedUser, message = "Profile updated successfully!")
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update profile: ${e.message}")
                // revert to previous success state after error logic if needed, 
                // but Error state is fine to show the problem
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.value = SettingsUiState.Error("New password and confirmation do not match.")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = SettingsUiState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val email = user?.email
                if (user == null || email.isNullOrBlank()) {
                    _uiState.value = SettingsUiState.Error("No authenticated user.")
                    return@launch
                }

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()

                currentUser?.let {
                    _uiState.value = SettingsUiState.Success(it, message = "Password updated successfully!")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Failed to update password.")
            }
        }
    }

    fun changeEmail(currentPassword: String, newEmail: String) {
        if (newEmail.isBlank() || !newEmail.contains("@")) {
            _uiState.value = SettingsUiState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val oldEmail = user?.email
                if (user == null || oldEmail.isNullOrBlank()) {
                    _uiState.value = SettingsUiState.Error("No authenticated user.")
                    return@launch
                }

                val credential = EmailAuthProvider.getCredential(oldEmail, currentPassword)
                user.reauthenticate(credential).await()
                
                user.verifyBeforeUpdateEmail(newEmail).await()

                // We do NOT update Firestore yet because the email hasn't changed.
                // We do NOT update currentUser local state either.
                
                _uiState.value = SettingsUiState.Success(
                    currentUser ?: User(uid = user.uid), 
                    message = "Verification email sent to $newEmail. Waiting for confirmation..."
                )
                
                monitorEmailVerification(newEmail)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("malformed", ignoreCase = true) == true || 
                              e.message?.contains("expired", ignoreCase = true) == true) {
                    "Session expired. Please Log Out and Log In again."
                } else {
                    e.message ?: "Failed to send verification email."
                }
                _uiState.value = SettingsUiState.Error(msg)
            }
        }
    }

    private fun monitorEmailVerification(targetEmail: String) {
        viewModelScope.launch {
            var checks = 0
            while (checks < 60) {
                delay(5000)
                checks++
                try {
                    auth.currentUser?.reload()?.await()
                    if (auth.currentUser?.email?.equals(targetEmail, ignoreCase = true) == true) {
                        loadProfile()
                        break
                    }
                } catch (e: Exception) { }
            }
        }
    }

    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email
                if (email.isNullOrBlank()) {
                    _uiState.value = SettingsUiState.Error("No email found for this account.")
                    return@launch
                }
                auth.sendPasswordResetEmail(email).await()
                currentUser?.let {
                    _uiState.value = SettingsUiState.Success(it, message = "Password reset email sent.")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Failed to send reset email.")
            }
        }
    }
    
    fun resetMessage() {
        (_uiState.value as? SettingsUiState.Success)?.let {
            _uiState.value = it.copy(message = null)
        }
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val user: User, val message: String? = null) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
