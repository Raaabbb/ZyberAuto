package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.secretary.appointments.AppointmentsUiState
import com.example.zyberauto.presentation.secretary.appointments.AppointmentsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SecretaryAppointmentsScreen(
    viewModel: AppointmentsViewModel = hiltViewModel(),
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToWalkIn: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Accepted", "Completed", "Declined")
    var showRejectDialog by remember { mutableStateOf<Appointment?>(null) }
    var showDetailsDialog by remember { mutableStateOf<Appointment?>(null) }
    
    var showAddMenu by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showAddMenu) {
                    FloatingActionButton(
                        onClick = { 
                            showAddMenu = false 
                            onNavigateToWalkIn()
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Walk-In")
                        }
                    }
                    
                    FloatingActionButton(
                         onClick = { 
                            showAddMenu = false 
                            onNavigateToSchedule()
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Icon(Icons.Default.Event, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Schedule")
                        }
                    }
                }
                
                FloatingActionButton(onClick = { showAddMenu = !showAddMenu }) {
                    Icon(if (showAddMenu) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Appointment")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            // Header removed as it is now part of the Tab/Shell structure


        // Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    elevation = FilterChipDefaults.filterChipElevation(
                        elevation = if (isSelected) 2.dp else 0.dp
                    )
                )
            }
        }

        when (val state = uiState) {
            is AppointmentsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingSpinner()
                }
            }
            is AppointmentsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is AppointmentsUiState.Success -> {
                val filteredList = if (selectedFilter == "All") {
                    state.appointments
                } else {
                    val statusKey = when (selectedFilter) {
                        "Pending" -> "PENDING"
                        "Accepted" -> "ACCEPTED"
                        "Completed" -> "COMPLETED"
                        "Declined" -> "DECLINED"
                        else -> selectedFilter.uppercase()
                    }
                    state.appointments.filter { it.status == statusKey }
                }

                if (filteredList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No appointments found.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onUpdateStatus = { id, status ->
                                    viewModel.updateStatus(id, status)
                                },
                                onRejectClick = { showRejectDialog = it },
                                onViewDetails = { showDetailsDialog = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Reject Dialog
    showRejectDialog?.let { appointment ->
        RejectBookingDialog(
            appointment = appointment,
            onDismiss = { showRejectDialog = null },
            onConfirm = { reason ->
                viewModel.rejectBooking(appointment.id, reason)
                showRejectDialog = null
            }
        )
    }

    // Details Dialog
    showDetailsDialog?.let { appointment ->
        AppointmentDetailsDialog(
            appointment = appointment,
            onDismiss = { showDetailsDialog = null }
        )
    }
}

@Composable
fun RejectBookingDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Decline Booking") },
        text = {
            Column {
                Text("Please provide a reason for declining the booking for ${appointment.customerName}:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Decline")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AppointmentDetailsDialog(
    appointment: Appointment,
    onDismiss: () -> Unit
) {
    val createdFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    val scheduledFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Appointment Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Customer: ${appointment.customerName}")
                Text("Status: ${appointment.status}")
                Text("Scheduled: ${scheduledFormat.format(appointment.dateScheduled)} • ${appointment.timeSlot}")
                Text("Vehicle: ${appointment.vehicleModel} (${appointment.plateNumber})")
                Text("Service: ${appointment.serviceType}")
                if (appointment.notes.isNotBlank()) {
                    Text("Notes: ${appointment.notes}")
                }
                if (appointment.price > 0) {
                    Text(String.format(Locale.getDefault(), "Price: ₱%.2f", appointment.price))
                }
                if (appointment.status == "DECLINED" && appointment.rejectionReason.isNotBlank()) {
                    Text("Rejection Reason: ${appointment.rejectionReason}")
                }
                Text("Booked: ${createdFormat.format(appointment.createdAt)}")
                if (appointment.userId.isNotBlank()) {
                    Text("User ID: ${appointment.userId}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onUpdateStatus: (String, String) -> Unit,
    onRejectClick: (Appointment) -> Unit,
    onViewDetails: (Appointment) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewDetails(appointment) },
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
                    text = appointment.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = appointment.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${dateFormat.format(appointment.dateScheduled)} • ${appointment.timeSlot}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vehicle: ${appointment.vehicleModel} (${appointment.plateNumber})", style = MaterialTheme.typography.bodyMedium)
            Text("Service: ${appointment.serviceType}", style = MaterialTheme.typography.bodyMedium)

            
            // Show rejection reason for declined appointments
            if (appointment.status == "DECLINED" && appointment.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reason: ${appointment.rejectionReason}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }
            
            if (appointment.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onRejectClick(appointment) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Decline")
                    }
                    Button(
                        onClick = { onUpdateStatus(appointment.id, "ACCEPTED") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Green
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
            
             if (appointment.status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onUpdateStatus(appointment.id, "COMPLETED") },
                    ) {
                        Text("Mark Complete")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "PENDING" -> Color(0xFFFFA000) // Amber
        "ACCEPTED" -> Color(0xFF4CAF50) // Green
        "DECLINED" -> Color.Red
        "REJECTED" -> Color.Red // Backwards compatibility
        "COMPLETED" -> Color.Blue
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
