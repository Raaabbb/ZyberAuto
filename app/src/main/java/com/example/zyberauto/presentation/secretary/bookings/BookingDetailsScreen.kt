package com.example.zyberauto.presentation.secretary.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.common.components.StatusBadge
import com.example.zyberauto.presentation.common.components.StatusType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BookingDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRejectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is BookingDetailsUiState.Loading -> {
                    LoadingSpinner(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
                is BookingDetailsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is BookingDetailsUiState.Success -> {
                    val booking = state.booking
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val statusType = when(booking.status) {
                                "PENDING" -> StatusType.Pending
                                "ACCEPTED", "IN_PROGRESS" -> StatusType.Accepted
                                "REJECTED" -> StatusType.Rejected
                                "COMPLETED" -> StatusType.Completed
                                else -> StatusType.Pending
                            }
                            StatusBadge(status = statusType)
                        }
                        
                        HorizontalDivider()
                        
                        // Customer Info
                        DetailSection(title = "Customer Information") {
                             DetailRow("Name", booking.customerName)
                             // Phone/Email not in booking model yet unless we join or add to model. 
                             // Using placeholder or what's available.
                             // DetailRow("Phone", booking.phoneNumber) 
                        }
                        
                        // Vehicle Info
                        DetailSection(title = "Vehicle Information") {
                            DetailRow("Model", booking.vehicleModel)
                            DetailRow("Plate Number", booking.plateNumber)
                        }
                        
                        // Service Info
                        DetailSection(title = "Service Details") {
                            DetailRow("Type", booking.serviceType)
                            DetailRow("Notes", booking.notes.ifBlank { "None" })
                        }
                        
                        // Schedule
                        DetailSection(title = "Schedule") {
                            DetailRow("Date", java.text.SimpleDateFormat("MMMM dd, yyyy").format(booking.dateScheduled))
                            DetailRow("Time", booking.timeSlot)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Actions
                        if (booking.status == "PENDING") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AppButton(
                                    text = "Accept Request",
                                    onClick = { viewModel.acceptBooking() },
                                    modifier = Modifier.weight(1f)
                                )
                                AppButton(
                                    text = "Decline",
                                    onClick = { showRejectDialog = true },
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    if (showRejectDialog) {
                        RejectBookingDialog(
                            onDismiss = { showRejectDialog = false },
                            onConfirm = { reason ->
                                viewModel.rejectBooking(reason)
                                showRejectDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        content()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
