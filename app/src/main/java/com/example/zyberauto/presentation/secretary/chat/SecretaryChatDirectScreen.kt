package com.example.zyberauto.presentation.secretary.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * SecretaryChatDirectScreen - Handles finding or creating a conversation with a customer
 * then navigating to the actual chat screen.
 */
@Composable
fun SecretaryChatDirectScreen(
    customerId: String,
    customerName: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: SecretaryChatDirectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(customerId, customerName) {
        viewModel.findOrCreateConversation(customerId, customerName)
    }
    
    LaunchedEffect(uiState.conversationId) {
        if (uiState.conversationId.isNotEmpty()) {
            onNavigateToChat(uiState.conversationId)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Starting conversation with $customerName...")
                }
            }
            uiState.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}