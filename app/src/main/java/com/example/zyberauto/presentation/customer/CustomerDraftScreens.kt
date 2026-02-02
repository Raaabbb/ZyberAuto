package com.example.zyberauto.presentation.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp



@Composable
fun CustomerComplaintsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
             Text("My Complaints", style = MaterialTheme.typography.headlineMedium)
             androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
             Button(onClick = { /* TODO: File Complaint */ }) {
                 Text("File a Complaint")
             }
        }
    }
}

@Composable
fun CustomerSettingsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
    }
}
