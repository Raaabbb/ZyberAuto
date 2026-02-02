package com.example.zyberauto.presentation.secretary.walkin

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
    
    // Customer Details
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Vehicle Details
    var vehicleBrand by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    
    // Service Details
    var selectedService by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val serviceTypes = com.example.zyberauto.common.AppConstants.ServiceTypes

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

                Text("New Customer Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    modifier = Modifier.fillMaxWidth()
                )
                
                 AppTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone Number",
                    modifier = Modifier.fillMaxWidth()
                )
                
                 AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email (Optional)",
                    modifier = Modifier.fillMaxWidth()
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AppButton(
                    text = "SUBMIT WALK-IN",
                    onClick = { 
                        viewModel.submitWalkIn(
                            fullName, phoneNumber, email,
                            vehicleBrand, vehicleModel, plateNumber,
                            selectedService, notes
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
