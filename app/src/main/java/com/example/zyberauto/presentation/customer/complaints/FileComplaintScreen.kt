package com.example.zyberauto.presentation.customer.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileComplaintScreen(
    onNavigateBack: () -> Unit,
    viewModel: FileComplaintViewModel = hiltViewModel()
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is FileComplaintUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File a Complaint") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("We're sorry you had a bad experience. Please tell us what happened.", style = MaterialTheme.typography.bodyMedium)
            
            AppTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject"
            )
            
            AppTextField(
                value = message,
                onValueChange = { message = it },
                label = "Message (Min 20 chars)",
                modifier = Modifier.height(150.dp),
                singleLine = false,
                minLines = 5
            )
            
            Text(
                text = "${message.length} / 20 characters minimum",
                style = MaterialTheme.typography.bodySmall,
                color = if (message.length < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(androidx.compose.ui.Alignment.End)
            )

            if (uiState is FileComplaintUiState.Error) {
                Text(
                    text = (uiState as FileComplaintUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            AppButton(
                text = "SUBMIT COMPLAINT",
                onClick = { viewModel.submitComplaint(subject, message) },
                isLoading = uiState is FileComplaintUiState.Loading,
                enabled = true
            )
        }
    }
}
