package com.example.zyberauto.presentation.customer.inquiries

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Inquiry
import com.example.zyberauto.presentation.common.components.StatusBadge
import com.example.zyberauto.presentation.common.components.StatusType

@Composable
fun CustomerInquiriesScreen(
    onNavigateToFileInquiry: () -> Unit,
    viewModel: CustomerInquiriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToFileInquiry) {
                Icon(Icons.Default.Add, contentDescription = "File Inquiry")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CustomerInquiriesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CustomerInquiriesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CustomerInquiriesUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No inquiries filed yet.")
                        Spacer(Modifier.height(8.dp))
                        Text("Tap + to file a new inquiry.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is CustomerInquiriesUiState.Success -> {
                    InquiryList(inquiries = state.inquiries)
                }
            }
        }
    }
}

@Composable
fun InquiryList(inquiries: List<Inquiry>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(inquiries) { inquiry ->
            InquiryCard(inquiry)
        }
    }
}

@Composable
fun InquiryCard(inquiry: Inquiry) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inquiry.subject,
                    style = MaterialTheme.typography.titleMedium
                )
                
                val statusType = when(inquiry.status.uppercase()) {
                    "NEW" -> StatusType.New
                    "REPLIED" -> StatusType.Replied
                    "CLOSED" -> StatusType.Closed
                    else -> StatusType.New // Default
                }
                
                StatusBadge(status = statusType)
            }
            
            Text(
                text = dateFormat.format(Date(inquiry.dateSubmitted)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = inquiry.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            if (inquiry.reply != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Reply from Staff:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = inquiry.reply,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
