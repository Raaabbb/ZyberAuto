package com.example.zyberauto.presentation.customer.cars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.presentation.common.components.DropdownField
import com.example.zyberauto.common.AppConstants
import com.example.zyberauto.ui.theme.PrimaryRed

/**
 * AddCarScreen - Form for registering a new vehicle.
 * 
 * UI Features:
 * - Light gray background matching unified design
 * - Section header with red label
 * - White form card with rounded corners
 * - Red submit button
 * 
 * @param onNavigateBack Callback to navigate back to My Cars list
 * @param viewModel ViewModel handling vehicle submission
 */
@Composable
fun AddCarScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCarViewModel = hiltViewModel()
) {
    // Form state
    var model by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Unified color scheme
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)

    // Navigate back on success
    LaunchedEffect(uiState) {
        if (uiState is AddCarUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back button
            AddCarHeader(
                textColor = textColor,
                onBackClick = onNavigateBack
            )
            
            // Vehicle Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Section title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Vehicle Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = textColor
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Enter your car information below",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtextColor
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    // Model field
                    AppTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = "Vehicle Model (e.g. Toyota Vios 2020)"
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Vehicle Type dropdown (required)
                    DropdownField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        options = AppConstants.VehicleTypes,
                        label = "Vehicle Type"
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Plate number field
                    AppTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it.uppercase() },
                        label = "Plate Number"
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Year field (optional)
                    AppTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = "Year (Optional)"
                    )
                }
            }
            
            // Error message if any
            if (uiState is AddCarUiState.Error) {
                Text(
                    text = (uiState as AddCarUiState.Error).message,
                    color = PrimaryRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.weight(1f))
            
            // Submit Button
            Button(
                onClick = {
                    viewModel.addVehicle(model, vehicleType, plateNumber, year)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                enabled = uiState !is AddCarUiState.Loading
            ) {
                if (uiState is AddCarUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "ADD VEHICLE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Header section with back button and title.
 */
@Composable
private fun AddCarHeader(
    textColor: Color,
    onBackClick: () -> Unit
) {
    Column {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.offset(x = (-12).dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = textColor
            )
        }
        
        // Red uppercase label
        Text(
            "NEW VEHICLE",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Main title
        Text(
            text = "Add a Car",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        // Subtitle
        Text(
            text = "Register your vehicle for easy booking",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}
