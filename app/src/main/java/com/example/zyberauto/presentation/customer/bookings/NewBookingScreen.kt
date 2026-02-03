package com.example.zyberauto.presentation.customer.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.presentation.common.components.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewBookingViewModel = hiltViewModel()
) {
    var vehicleModel by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle Success Navigation
    LaunchedEffect(uiState) {
        if (uiState is NewBookingUiState.Success) {
            onNavigateBack() // Go back to list on success
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Booking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vehicle Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        label = "Vehicle Model (e.g. Toyota Vios)"
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = "Plate Number"
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    DropdownField(
                        value = serviceType,
                        onValueChange = { serviceType = it },
                        options = com.example.zyberauto.common.AppConstants.ServiceTypes,
                        label = "Service Type"
                    )
                    Spacer(Modifier.height(8.dp))
                    DropdownField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        options = com.example.zyberauto.common.AppConstants.TimeSlots,
                        label = "Preferred Time"
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes / Issues",
                        modifier = Modifier.height(100.dp),
                        singleLine = false
                    )
                }
            }
            
            if (uiState is NewBookingUiState.Error) {
                Text(
                    text = (uiState as NewBookingUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.weight(1f))
            
            AppButton(
                text = "SUBMIT BOOKING",
                onClick = {
                    viewModel.submitBooking(vehicleModel, plateNumber, serviceType, notes, timeSlot)
                },
                isLoading = uiState is NewBookingUiState.Loading
            )
        }
    }
}
