package com.example.zyberauto.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.ui.theme.PrimaryRed

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    // Form state - single field for email or phone
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val savedEmail = viewModel.getSavedEmail()
        if (!savedEmail.isNullOrEmpty()) {
            emailOrPhone = savedEmail
            rememberMe = true
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                isLoading = false
                onLoginSuccess(state.role)
            }
            is AuthUiState.Error -> {
                isLoading = false
                errorMessage = state.message
            }
            is AuthUiState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AuthUiState.Message -> {
                isLoading = false
                errorMessage = null
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF0F172A)
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding)
        ) {
        // Decorative Orbs
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 150.dp, y = (-50).dp)
                .blur(80.dp)
                .background(PrimaryRed.copy(alpha = 0.3f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .blur(60.dp)
                .background(PrimaryRed.copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            LogoSection()
            Spacer(modifier = Modifier.height(48.dp))

            // Login Card - Single field for email or phone
            LoginCard(
                emailOrPhone = emailOrPhone,
                onEmailOrPhoneChange = { emailOrPhone = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
                errorMessage = errorMessage,
                isLoading = isLoading,
                onSignIn = {
                    if (emailOrPhone.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter email/phone and password"
                    } else {
                        errorMessage = null
                        // Detect if input is phone or email
                        val input = emailOrPhone.trim()
                        val isPhone = input.all { it.isDigit() } || input.startsWith("+")
                        
                        if (isPhone) {
                            // Format as Philippine number
                            val cleanNum = input.replace("+63", "").replace("+", "")
                            val formatted = if (cleanNum.startsWith("0")) cleanNum.substring(1) else cleanNum
                            val fullNumber = "+63$formatted"
                            viewModel.loginWithPhoneAndPassword(fullNumber, password, rememberMe)
                        } else {
                            viewModel.login(input, password, rememberMe)
                        }
                    }
                },
                onNavigateToRegister = onNavigateToRegister,
                onForgotPassword = { showForgotPasswordDialog = true }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            viewModel.sendPasswordResetEmail(resetEmail)
                            showForgotPasswordDialog = false
                        }
                    },
                    enabled = resetEmail.isNotBlank()
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    }
}

@Composable
private fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .blur(30.dp)
                    .background(PrimaryRed.copy(alpha = 0.5f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(PrimaryRed, Color(0xFFB91C1C))),
                        CircleShape
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, "Logo", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("ZYBERAUTO", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 4.sp), color = Color.White)
        Text("Premium Auto Service", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp)
    }
}

@Composable
private fun LoginCard(
    emailOrPhone: String,
    onEmailOrPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Single field for Email or Phone
            StyledTextField(
                value = emailOrPhone, 
                onValueChange = onEmailOrPhoneChange, 
                placeholder = "Email or Phone Number", 
                leadingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(16.dp))
            StyledTextField(
                value = password, 
                onValueChange = onPasswordChange, 
                placeholder = "Password", 
                leadingIcon = Icons.Default.Lock, 
                isPassword = true, 
                passwordVisible = passwordVisible, 
                onTogglePasswordVisibility = onTogglePasswordVisibility
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe, 
                        onCheckedChange = { onRememberMeChange(it) }, 
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryRed, uncheckedColor = Color.White.copy(alpha = 0.6f), checkmarkColor = Color.White)
                    )
                    Text("Remember Me", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.clickable { onRememberMeChange(!rememberMe) })
                }
                
                Text(
                    "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryRed,
                    modifier = Modifier.clickable { onForgotPassword() }
                )
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage, color = PrimaryRed, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSignIn, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                shape = RoundedCornerShape(16.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed, contentColor = Color.White), 
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("SIGN IN", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                Text("Register", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryRed, modifier = Modifier.clickable { onNavigateToRegister() })
            }
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    val trailingIconComposable: @Composable (() -> Unit)? = if (isPassword) {
        {
            IconButton(onClick = { onTogglePasswordVisibility?.invoke() }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Password Visibility",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    } else null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        leadingIcon = { Icon(leadingIcon, null, tint = Color.White.copy(alpha = 0.6f)) },
        trailingIcon = trailingIconComposable,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedBorderColor = PrimaryRed,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            cursorColor = PrimaryRed
        )
    )
}
