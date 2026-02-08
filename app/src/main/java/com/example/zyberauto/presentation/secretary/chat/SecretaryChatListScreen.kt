package com.example.zyberauto.presentation.secretary.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * SecretaryChatListScreen - Shows all customer conversations for staff.
 * 
 * @param onNavigateBack Navigate back
 * @param onNavigateToChat Navigate to specific chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: SecretaryChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Customer Chats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SecretaryChatListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryRed
                    )
                }
                is SecretaryChatListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = PrimaryRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SecretaryChatListUiState.Success -> {
                    if (state.conversations.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = subtextColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No conversations yet",
                                color = subtextColor
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Unified List - All conversations sorted by time
                            items(state.conversations) { conversation ->
                                SecretaryConversationCard(
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
        }
    }
}

@Composable
private fun SecretaryConversationCard(
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
    
    // Highlight if last message is from customer (needs reply)
    val needsReply = conversation.lastSenderType == ChatMessage.SENDER_CUSTOMER

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
            // Avatar with customer initial
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (needsReply) PrimaryRed else subtextColor.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.customerName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (needsReply) Color.White else textColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                }
                
                Text(
                    text = conversation.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Needs reply indicator
                if (needsReply) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⬤ Needs reply",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = subtextColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
