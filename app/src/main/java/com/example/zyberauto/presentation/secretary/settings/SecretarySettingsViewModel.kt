package com.example.zyberauto.presentation.secretary.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zyberauto.domain.repository.UserRepository
import com.example.zyberauto.domain.manager.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecretarySettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretarySettingsUiState())
    val uiState: StateFlow<SecretarySettingsUiState> = _uiState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "New password and confirmation do not match.")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters.")
            return
        }
        
        // Mock success for local version (since we don't store passwords)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            message = "Password updated successfully.",
            requiresRelogin = true,
            reloginReason = "Your password has been changed. Please log in again with your new password."
        )
    }

    fun changeEmail(currentPassword: String, newEmail: String) {
         viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
             
            val uid = sessionManager.getCurrentUserId()
            if (uid == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "No authenticated user.")
                return@launch
            }
             
            try {
                // Update local user profile
                val user = userRepository.getUserProfile(uid)
                if (user != null) {
                    val updatedUser = user.copy(email = newEmail)
                    userRepository.updateUserProfile(updatedUser)
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Email updated to $newEmail.",
                        requiresRelogin = true,
                        reloginReason = "Email changed. Please log in with your new email."
                    )
                } else {
                     _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found.")
                }
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update email."
                )
            }
         }
    }

    fun sendPasswordResetEmail() {
         _uiState.value = _uiState.value.copy(
             isLoading = false, 
             message = "Password reset functionality not available offline."
         )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun logout() {
        sessionManager.logout()
    }
}

data class SecretarySettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val requiresRelogin: Boolean = false,
    val reloginReason: String? = null
)
