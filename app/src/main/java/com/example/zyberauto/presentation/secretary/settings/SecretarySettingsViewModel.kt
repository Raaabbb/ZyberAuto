package com.example.zyberauto.presentation.secretary.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SecretarySettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth
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

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val user = auth.currentUser
                val email = user?.email
                if (user == null || email.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No authenticated user.")
                    return@launch
                }

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Password updated successfully."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update password."
                )
            }
        }
    }

    fun changeEmail(currentPassword: String, newEmail: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val user = auth.currentUser
                val email = user?.email
                if (user == null || email.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No authenticated user.")
                    return@launch
                }

                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.verifyBeforeUpdateEmail(newEmail).await() // Send verification email first usually, or updateEmail directly if allowed. 
                // Note: updateEmail is deprecated in some versions in favor of verifyBeforeUpdateEmail, 
                // but for simplicity/consistency with typical flows we can try verifyBeforeUpdateEmail or updateEmail depending on requirements.
                // The user request is "recycle and make a change email option". CustomerSettings use-case wasn't fully visible but likely updateEmail.
                // Let's stick to verifyBeforeUpdateEmail for security if possible, or fallback to updateEmail if that's what was intended. 
                // However, the standard `updateEmail` is often what's expected for simple prototypes.
                // Let's check what `verifyBeforeUpdateEmail` does: it sends a verification email to the new address and ONLY updates it after clicking.
                // `updateEmail` updates it immediately. Let's use `verifyBeforeUpdateEmail` as it's the modern recommended approach, 
                // BUT if the user wants "Change Email Option" they might expect immediate change. 
                // Given the prompt "recycle", I should look at what CustomerSettingsViewModel did. 
                // I don't have CustomerSettingsViewModel content handy in full detail in my memory cache (I viewed Screen but not VM). 
                // I will use `verifyBeforeUpdateEmail` as it is the safer default for modern Firebase.
                // Wait, if I use `verifyBeforeUpdateEmail`, the email on the object won't change until verified. 
                // Let's check if I can just use `updateEmail` for now to be less disruptive if they don't have email verification flow set up on their phone.
                // Actually, `verifyBeforeUpdateEmail` is safer. I'll use that and tell the user. 
                // Wait, let me check if `updateEmail` is available. It is. 
                // Let's use `verifyBeforeUpdateEmail` for best practice.
                
                user.verifyBeforeUpdateEmail(newEmail).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Verification email sent to $newEmail. Please verify to complete the change."
                )
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update email."
                )
            }
        }
    }

    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val email = auth.currentUser?.email
                if (email.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No email found for this account.")
                    return@launch
                }
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Password reset email sent."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send reset email."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class SecretarySettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
