package com.example.zyberauto.presentation.secretary.inquiries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.common.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiriesScreen(
    viewModel: InquiriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedComplaint by remember { mutableStateOf<Complaint?>(null) }
    var showReplyDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Customer Inquiries",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "New", "Replied").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is InquiriesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingSpinner()
                    }
                }
                is InquiriesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is InquiriesUiState.Success -> {
                    val filteredList = state.complaints.filter {
                        when (selectedFilter) {
                            "All" -> true
                            "New" -> it.status == "NEW"
                            "Replied" -> it.status == "REPLIED"
                            else -> true
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No inquiries found.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredList) { complaint ->
                                InquiryCard(complaint = complaint) {
                                    selectedComplaint = complaint
                                    showReplyDialog = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog && selectedComplaint != null) {
        ReplyDialog(
            complaint = selectedComplaint!!,
            onDismiss = { showReplyDialog = false },
            onSubmit = { reply ->
                viewModel.submitReply(selectedComplaint!!.id, reply)
                showReplyDialog = false
            }
        )
    }
}

@Composable
fun InquiryCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = complaint.customerName.ifBlank { "Unknown User" },
                    style = MaterialTheme.typography.titleMedium
                )
                val statusType = when (complaint.status) {
                    "NEW" -> com.example.zyberauto.presentation.common.components.StatusType.New
                    "REPLIED" -> com.example.zyberauto.presentation.common.components.StatusType.Replied
                    else -> com.example.zyberauto.presentation.common.components.StatusType.Closed
                }
                StatusBadge(status = statusType)
            }

            Text(
                text = complaint.subject,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = complaint.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Text(
                text = dateFormat.format(complaint.dateSubmitted),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReplyDialog(
    complaint: Complaint,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var replyText by remember { mutableStateOf(complaint.reply ?: "") }
    val isReadOnly = complaint.status == "REPLIED"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = complaint.subject) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("From: ${complaint.customerName}", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(complaint.message, style = MaterialTheme.typography.bodyMedium)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

                if (isReadOnly) {
                    Column {
                        Text("Reply:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(replyText, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Enter Reply") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            if (!isReadOnly) {
                Button(
                    onClick = { onSubmit(replyText) },
                    enabled = replyText.isNotBlank()
                ) {
                    Text("Send Reply")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (!isReadOnly) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
