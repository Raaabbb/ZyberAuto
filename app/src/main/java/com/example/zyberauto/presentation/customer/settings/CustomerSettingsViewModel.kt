package com.example.zyberauto.presentation.customer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.model.User
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
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
            
            val uid = sessionManager.getCurrentUserId()
            
            if (uid != null) {
                val user = userRepository.getUserProfile(uid)
                
                if (user != null) {
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
            val uid = sessionManager.getCurrentUserId() ?: return@launch
            
            try {
                val updatedUser = currentUser?.copy(
                    name = name,
                    phoneNumber = phone
                ) ?: User(uid = uid, name = name, phoneNumber = phone)

                userRepository.updateUserProfile(updatedUser)
                
                // Refresh local state
                currentUser = updatedUser
                _uiState.value = SettingsUiState.Success(updatedUser, message = "Profile updated successfully!")
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update profile: ${e.message}")
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
            // Mock success for local version
            currentUser?.let {
                _uiState.value = SettingsUiState.Success(
                    user = it, 
                    message = "Password updated successfully!",
                    requiresRelogin = true,
                    reloginReason = "Your password has been changed. Please log in again with your new password."
                )
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
                val uid = sessionManager.getCurrentUserId()
                if (uid == null) {
                    _uiState.value = SettingsUiState.Error("No authenticated user.")
                    return@launch
                }
                
                // Update local user profile
                val user = userRepository.getUserProfile(uid)
                if (user != null) {
                     val updatedUser = user.copy(email = newEmail)
                     userRepository.updateUserProfile(updatedUser)
                     
                     _uiState.value = SettingsUiState.Success(
                        user = updatedUser, 
                        message = "Email updated to $newEmail.",
                        requiresRelogin = true,
                        reloginReason = "Email changed. Please log in with your new email."
                    )
                } else {
                    _uiState.value = SettingsUiState.Error("User not found.")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Failed to update email.")
            }
        }
    }

    fun sendPasswordResetEmail() {
        _uiState.value = SettingsUiState.Error("Password reset not supported offline.")
    }
    
    fun resetMessage() {
        (_uiState.value as? SettingsUiState.Success)?.let {
            _uiState.value = it.copy(message = null)
        }
    }

    fun logout() {
        sessionManager.logout()
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val user: User, 
        val message: String? = null,
        val requiresRelogin: Boolean = false,
        val reloginReason: String? = null
    ) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
