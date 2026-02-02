package com.example.zyberauto.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DebugSeederScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: DebugSeederViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loginEvent by viewModel.loginEvent.collectAsStateWithLifecycle()

    LaunchedEffect(loginEvent) {
        if (loginEvent) {
            viewModel.resetLoginEvent()
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Debug System Seeder") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
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
            Text(
                "Warning: This will inject test data into the Firestore database. Do not run in production.",
                color = MaterialTheme.colorScheme.error
            )
            
            Button(
                onClick = { viewModel.seedSystem() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("SEED SYSTEM & RESET")
                }
            }

            Text("Test User Login (Bypass Auth):", style = MaterialTheme.typography.titleMedium)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.loginAs("user_oreki_seed") }, modifier = Modifier.weight(1f)) {
                    Text("Oreki (Today)")
                }
                Button(onClick = { viewModel.loginAs("user_bored_seed") }, modifier = Modifier.weight(1f)) {
                    Text("Bored (Pending)")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.loginAs("user_sacred_seed") }, modifier = Modifier.weight(1f)) {
                    Text("Sacred (Declined)")
                }
                Button(onClick = { viewModel.loginAs("user_t788_seed") }, modifier = Modifier.weight(1f)) {
                    Text("T-800 (Done)")
                }
            }

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
                ) {
                    Text("Log Output:", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.log,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}
