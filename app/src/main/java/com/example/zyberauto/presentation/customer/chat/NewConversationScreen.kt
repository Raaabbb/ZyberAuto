package com.example.zyberauto.presentation.customer.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.ui.theme.PrimaryRed

/**
 * NewConversationScreen - Form to start a new chat conversation.
 * 
 * @param onNavigateBack Go back to conversations list
 * @param onConversationCreated Navigate to the newly created chat
 */
@Composable
fun NewConversationScreen(
    onNavigateBack: () -> Unit,
    onConversationCreated: (String) -> Unit,
    viewModel: NewConversationViewModel = hiltViewModel()
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)

    // Navigate to chat on success
    LaunchedEffect(uiState) {
        if (uiState is NewConversationUiState.Success) {
            val conversationId = (uiState as NewConversationUiState.Success).conversationId
            viewModel.resetState()
            onConversationCreated(conversationId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back button
            Column {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                
                Text(
                    "NEW CONVERSATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Start a Chat",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                Text(
                    "We'll respond as soon as possible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            
            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Chat,
                            null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Message Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = textColor
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Subject field
                    AppTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = "Subject (e.g. Question about service)"
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Message field
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Your message") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    // Character count
                    Text(
                        text = "${message.length} characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.length < 10) PrimaryRed else subtextColor,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            
            // Error message
            if (uiState is NewConversationUiState.Error) {
                Text(
                    text = (uiState as NewConversationUiState.Error).message,
                    color = PrimaryRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.createConversation(subject, message)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                enabled = uiState !is NewConversationUiState.Loading
            ) {
                if (uiState is NewConversationUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "START CONVERSATION",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
