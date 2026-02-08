package com.example.zyberauto.presentation.customer.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * CustomerBookingsScreen - Displays list of customer's bookings.
 * 
 * UI Features:
 * - Light gray background matching unified design
 * - Section header with red label
 * - White cards for each booking
 * - Floating red FAB with glow effect
 * 
 * @param onNavigateToNewBooking Callback to navigate to new booking form
 * @param viewModel ViewModel providing booking data
 */
@Composable
fun CustomerBookingsScreen(
    onNavigateToNewBooking: () -> Unit,
    viewModel: CustomerBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rescheduleState by viewModel.rescheduleState.collectAsStateWithLifecycle()
    val isSlotAvailable by viewModel.isSlotAvailable.collectAsStateWithLifecycle()
    
    // Reschedule dialog state
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }
    
    // Unified color scheme
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)
    
    // Handle reschedule success
    LaunchedEffect(rescheduleState) {
        if (rescheduleState is RescheduleState.Success) {
            showRescheduleDialog = false
            appointmentToReschedule = null
            viewModel.resetRescheduleState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = uiState) {
            is CustomerBookingsUiState.Loading -> {
                // Centered loading spinner
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingSpinner()
                }
            }
            is CustomerBookingsUiState.Error -> {
                // Error state with red text
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = PrimaryRed)
                }
            }
            is CustomerBookingsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 100.dp, // Space for bottom nav
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Header
                    item {
                        BookingsSectionHeader(
                            bookingCount = state.bookings.size,
                            textColor = textColor
                        )
                    }

                    // Booking Cards or Empty State
                    if (state.bookings.isEmpty()) {
                        item {
                            EmptyBookingsCard(subtextColor = subtextColor)
                        }
                    } else {
                        items(state.bookings) { appointment ->
                            CustomerBookingCard(
                                appointment = appointment,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                onRescheduleClick = {
                                    appointmentToReschedule = appointment
                                    showRescheduleDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Reschedule Dialog
        if (showRescheduleDialog && appointmentToReschedule != null) {
            RescheduleDialog(
                appointment = appointmentToReschedule!!,
                rescheduleState = rescheduleState,
                isSlotAvailable = isSlotAvailable,
                onCheckSlotAvailability = { date, timeSlot ->
                    viewModel.checkSlotAvailability(date, timeSlot)
                },
                onReschedule = { newDate, newTimeSlot ->
                    viewModel.rescheduleAppointment(appointmentToReschedule!!, newDate, newTimeSlot)
                },
                onDismiss = {
                    showRescheduleDialog = false
                    appointmentToReschedule = null
                    viewModel.resetRescheduleState()
                }
            )
        }
        
        // Floating Action Button with red glow (matches secretary style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
        ) {
            RedGlowFab(onClick = onNavigateToNewBooking)
        }
    }
}

/**
 * Section header with red label and title.
 */
@Composable
private fun BookingsSectionHeader(
    bookingCount: Int,
    textColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        // Red uppercase label
        Text(
            "YOUR APPOINTMENTS",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Main title
        Text(
            text = if (bookingCount > 0) "My Bookings" else "No Bookings Yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        // Subtitle
        Text(
            text = if (bookingCount > 0) "$bookingCount scheduled appointments" else "Schedule your first service",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

/**
 * Empty state card when no bookings exist.
 */
@Composable
private fun EmptyBookingsCard(subtextColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = subtextColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tap the + button to create your first booking",
                style = MaterialTheme.typography.bodyMedium,
                color = subtextColor
            )
        }
    }
}

/**
 * Individual booking card with white background and rounded corners.
 * 
 * @param appointment The booking data to display
 * @param textColor Primary text color
 * @param subtextColor Secondary text color
 * @param onRescheduleClick Callback when reschedule button is clicked
 */
@Composable
fun CustomerBookingCard(
    appointment: Appointment,
    textColor: Color = Color(0xFF0F172A),
    subtextColor: Color = Color(0xFF64748B),
    onRescheduleClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Can reschedule if PENDING or ACCEPTED (not COMPLETED, DECLINED, or IN_PROGRESS)
    val canReschedule = appointment.status == "PENDING" || appointment.status == "ACCEPTED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: Service type + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceType,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
                BookingStatusBadge(status = appointment.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date and time row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = subtextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Show time range if end time is available
                val timeDisplay = if (appointment.timeSlotEnd.isNotBlank()) {
                    "${appointment.timeSlot} - ${appointment.timeSlotEnd}"
                } else {
                    appointment.timeSlot
                }
                Text(
                    text = "${dateFormat.format(appointment.dateScheduled)} • $timeDisplay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtextColor
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Vehicle info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = subtextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${appointment.vehicleModel} (${appointment.plateNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor
                )
            }
            
            // Show assigned mechanic for accepted/completed appointments
            if (appointment.assignedMechanic > 0 && (appointment.status == "ACCEPTED" || appointment.status == "COMPLETED" || appointment.status == "IN_PROGRESS")) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Assigned to: Mechanic ${appointment.assignedMechanic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Show rejection reason if declined
            if (appointment.status == "DECLINED" && appointment.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reason: ${appointment.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryRed
                )
            }
            
            // Reschedule button for PENDING or ACCEPTED appointments
            if (canReschedule) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRescheduleClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryRed.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reschedule")
                }
            }
        }
    }
}

/**
 * Status badge with color coding.
 */
@Composable
private fun BookingStatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PENDING" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "ACCEPTED", "CONFIRMED" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "IN_PROGRESS" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        "COMPLETED" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "DECLINED", "CANCELLED" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

/**
 * Floating Action Button with red glow effect.
 * Matches the secretary dashboard FAB style.
 */
@Composable
private fun RedGlowFab(onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        // Red glow effect behind button
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(PrimaryRed.copy(alpha = 0.4f), CircleShape)
                .blur(16.dp)
        )
        // Actual FAB button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(PrimaryRed, CircleShape)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "New Booking",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Dialog for rescheduling an appointment.
 * Includes date picker, time slot selection, and availability validation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescheduleDialog(
    appointment: Appointment,
    rescheduleState: RescheduleState,
    isSlotAvailable: Boolean,
    onCheckSlotAvailability: (java.util.Date, String) -> Unit,
    onReschedule: (newDate: String, newTimeSlot: String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val currentDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    var selectedDate by remember { mutableStateOf(dateFormat.format(appointment.dateScheduled)) }
    var selectedTimeSlot by remember { mutableStateOf(appointment.timeSlot) }
    var timeSlotExpanded by remember { mutableStateOf(false) }
    
    val timeSlots = AppConstants.TimeSlots
    
    // Check slot availability when date or time changes
    LaunchedEffect(selectedDate, selectedTimeSlot) {
        try {
            val parsedDate = dateFormat.parse(selectedDate)
            if (parsedDate != null) {
                onCheckSlotAvailability(parsedDate, selectedTimeSlot)
            }
        } catch (e: Exception) {
            // Invalid date format, ignore
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Reschedule Appointment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current appointment info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Current Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            "${currentDateFormat.format(appointment.dateScheduled)} • ${appointment.timeSlot}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            appointment.serviceType,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                HorizontalDivider()
                
                Text(
                    "New Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Date input
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date (MM/DD/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                )
                
                // Time slot dropdown
                ExposedDropdownMenuBox(
                    expanded = timeSlotExpanded,
                    onExpandedChange = { timeSlotExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTimeSlot,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time Slot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeSlotExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = timeSlotExpanded,
                        onDismissRequest = { timeSlotExpanded = false }
                    ) {
                        timeSlots.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text(slot) },
                                onClick = {
                                    selectedTimeSlot = slot
                                    timeSlotExpanded = false
                                }
                            )
                        }
                    }
                }
           vb
                // Slot availability indicator
                if (!isSlotAvailable) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⚠️ This time slot is fully booked. Please select a different time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }
                
                // Error message from reschedule attempt
                if (rescheduleState is RescheduleState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Text(
                            rescheduleState.message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onReschedule(selectedDate, selectedTimeSlot) },
                enabled = rescheduleState !is RescheduleState.Loading && isSlotAvailable,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
            ) {
                if (rescheduleState is RescheduleState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
