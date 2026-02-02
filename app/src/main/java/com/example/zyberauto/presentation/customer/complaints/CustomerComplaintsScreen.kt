package com.example.zyberauto.presentation.customer.complaints

import java.text.SimpleDateFormat
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
import com.example.zyberauto.domain.model.Complaint
import com.example.zyberauto.presentation.common.components.StatusBadge
import com.example.zyberauto.presentation.common.components.StatusType

@Composable
fun CustomerComplaintsScreen(
    onNavigateToFileComplaint: () -> Unit,
    viewModel: CustomerComplaintsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToFileComplaint) {
                Icon(Icons.Default.Add, contentDescription = "File Complaint")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CustomerComplaintsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CustomerComplaintsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CustomerComplaintsUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No complaints filed yet.")
                        Spacer(Modifier.height(8.dp))
                        Text("Tap + to file a new complaint.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is CustomerComplaintsUiState.Success -> {
                    ComplaintList(complaints = state.complaints)
                }
            }
        }
    }
}

@Composable
fun ComplaintList(complaints: List<Complaint>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(complaints) { complaint ->
            ComplaintCard(complaint)
        }
    }
}

@Composable
fun ComplaintCard(complaint: Complaint) {
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
                    text = complaint.subject,
                    style = MaterialTheme.typography.titleMedium
                )
                
                val statusType = when(complaint.status.uppercase()) {
                    "NEW" -> StatusType.New
                    "REPLIED" -> StatusType.Replied
                    "CLOSED" -> StatusType.Closed
                    else -> StatusType.New // Default
                }
                
                StatusBadge(status = statusType)
            }
            
            Text(
                text = dateFormat.format(complaint.dateSubmitted),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = complaint.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            
            if (complaint.reply != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Reply from Staff:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = complaint.reply,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
