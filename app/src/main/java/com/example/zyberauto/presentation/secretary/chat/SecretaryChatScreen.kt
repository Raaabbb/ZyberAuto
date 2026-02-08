package com.example.zyberauto.presentation.secretary.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.presentation.customer.chat.ChatViewModel
import com.example.zyberauto.presentation.customer.chat.MessageBubble
import com.example.zyberauto.ui.theme.PrimaryRed

/**
 * SecretaryChatScreen - Staff's real-time chat interface with image support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    var messageText by remember { mutableStateOf("") }
    
    val backgroundColor = Color(0xFFF5F5F7)
    val currentUserId = uiState.currentUserId

    // Image picker - DISABLED per user request
    // val imagePickerLauncher = rememberLauncherForActivityResult(
    //     contract = ActivityResultContracts.GetContent()
    // ) { uri: Uri? ->
    //     uri?.let { viewModel.sendImageMessage(it) }
    // }

    // Auto-scroll to bottom
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            SecretaryChatTopBar(
                conversation = uiState.conversation,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Column {
                SecretaryChatInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    onImageClick = {},
                    isSending = uiState.isSending,
                    isUploadingImage = uiState.isUploadingImage
                )
                // Spacer for bottom navigation
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryRed
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == currentUserId
                        )
                    }
                }
            }
            
            // Image upload progress overlay
            if (uiState.isUploadingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = PrimaryRed)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Uploading image...")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecretaryChatTopBar(
    conversation: ChatConversation?,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = conversation?.customerName ?: "Chat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = conversation?.subject ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            // Status indicator
            val statusColor = if (conversation?.status == ChatConversation.STATUS_ACTIVE) {
                Color(0xFF4CAF50)
            } else {
                Color.Gray
            }
            Text(
                text = conversation?.status ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryRed
        )
    )
}

@Composable
private fun SecretaryChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImageClick: () -> Unit,
    isSending: Boolean,
    isUploadingImage: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image button - DISABLED per user request
            // IconButton(
            //     onClick = onImageClick,
            //     enabled = !isUploadingImage && !isSending
            // ) {
            //     Icon(
            //         Icons.Default.Image,
            //         contentDescription = "Send Image",
            //         tint = if (isUploadingImage) Color.LightGray else PrimaryRed
            //     )
            // }
            
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = { Text("Reply to customer...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryRed,
                    unfocusedBorderColor = Color.LightGray
                ),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank() && !isSending && !isUploadingImage,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (messageText.isNotBlank()) PrimaryRed else Color.LightGray,
                        CircleShape
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
