package com.example.zyberauto.presentation.secretary.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Appointment
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.FilterChips
import com.example.zyberauto.presentation.common.components.LoadingSpinner
import com.example.zyberauto.presentation.common.components.SearchBar
import com.example.zyberauto.presentation.common.components.StatusBadge
import com.example.zyberauto.presentation.common.components.StatusType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingRequestsScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: BookingRequestsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showRejectDialog by remember { mutableStateOf<Appointment?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp) // Removed top padding to reduce header space
        ) {
            // Header removed

            
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FilterChips(
                options = listOf("All", "Pending", "Approved", "Rejected"),
                selectedOption = selectedFilter,
                onOptionSelected = { selectedFilter = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is BookingRequestsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        LoadingSpinner()
                    }
                }
                is BookingRequestsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is BookingRequestsUiState.Success -> {
                    // Filter Logic
                    val filteredList = state.bookings.filter { appointment ->
                        val matchesSearch = appointment.customerName.contains(searchQuery, ignoreCase = true) ||
                                            appointment.plateNumber.contains(searchQuery, ignoreCase = true) ||
                                            appointment.vehicleModel.contains(searchQuery, ignoreCase = true)
                        
                        val matchesFilter = when (selectedFilter) {
                            "All" -> true
                            "Pending" -> appointment.status == "PENDING"
                            "Approved" -> appointment.status == "ACCEPTED" || appointment.status == "IN_PROGRESS" || appointment.status == "COMPLETED"
                            "Rejected" -> appointment.status == "REJECTED"
                            else -> true
                        }
                        
                        matchesSearch && matchesFilter
                    }

                    if (filteredList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No requests found.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredList) { appointment ->
                                BookingRequestCard(
                                    appointment = appointment,
                                    onAccept = { viewModel.acceptBooking(appointment) },
                                    onReject = { showRejectDialog = appointment },
                                    onClick = { onNavigateToDetails(appointment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (showRejectDialog != null) {
            RejectBookingDialog(
                onDismiss = { showRejectDialog = null },
                onConfirm = { reason ->
                    viewModel.rejectBooking(showRejectDialog!!, reason)
                    showRejectDialog = null
                }
            )
        }
    }
}

@Composable
fun BookingRequestCard(
    appointment: Appointment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.customerName,
                    style = MaterialTheme.typography.titleMedium
                )
                // Map string status to StatusType enum if possible, or fallback
                val statusType = when(appointment.status) {
                    "PENDING" -> StatusType.Pending
                    "ACCEPTED", "IN_PROGRESS" -> StatusType.Accepted
                    "REJECTED" -> StatusType.Rejected
                    "COMPLETED" -> StatusType.Completed
                    else -> StatusType.Pending
                }
                StatusBadge(status = statusType)
            }
            
            Text("${appointment.vehicleModel} - ${appointment.plateNumber}")
            Text("Service: ${appointment.serviceType}")
            Text("Schedule: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(appointment.dateScheduled)} @ ${appointment.timeSlot}")
            
            if (appointment.status == "PENDING") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                   horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppButton(
                        text = "Accept",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    )
                    AppButton(
                        text = "Decline",
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
