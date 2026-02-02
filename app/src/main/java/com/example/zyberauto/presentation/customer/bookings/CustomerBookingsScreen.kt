package com.example.zyberauto.presentation.customer.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.secretary.StatusBadge
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CustomerBookingsScreen(
    onNavigateToNewBooking: () -> Unit,
    viewModel: CustomerBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewBooking,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Booking")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("My Bookings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is CustomerBookingsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingSpinner()
                    }
                }
                is CustomerBookingsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is CustomerBookingsUiState.Success -> {
                    if (state.bookings.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No bookings yet.", style = MaterialTheme.typography.bodyLarge)
                                Text("Tap + to create one.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.bookings) { appointment ->
                                CustomerBookingCard(appointment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBookingCard(appointment: Appointment) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceType,
                    style = MaterialTheme.typography.titleMedium
                )
                StatusBadge(status = appointment.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${dateFormat.format(appointment.dateScheduled)} • ${appointment.timeSlot}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${appointment.vehicleModel} (${appointment.plateNumber})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
