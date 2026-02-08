package com.example.zyberauto.presentation.customer.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.ChatConversation
import com.example.zyberauto.domain.model.ChatMessage
import com.example.zyberauto.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * ConversationsScreen - Shows list of all customer's chat conversations.
 * 
 * @param onNavigateToChat Navigate to a specific conversation
 * @param onNavigateToNewChat Navigate to create new conversation
 */
@Composable
fun ConversationsScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = uiState) {
            is ConversationsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryRed
                )
            }
            is ConversationsUiState.Error -> {
                Text(
                    text = state.message,
                    color = PrimaryRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ConversationsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 100.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        ConversationsHeader(
                            count = state.conversations.size,
                            textColor = textColor
                        )
                    }
                    
                    if (state.conversations.isEmpty()) {
                        item {
                            EmptyConversationsCard(subtextColor = subtextColor)
                        }
                    } else {
                        items(state.conversations) { conversation ->
                            ConversationCard(
                                conversation = conversation,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                onClick = { onNavigateToChat(conversation.id) }
                            )
                        }
                    }
                }
            }
        }
        
        // FAB to create new conversation
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(PrimaryRed.copy(alpha = 0.4f), CircleShape)
                        .blur(16.dp)
                )
                IconButton(
                    onClick = onNavigateToNewChat,
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryRed, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationsHeader(count: Int, textColor: Color) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            "SUPPORT CENTER",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (count > 0) "Your Conversations" else "No Conversations Yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        Text(
            text = if (count > 0) "$count active chat${if (count > 1) "s" else ""}" else "Start a chat with us",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyConversationsCard(subtextColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = subtextColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tap the + button to start a conversation",
                style = MaterialTheme.typography.bodyMedium,
                color = subtextColor
            )
        }
    }
}

@Composable
fun ConversationCard(
    conversation: ChatConversation,
    textColor: Color,
    subtextColor: Color,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    
    val timeText = remember(conversation.lastMessageTime) {
        val now = System.currentTimeMillis()
        val diff = now - conversation.lastMessageTime
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        if (diff < oneDayMs) {
            timeFormat.format(Date(conversation.lastMessageTime))
        } else {
            dateFormat.format(Date(conversation.lastMessageTime))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Chat,
                    null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Conversation details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.subject,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Show who sent last message
                    if (conversation.lastSenderType == ChatMessage.SENDER_CUSTOMER) {
                        Text(
                            "You: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Status badge
                if (conversation.status == ChatConversation.STATUS_INACTIVE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "INACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Arrow indicator
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = subtextColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
