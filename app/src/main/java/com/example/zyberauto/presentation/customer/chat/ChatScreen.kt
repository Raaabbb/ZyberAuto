package com.example.zyberauto.presentation.customer.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatScreen - Real-time chat interface with image support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
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

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            ChatTopBar(
                title = uiState.conversation?.subject ?: "Chat",
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            ChatInputBar(
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
            } else if (uiState.messages.isEmpty()) {
                // Empty state - no messages yet
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start the conversation",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Type a message below to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
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
private fun ChatTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "ZyberAuto Support",
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrimaryRed
        )
    )
}

@Composable
private fun ChatInputBar(
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
                placeholder = { Text("Type a message...") },
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

/**
 * Message bubble with image support.
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    val bubbleColor = if (isFromCurrentUser) PrimaryRed else Color.White
    val textColor = if (isFromCurrentUser) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isFromCurrentUser) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Sender name (for staff messages)
        if (!isFromCurrentUser && message.senderName.isNotEmpty()) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(if (message.imageUrl != null) 4.dp else 12.dp)) {
                // Image if present
                if (!message.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Image",
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Text message if present
                if (message.message.isNotEmpty()) {
                    if (message.imageUrl != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = if (message.imageUrl != null) Modifier.padding(horizontal = 8.dp) else Modifier
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = if (message.imageUrl != null) 8.dp else 0.dp)
                ) {
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor,
                        fontSize = 10.sp
                    )
                    
                    // Status indicator (only for sender's messages)
                    if (isFromCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.status, tint = subtextColor)
                    }
                }
            }
        }
    }
}

/**
 * Message status icon.
 */
@Composable
fun MessageStatusIcon(status: String, tint: Color) {
    val seenColor = Color(0xFF4FC3F7)
    
    when (status) {
        ChatMessage.STATUS_SENDING -> {
            Icon(
                Icons.Default.Schedule,
                contentDescription = "Sending",
                modifier = Modifier.size(14.dp),
                tint = tint
            )
        }
        ChatMessage.STATUS_DELIVERED -> {
            Icon(
                Icons.Default.Check,
                contentDescription = "Delivered",
                modifier = Modifier.size(14.dp),
                tint = tint
            )
        }
        ChatMessage.STATUS_SEEN -> {
            Icon(
                Icons.Default.DoneAll,
                contentDescription = "Seen",
                modifier = Modifier.size(14.dp),
                tint = seenColor
            )
        }
    }
}
