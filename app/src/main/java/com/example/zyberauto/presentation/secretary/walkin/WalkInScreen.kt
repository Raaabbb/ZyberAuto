package com.example.zyberauto.presentation.secretary.walkin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zyberauto.presentation.common.components.AppButton
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.presentation.common.components.DatePickerField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkInScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalkInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableMechanics by viewModel.availableMechanics.collectAsStateWithLifecycle()
    val randomMechanicSuggestion by viewModel.randomMechanicSuggestion.collectAsStateWithLifecycle()
    val existingCustomers by viewModel.existingCustomers.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val customerVehicles by viewModel.customerVehicles.collectAsStateWithLifecycle()
    
    // Toggle for new vs existing customer
    var isNewCustomer by remember { mutableStateOf(true) }
    
    // Customer dropdown expanded state
    var customerDropdownExpanded by remember { mutableStateOf(false) }
    
    // Selected vehicle from existing customer
    var selectedVehicleIndex by remember { mutableStateOf(-1) }
    
    // Customer Details
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Vehicle Details
    var vehicleBrand by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Sedan") }
    
    // Auto-fill when existing customer is selected
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let { customer ->
            fullName = customer.name
            phoneNumber = customer.phoneNumber
            email = customer.email
            selectedVehicleIndex = -1 // Reset vehicle selection
        }
    }
    
    // Auto-fill when vehicle is selected from customer's vehicles
    LaunchedEffect(selectedVehicleIndex, customerVehicles) {
        if (selectedVehicleIndex >= 0 && selectedVehicleIndex < customerVehicles.size) {
            val vehicle = customerVehicles[selectedVehicleIndex]
            // Vehicle.model contains full model name (e.g., "Toyota Vios")
            // For existing vehicles, put the full model in vehicleModel and leave brand empty
            vehicleBrand = "" // Existing vehicles don't have separate brand
            vehicleModel = vehicle.model
            plateNumber = vehicle.plateNumber
            vehicleType = vehicle.vehicleType
        }
    }
    
    // Service Details
    var selectedService by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Mechanic Assignment - default to random suggestion or first available
    var selectedMechanic by remember(randomMechanicSuggestion, availableMechanics) { 
        mutableIntStateOf(randomMechanicSuggestion ?: availableMechanics.firstOrNull() ?: 1) 
    }
    
    // Update selection when random suggestion changes
    LaunchedEffect(randomMechanicSuggestion) {
        randomMechanicSuggestion?.let { selectedMechanic = it }
    }
    
    val serviceTypes = com.example.zyberauto.common.AppConstants.ServiceTypes
    val vehicleTypes = com.example.zyberauto.common.AppConstants.VehicleTypes

    val scrollState = rememberScrollState()
    
    LaunchedEffect(uiState) {
        if (uiState is WalkInUiState.Success) {
            // Maybe show toast or dialog
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Walk-In Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState is WalkInUiState.Error) {
                    Text(
                        text = (uiState as WalkInUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Customer Type Toggle: New vs Existing
                Text("Customer Type", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isNewCustomer,
                        onClick = { 
                            isNewCustomer = true
                            viewModel.selectExistingCustomer(null)
                            // Clear fields for new customer
                            fullName = ""
                            phoneNumber = ""
                            email = ""
                            vehicleBrand = ""
                            vehicleModel = ""
                            plateNumber = ""
                            vehicleType = "Sedan"
                            selectedVehicleIndex = -1
                        },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New Customer")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isNewCustomer,
                        onClick = { isNewCustomer = false },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Existing Customer")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                HorizontalDivider()
                
                // Show existing customer dropdown if "Existing" is selected
                if (!isNewCustomer) {
                    Text("Select Customer", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    
                    ExposedDropdownMenuBox(
                        expanded = customerDropdownExpanded,
                        onExpandedChange = { customerDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "Select a customer...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = customerDropdownExpanded,
                            onDismissRequest = { customerDropdownExpanded = false }
                        ) {
                            existingCustomers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(customer.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(customer.phoneNumber.ifBlank { customer.email }, 
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectExistingCustomer(customer)
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Show customer's saved vehicles if any
                    if (selectedCustomer != null && customerVehicles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select Vehicle", style = MaterialTheme.typography.titleSmall)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option for manual entry
                            FilterChip(
                                selected = selectedVehicleIndex == -1,
                                onClick = { 
                                    selectedVehicleIndex = -1
                                    vehicleBrand = ""
                                    vehicleModel = ""
                                    plateNumber = ""
                                    vehicleType = "Sedan"
                                },
                                label = { Text("Enter Manually") }
                            )
                        }
                        
                        // Show saved vehicles as selectable chips
                        customerVehicles.forEachIndexed { index, vehicle ->
                            FilterChip(
                                selected = selectedVehicleIndex == index,
                                onClick = { selectedVehicleIndex = index },
                                label = { 
                                    Text("${vehicle.model} (${vehicle.plateNumber})") 
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    HorizontalDivider()
                }

                Text(
                    if (isNewCustomer) "New Customer Details" else "Customer Details (Auto-filled)", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.primary
                )
                
                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isNewCustomer // Disable if using existing customer
                )
                
                 AppTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone Number",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isNewCustomer
                )
                
                 AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email (Optional)",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isNewCustomer
                )
                
                HorizontalDivider()
                
                Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                AppTextField(
                    value = vehicleBrand,
                    onValueChange = { vehicleBrand = it },
                    label = "Brand (e.g. Toyota)",
                    modifier = Modifier.fillMaxWidth()
                )
                
                AppTextField(
                    value = vehicleModel,
                    onValueChange = { vehicleModel = it },
                    label = "Model (e.g. Vios)",
                    modifier = Modifier.fillMaxWidth()
                )
                
                AppTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = "Plate Number",
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Vehicle Type dropdown
                com.example.zyberauto.presentation.common.components.DropdownField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    options = vehicleTypes,
                    label = "Vehicle Type",
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
                Text("Service Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                // Dropdown substitution for Service Type
                com.example.zyberauto.presentation.common.components.DropdownField(
                    value = selectedService,
                    onValueChange = { selectedService = it },
                    options = serviceTypes,
                    label = "Service Type",
                    modifier = Modifier.fillMaxWidth()
                )
                
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes / Issues",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                HorizontalDivider()
                
                Text("Assign Mechanic", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                // Random Select Button
                OutlinedButton(
                    onClick = { selectedMechanic = viewModel.selectRandomMechanic() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Random Select (No Bias)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Mechanic Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..3).forEach { mechanic ->
                        val isAvailable = availableMechanics.contains(mechanic)
                        val isRandomSelected = randomMechanicSuggestion == mechanic
                        
                        FilterChip(
                            selected = selectedMechanic == mechanic,
                            onClick = { selectedMechanic = mechanic },
                            label = { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mechanic $mechanic") 
                                    if (isRandomSelected) {
                                        Text("🎲", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = isAvailable || availableMechanics.isEmpty(),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isRandomSelected) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
                
                // Show availability status
                if (availableMechanics.size < 3 && availableMechanics.isNotEmpty()) {
                    Text(
                        text = "⚠️ Some mechanics are busy. Available: ${availableMechanics.joinToString(", ") { "Mechanic $it" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFA000)
                    )
                } else if (availableMechanics.isEmpty()) {
                    Text(
                        text = "⚠️ All mechanics are currently busy for walk-ins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFA000)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AppButton(
                    text = "SUBMIT WALK-IN",
                    onClick = { 
                        viewModel.submitWalkIn(
                            fullName, phoneNumber, email,
                            vehicleBrand, vehicleModel, plateNumber, vehicleType,
                            selectedService, notes, selectedMechanic,
                            existingCustomerId = if (!isNewCustomer) selectedCustomer?.uid else null
                        ) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState is WalkInUiState.Loading
                )
            }
        }
    }
}
