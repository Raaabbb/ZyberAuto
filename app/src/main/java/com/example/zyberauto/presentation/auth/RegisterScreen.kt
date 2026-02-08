package com.example.zyberauto.presentation.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.ui.theme.PrimaryRed

data class VehicleFormData(
    val model: String = "",
    val plateNumber: String = "",
    val year: String = ""
)

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Form State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") } // Phone is now just a regular field
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Vehicle State
    var showVehicleSection by remember { mutableStateOf(false) }
    var vehicles by remember { mutableStateOf(listOf(VehicleFormData())) }
    
    // Observables
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Effects
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RegisterUiState.Success -> showSuccessDialog = true
            is RegisterUiState.Error -> errorMessage = state.message
            else -> {}
        }
    }

    /* ====== PHONE OTP STATUS LISTENER COMMENTED OUT ======
    val otpStatus by viewModel.otpState.collectAsStateWithLifecycle()
    
    LaunchedEffect(otpStatus) {
        if (otpStatus is OtpStatus.Error) {
            errorMessage = (otpStatus as OtpStatus.Error).message
        }
    }
    ====== END PHONE OTP STATUS LISTENER COMMENTED OUT ====== */

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1E293B),
            title = { 
                Text("Registration Successful!", color = Color.White, fontWeight = FontWeight.Bold) 
            },
            text = { 
                Column {
                    Text(
                        "Your account has been created.",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "We sent a verification link to $email.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Please check your SPAM folder if you don't see it.",
                        color = PrimaryRed,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.resendVerificationEmail() }) {
                        Text("Resend Verification Email", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Go to Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        // Decor
        Box(modifier = Modifier.size(200.dp).offset(200.dp, 100.dp).blur(80.dp).background(PrimaryRed.copy(0.2f), CircleShape))
        
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back
            Row(modifier = Modifier.fillMaxWidth().clickable { onNavigateToLogin() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Back to Login", color = Color.White.copy(0.7f))
            }
            
            Spacer(Modifier.height(24.dp))
            RegisterHeader()
            Spacer(Modifier.height(24.dp))

            /* ====== PHONE VERIFICATION CARD COMMENTED OUT ======
            // Phone Number Section (REQUIRED - moved to top)
            PhoneVerificationCard(
                phone = phone,
                onPhoneChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                otpCode = otpCode,
                onOtpChange = { if (it.length <= 6) otpCode = it.filter { c -> c.isDigit() } },
                otpStatus = otpStatus,
                onSendOtp = {
                    if (activity != null && phone.length >= 9) {
                        viewModel.sendOtp("+63$phone", activity)
                    } else {
                        errorMessage = "Please enter a valid PH number"
                    }
                },
                onVerifyOtp = { viewModel.verifyOtp(otpCode) }
            )
            
            Spacer(Modifier.height(16.dp))
            ====== END PHONE VERIFICATION CARD COMMENTED OUT ====== */

            // Account Form (now includes phone as a regular field)
            AccountFormCard(
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                phone = phone,
                onPhoneChange = { phone = it },
                password = password,
                onPasswordChange = { password = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                errorMessage = errorMessage
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Register Button
            Button(
                onClick = {
                    // Validate fields
                    if (name.isBlank()) {
                        errorMessage = "Name is required"
                        return@Button
                    }
                    
                    // Email is REQUIRED now
                    if (email.isBlank()) {
                        errorMessage = "Email is required"
                        return@Button
                    }
                    
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid email address"
                        return@Button
                    }
                    
                    // Password validation
                    if (password.length < 6) {
                        errorMessage = "Password must be 6+ chars"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    
                    // Clear error and register
                    errorMessage = null
                    viewModel.register(email, password, name, phone)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                enabled = uiState !is RegisterUiState.Loading
            ) {
                if (uiState is RegisterUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.clickable { onNavigateToLogin() }
            ) {
                Text("Already have an account? ", color = Color.White.copy(0.6f))
                Text("Sign In", color = PrimaryRed, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

/* ====== PHONE VERIFICATION CARD COMPOSABLE COMMENTED OUT ======
@Composable
fun PhoneVerificationCard(
    phone: String,
    onPhoneChange: (String) -> Unit,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    otpStatus: OtpStatus,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit
) {
    GlassCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Phone Number (Required)", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (otpStatus is OtpStatus.Verified) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(20.dp))
                }
            }
            Text(
                "Verify your phone number to create an account",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(20.dp))
            
            // Phone Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("+63", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    DarkTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        placeholder = "912 345 6789",
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = otpStatus !is OtpStatus.Verified
                    )
                }
                if (otpStatus !is OtpStatus.Verified && otpStatus !is OtpStatus.CodeSent) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSendOtp,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(12.dp),
                        enabled = otpStatus !is OtpStatus.Sending
                    ) {
                        if (otpStatus is OtpStatus.Sending) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Send")
                        }
                    }
                }
            }
            
            // OTP Input
            if (otpStatus is OtpStatus.CodeSent || otpStatus is OtpStatus.Error) {
                Spacer(Modifier.height(12.dp))
                Text("Enter the 6-digit code sent to you", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        DarkTextField(
                            value = otpCode,
                            onValueChange = onOtpChange,
                            placeholder = "123456",
                            leadingIcon = Icons.Default.Lock,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onVerifyOtp,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}
====== END PHONE VERIFICATION CARD COMPOSABLE COMMENTED OUT ====== */

@Composable
fun AccountFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    errorMessage: String?
) {
    GlassCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Account Details", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(Modifier.height(20.dp))
            
            DarkTextField(value = name, onValueChange = onNameChange, placeholder = "Full Name", leadingIcon = Icons.Default.Person)
            Spacer(Modifier.height(12.dp))
            
            DarkTextField(
                value = email, 
                onValueChange = onEmailChange, 
                placeholder = "Email (Required)", 
                leadingIcon = Icons.Default.Email
            )
            Spacer(Modifier.height(12.dp))
            
            // Phone is now just a regular optional field
            DarkTextField(
                value = phone, 
                onValueChange = onPhoneChange, 
                placeholder = "Phone Number (Optional)", 
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(Modifier.height(12.dp))
            
            DarkTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility
            )
            Spacer(Modifier.height(12.dp))
            DarkTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = onTogglePasswordVisibility
            )
            
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage, color = PrimaryRed, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Join ZyberAuto today", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        content()
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        leadingIcon = { Icon(leadingIcon, null, tint = Color.White.copy(alpha = 0.6f)) },
        trailingIcon = if (isPassword) {
             { IconButton(onClick = { onTogglePasswordVisibility?.invoke() }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.White.copy(0.6f)) } }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(0.05f),
            unfocusedContainerColor = Color.White.copy(0.05f),
            focusedBorderColor = PrimaryRed,
            unfocusedBorderColor = Color.White.copy(0.1f),
            cursorColor = PrimaryRed,
            disabledTextColor = Color.White.copy(0.3f),
            disabledContainerColor = Color.White.copy(0.02f),
            disabledBorderColor = Color.White.copy(0.05f)
        ),
        keyboardOptions = keyboardOptions,
        enabled = enabled
    )
}
