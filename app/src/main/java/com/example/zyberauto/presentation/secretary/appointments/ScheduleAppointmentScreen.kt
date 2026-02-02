package com.example.zyberauto.presentation.secretary.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.presentation.common.components.DatePickerField
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleAppointmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableVehicles by viewModel.customerVehicles.collectAsStateWithLifecycle()

    var selectedCustomerName by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var selectedDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var timeSlot by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    val serviceTypes = com.example.zyberauto.common.AppConstants.ServiceTypes
    val timeSlots = com.example.zyberauto.common.AppConstants.TimeSlots

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                onNavigateBack()
            },
            title = { Text("Success") },
            text = { Text("Appointment scheduled successfully.") },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = false 
                    onNavigateBack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            when (val state = uiState) {
                is ScheduleAppointmentUiState.Loading -> {
                     LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ScheduleAppointmentUiState.Error -> {
                    Text("Error loading customers: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is ScheduleAppointmentUiState.Success -> {
                    Text("Customer Details", style = MaterialTheme.typography.titleMedium)
                    
                    val customerNames = state.customers.map { it.name }
                    com.example.zyberauto.presentation.common.components.DropdownField(
                        value = selectedCustomerName,
                        onValueChange = { 
                            selectedCustomerName = it
                            viewModel.onCustomerSelected(it)
                            selectedVehicle = "" // Reset vehicle when customer changes
                        },
                        options = customerNames,
                        label = "Select Customer",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vehicle Dropdown (dependent on customer)
                    // If no vehicles found (new customer car?), maybe allow text entry? 
                    // For 9.1 we assume selecting existing vehicle. 
                    // If distinct vehicles logic returned empty, maybe show a text field or allow adding?
                    // For now, strict dropdown from history + "Other/New" option?
                    // Let's stick to dropdown.
                    
                    val vehicleOptions = if (availableVehicles.isNotEmpty()) availableVehicles else listOf("No historical vehicles found")
                    
                    com.example.zyberauto.presentation.common.components.DropdownField(
                        value = selectedVehicle,
                        onValueChange = { selectedVehicle = it },
                        options = vehicleOptions,
                        label = "Select Vehicle",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCustomerName.isNotEmpty()
                    )

                    HorizontalDivider()

                    Text("Service Details", style = MaterialTheme.typography.titleMedium)

                    com.example.zyberauto.presentation.common.components.DropdownField(
                        value = selectedService,
                        onValueChange = { selectedService = it },
                        options = serviceTypes,
                        label = "Service Type",
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        value = dateString,
                        onDateSelected = { timestamp ->
                             selectedDateTimestamp = timestamp
                             dateString = dateFormat.format(Date(timestamp))
                        },
                        label = "Preferred Date",
                        modifier = Modifier.fillMaxWidth()
                    )

                    com.example.zyberauto.presentation.common.components.DropdownField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        options = timeSlots,
                        label = "Time Slot",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes (Optional)",
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        text = "SCHEDULE APPOINTMENT",
                        onClick = { 
                             if (selectedCustomerName.isBlank() || selectedVehicle.isBlank() || 
                                 selectedService.isBlank() || selectedDateTimestamp == null || timeSlot.isBlank()) {
                                 errorMessage = "Please fill in all required fields"
                             } else {
                                 isSubmitting = true
                                 viewModel.submitAppointment(
                                     serviceType = selectedService,
                                     dateScheduled = selectedDateTimestamp!!,
                                     timeSlot = timeSlot,
                                     notes = notes,
                                     vehicleInfo = selectedVehicle,
                                     onSuccess = {
                                         isSubmitting = false
                                         showSuccessDialog = true
                                     },
                                     onError = { msg ->
                                         isSubmitting = false
                                         errorMessage = msg
                                     }
                                 )
                             }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = isSubmitting
                    )
                }
            }
        }
    }
}
