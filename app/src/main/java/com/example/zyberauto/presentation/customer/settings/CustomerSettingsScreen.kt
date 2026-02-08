package com.example.zyberauto.presentation.customer.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSettingsScreen(
    onLogout: () -> Unit,
    viewModel: CustomerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local state for form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") } // Read-only for now
    var isInitialized by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    
    // Re-login dialog state
    var showReloginDialog by remember { mutableStateOf(false) }
    var reloginMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.Success -> {
                // Initialize fields only once to allow editing
                if (!isInitialized) {
                    name = state.user.name
                    phone = state.user.phoneNumber
                    isInitialized = true
                }
                email = state.user.email
                
                // Check if re-login is required
                if (state.requiresRelogin && state.reloginReason != null) {
                    reloginMessage = state.reloginReason
                    showReloginDialog = true
                } else if (state.message != null) {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetMessage()
                }
            }
            is SettingsUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    // Refresh profile every time to sync email
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState is SettingsUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profile Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )

                    AppTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone
                    )

                    AppTextField(
                        value = email,
                        onValueChange = {},
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        readOnly = true,
                        enabled = false // Visual cue that it's not editable
                    )

                    OutlinedButton(
                        onClick = { showChangeEmailDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Email")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        text = "SAVE CHANGES",
                        onClick = { viewModel.updateProfile(name, phone) },
                        enabled = name.isNotBlank() // Simple validation
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Password")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Button(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Log Out")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm new password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.sendPasswordResetEmail() }) {
                            Text("Forgot password?")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                        showChangePasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    },
                    enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text("Change Email") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("New email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeEmail(currentPassword, newEmail)
                        showChangeEmailDialog = false
                        currentPassword = ""
                        newEmail = ""
                    },
                    enabled = currentPassword.isNotBlank() && newEmail.isNotBlank()
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Re-login Required Dialog
    if (showReloginDialog) {
        AlertDialog(
            onDismissRequest = { }, // Cannot dismiss, must acknowledge
            title = { Text("Re-login Required") },
            text = {
                Text(reloginMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReloginDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("OK, Log Out")
                }
            }
        )
    }
}
