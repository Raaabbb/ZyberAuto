package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
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
    onNavigateToWalkIn: () -> Unit = {},
    onMessageCustomer: (customerId: String, customerName: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableMechanics by viewModel.availableMechanics.collectAsStateWithLifecycle()
    val randomMechanicSuggestion by viewModel.randomMechanicSuggestion.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf("All") }
    // Added "Queue Order" filter for first-come-first-served sorting
    val filters = listOf("All", "Queue Order", "Pending", "Accepted", "Completed", "Declined")
    var showRejectDialog by remember { mutableStateOf<Appointment?>(null) }
    var showDetailsDialog by remember { mutableStateOf<Appointment?>(null) }
    var showAcceptDialog by remember { mutableStateOf<Appointment?>(null) }
    
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
                val filteredList = when (selectedFilter) {
                    "All" -> state.appointments
                    "Queue Order" -> {
                        // First-come-first-served: sort by createdAt (oldest first), only PENDING
                        state.appointments
                            .filter { it.status == "PENDING" }
                            .sortedBy { it.createdAt }
                    }
                    else -> {
                        val statusKey = when (selectedFilter) {
                            "Pending" -> "PENDING"
                            "Accepted" -> "ACCEPTED"
                            "Completed" -> "COMPLETED"
                            "Declined" -> "DECLINED"
                            else -> selectedFilter.uppercase()
                        }
                        state.appointments.filter { it.status == statusKey }
                    }
                }

                if (filteredList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No appointments found.")
                            if (selectedFilter == "Queue Order") {
                                Text(
                                    "No pending requests in queue.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    // Show queue position indicator for Queue Order filter
                    if (selectedFilter == "Queue Order") {
                        Text(
                            "📋 Showing ${filteredList.size} pending request(s) in queue order (first come, first served)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                showQueuePosition = selectedFilter == "Queue Order",
                                queuePosition = if (selectedFilter == "Queue Order") {
                                    filteredList.indexOf(appointment) + 1
                                } else null,
                                onUpdateStatus = { id, status ->
                                    viewModel.updateStatus(id, status)
                                },
                                onRejectClick = { showRejectDialog = it },
                                onViewDetails = { showDetailsDialog = it },
                                onAcceptClick = { 
                                    // Load available mechanics before showing dialog
                                    viewModel.loadAvailableMechanicsForAppointment(it)
                                    showAcceptDialog = it 
                                },
                                onMessageClick = { appt ->
                                    onMessageCustomer(appt.userId, appt.customerName)
                                }
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

    // Accept with Mechanic Selection Dialog
    showAcceptDialog?.let { appointment ->
        AcceptWithMechanicDialog(
            appointment = appointment,
            availableMechanics = availableMechanics,
            randomSuggestion = randomMechanicSuggestion,
            onRandomSelect = { viewModel.selectRandomMechanic() },
            onDismiss = { showAcceptDialog = null },
            onConfirm = { mechanicNumber ->
                viewModel.acceptAppointment(appointment.id, mechanicNumber)
                showAcceptDialog = null
            }
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
fun AcceptWithMechanicDialog(
    appointment: Appointment,
    availableMechanics: List<Int>,
    randomSuggestion: Int?,
    onRandomSelect: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    // Default to first available mechanic or random suggestion
    var selectedMechanic by remember(randomSuggestion, availableMechanics) { 
        mutableStateOf(randomSuggestion ?: availableMechanics.firstOrNull() ?: 1) 
    }
    
    // Update selection when random suggestion changes
    LaunchedEffect(randomSuggestion) {
        randomSuggestion?.let { selectedMechanic = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Accept & Assign Mechanic") },
        text = {
            Column {
                Text("Assign a mechanic for ${appointment.customerName}'s appointment:")
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show warning if slot is getting full
                if (availableMechanics.size == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Only 1 mechanic available for this time slot!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA000)
                        )
                    }
                } else if (availableMechanics.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "All mechanics busy! Consider rescheduling.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Random Select Button
                OutlinedButton(
                    onClick = onRandomSelect,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = availableMechanics.isNotEmpty()
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Random Select (No Bias)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mechanic options - only show available ones
                val mechanicsToShow = if (availableMechanics.isNotEmpty()) availableMechanics else listOf(1, 2, 3)
                mechanicsToShow.forEach { mechanic ->
                    val isAvailable = availableMechanics.contains(mechanic) || availableMechanics.isEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isAvailable) { selectedMechanic = mechanic }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMechanic == mechanic,
                            onClick = { selectedMechanic = mechanic },
                            enabled = isAvailable
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mechanic $mechanic",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isAvailable) Color.Unspecified else Color.Gray
                            )
                            if (!isAvailable) {
                                Text(
                                    text = "Already assigned for this slot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            if (randomSuggestion == mechanic) {
                                Text(
                                    text = "🎲 Randomly selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMechanic) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = availableMechanics.isEmpty() || availableMechanics.contains(selectedMechanic)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Accept")
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
                // Show time range if end time is available
                val timeDisplay = if (appointment.timeSlotEnd.isNotBlank()) {
                    "${appointment.timeSlot} - ${appointment.timeSlotEnd}"
                } else {
                    appointment.timeSlot
                }
                Text("Scheduled: ${scheduledFormat.format(appointment.dateScheduled)} • $timeDisplay")
                // Show estimated duration if available
                if (appointment.estimatedDurationMinutes > 0) {
                    Text("Est. Duration: ${com.example.zyberauto.common.AppConstants.formatDuration(appointment.estimatedDurationMinutes)}")
                }
                Text("Vehicle: ${appointment.vehicleModel} (${appointment.plateNumber})")
                Text("Service: ${appointment.serviceType}")
                if (appointment.assignedMechanic > 0) {
                    Text("Assigned to: Mechanic ${appointment.assignedMechanic}")
                }
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
    showQueuePosition: Boolean = false,
    queuePosition: Int? = null,
    onUpdateStatus: (String, String) -> Unit,
    onRejectClick: (Appointment) -> Unit,
    onViewDetails: (Appointment) -> Unit,
    onAcceptClick: (Appointment) -> Unit,
    onMessageClick: (Appointment) -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val createdFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Show queue position badge if in queue order mode
                    if (showQueuePosition && queuePosition != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$queuePosition",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = appointment.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = appointment.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                // Show time range if end time is available
                val timeDisplay = if (appointment.timeSlotEnd.isNotBlank()) {
                    "${appointment.timeSlot} - ${appointment.timeSlotEnd}"
                } else {
                    appointment.timeSlot
                }
                Text(
                    text = "${dateFormat.format(appointment.dateScheduled)} • $timeDisplay",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Show when the request was created (for queue order context)
            if (showQueuePosition) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Submitted: ${createdFormat.format(appointment.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text("Vehicle: ${appointment.vehicleModel} (${appointment.plateNumber})", style = MaterialTheme.typography.bodyMedium)
            Text("Service: ${appointment.serviceType}", style = MaterialTheme.typography.bodyMedium)

            // Show estimated cost if available
            if (appointment.estimatedTotalCost > 0) {
                val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "PH"))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Est. Cost: ${currencyFormat.format(appointment.estimatedTotalCost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1E40AF),
                    fontWeight = FontWeight.Medium
                )
            }

            // Show insufficient items warning
            if (appointment.hasInsufficientItems && appointment.insufficientItemsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFD97706)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "INSUFFICIENT ITEMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Bold
                        )
                        appointment.insufficientItemsList.forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // Show assigned mechanic for accepted/completed appointments
            if (appointment.assignedMechanic > 0 && (appointment.status == "ACCEPTED" || appointment.status == "COMPLETED")) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assigned to: Mechanic ${appointment.assignedMechanic}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
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
                        onClick = { onAcceptClick(appointment) },
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
            
            // Message Now button - only show if appointment has userId (registered customer)
            if (appointment.userId.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onMessageClick(appointment) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Message Now")
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
