package com.example.zyberauto.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.ui.theme.PrimaryRed
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") } // Changed username to email
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Handle State Changes
    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is RegisterUiState.Success -> {
                showSuccessDialog = true
            }
            is RegisterUiState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("Registration Successful") },
            text = { Text("Your account is ready. You can sign in now.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToLogin()
                    }
                ) {
                    Text("Go to Login")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        modifier = Modifier.padding(bottom = 16.dp),
                        errorMessage = if (name.isEmpty() && errorMessage != null) "Required" else null
                    )

                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        modifier = Modifier.padding(bottom = 16.dp),
                        errorMessage = if (email.isEmpty() && errorMessage != null) "Required" else null
                    )

                    AppTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isPassword = true,
                        modifier = Modifier.padding(bottom = 16.dp),
                        errorMessage = if (password.isEmpty() && errorMessage != null) "Required" else null
                    )

                    AppTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isPassword = true,
                        modifier = Modifier.padding(bottom = 24.dp),
                        errorMessage = if (confirmPassword != password && confirmPassword.isNotEmpty()) "Passwords do not match" else null
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    AppButton(
                        text = "REGISTER",
                        onClick = {
                            if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Please fill in all fields"
                            } else if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                            } else {
                                errorMessage = null
                                viewModel.register(email, password, name)
                            }
                        },
                        isLoading = uiState is RegisterUiState.Loading,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextButton(onClick = onNavigateToLogin) {
                        Text(text = "Already have an account? Sign In")
                    }
                }
            }
        }
    }
}
