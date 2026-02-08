package com.example.zyberauto.presentation.customer.bookings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.presentation.common.components.AppTextField
import com.example.zyberauto.presentation.common.components.DropdownField
import com.example.zyberauto.presentation.common.components.SaveCarDialog
import com.example.zyberauto.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * NewBookingScreen - Enhanced form for creating a new booking request.
 * 
 * Features:
 * - Saved vehicle selection with auto-fill
 * - Manual entry mode with save-car prompt
 * - Custom date/time picker with future-date validation
 * - Estimated cost calculation with inventory availability check
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewBookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // ViewModel state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedVehicles by viewModel.savedVehicles.collectAsStateWithLifecycle()
    val selectedVehicle by viewModel.selectedVehicle.collectAsStateWithLifecycle()
    val isManualEntry by viewModel.isManualEntry.collectAsStateWithLifecycle()
    val showSaveCarDialog by viewModel.showSaveCarDialog.collectAsStateWithLifecycle()
    val isTimeSlotFullyBooked by viewModel.isTimeSlotFullyBooked.collectAsStateWithLifecycle()
    val vehicleHasActiveBooking by viewModel.vehicleHasActiveBooking.collectAsStateWithLifecycle()
    val costEstimate by viewModel.costEstimate.collectAsStateWithLifecycle()
    val isCalculatingCost by viewModel.isCalculatingCost.collectAsStateWithLifecycle()

    // Form state
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Date/Time state
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) } // hour, minute
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Unified color scheme
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)

    // Auto-fill when vehicle is selected
    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let { vehicle ->
            vehicleModel = vehicle.model
            vehicleType = vehicle.vehicleType
            plateNumber = vehicle.plateNumber
            vehicleYear = vehicle.year
        }
    }

    // Clear fields when switching to manual entry
    LaunchedEffect(isManualEntry) {
        if (isManualEntry && selectedVehicle == null) {
            vehicleModel = ""
            vehicleType = ""
            plateNumber = ""
            vehicleYear = ""
        }
    }

    // Auto-validate plate number when manually entered (debounced)
    LaunchedEffect(plateNumber) {
        if (isManualEntry && plateNumber.length >= 3) {
            kotlinx.coroutines.delay(500) // Debounce 500ms
            viewModel.checkVehicleActiveBooking(plateNumber)
        }
    }

    // Calculate cost estimate when service type or vehicle type changes
    LaunchedEffect(serviceType, vehicleType) {
        if (serviceType.isNotBlank() && vehicleType.isNotBlank()) {
            kotlinx.coroutines.delay(300) // Debounce 300ms
            viewModel.calculateCostEstimate(serviceType, vehicleType)
        }
    }

    // Navigate back on successful submission
    LaunchedEffect(uiState) {
        if (uiState is NewBookingUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    // Save Car Dialog
    if (showSaveCarDialog) {
        SaveCarDialog(
            vehicleModel = vehicleModel,
            plateNumber = plateNumber,
            onConfirm = { viewModel.saveVehicleAfterBooking() },
            onDismiss = { viewModel.dismissSaveCarDialog() }
        )
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
            // Header
            NewBookingHeader(
                textColor = textColor,
                onBackClick = onNavigateBack
            )

            // Vehicle Details Card
            FormSectionCard(
                title = "Vehicle Details",
                textColor = textColor
            ) {
                // Show toggle only if user has saved vehicles
                if (savedVehicles.isNotEmpty()) {
                    VehicleSourceToggle(
                        isManualEntry = isManualEntry,
                        onToggle = { viewModel.setManualEntry(it) }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                if (!isManualEntry && savedVehicles.isNotEmpty()) {
                    // Saved Vehicle Dropdown
                    SavedVehicleDropdown(
                        vehicles = savedVehicles,
                        selectedVehicle = selectedVehicle,
                        onSelect = { viewModel.selectVehicle(it) }
                    )
                    
                    // Show read-only fields
                    if (selectedVehicle != null) {
                        Spacer(Modifier.height(12.dp))
                        ReadOnlyField(label = "Model", value = vehicleModel)
                        Spacer(Modifier.height(8.dp))
                        ReadOnlyField(label = "Type", value = vehicleType)
                        Spacer(Modifier.height(8.dp))
                        ReadOnlyField(label = "Plate Number", value = plateNumber)
                        if (vehicleYear.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            ReadOnlyField(label = "Year", value = vehicleYear)
                        }
                    }
                } else {
                    // Manual Entry Fields
                    AppTextField(
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        label = "Vehicle Model (e.g. Toyota Vios)"
                    )
                    Spacer(Modifier.height(12.dp))
                    DropdownField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        options = com.example.zyberauto.common.AppConstants.VehicleTypes,
                        label = "Vehicle Type"
                    )
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        value = plateNumber,
                        onValueChange = { plateNumber = it },
                        label = "Plate Number"
                    )
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        value = vehicleYear,
                        onValueChange = { vehicleYear = it },
                        label = "Year (Optional)"
                    )
                }

                // Show warning if vehicle already has an active booking
                if (vehicleHasActiveBooking) {
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PrimaryRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ This vehicle already has an active booking. Please wait until the current service is completed.",
                            color = PrimaryRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Service & Schedule Card
            FormSectionCard(
                title = "Service & Schedule",
                textColor = textColor
            ) {
                DropdownField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    options = com.example.zyberauto.common.AppConstants.ServiceTypes,
                    label = "Service Type"
                )
                
                // Cost Estimate Card - shows when service type is selected
                if (serviceType.isNotBlank() && vehicleType.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    CostEstimateCard(
                        costEstimate = costEstimate,
                        isCalculating = isCalculatingCost
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Date Picker
                DatePickerButton(
                    selectedDate = selectedDate,
                    dateFormat = dateFormat,
                    onClick = {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                selectedDate = calendar.time
                                
                                // Check availability when both date and time are selected
                                if (selectedTime != null) {
                                    val checkCal = Calendar.getInstance().apply {
                                        set(year, month, day)
                                        set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                                        set(Calendar.MINUTE, selectedTime!!.second)
                                    }
                                    val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                    viewModel.checkTimeSlotAvailability(
                                        checkCal.time,
                                        timeFormat.format(checkCal.time)
                                    )
                                }
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.minDate = System.currentTimeMillis() - 1000
                        }.show()
                    }
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Time Picker
                TimePickerButton(
                    selectedTime = selectedTime,
                    onClick = {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedTime = Pair(hour, minute)
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                
                                // Check availability when both date and time are selected
                                if (selectedDate != null) {
                                    val checkCal = Calendar.getInstance().apply {
                                        time = selectedDate!!
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                    }
                                    val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                    viewModel.checkTimeSlotAvailability(
                                        checkCal.time,
                                        timeFormat.format(checkCal.time)
                                    )
                                }
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                )
                
                // Show availability status
                if (selectedDate != null && selectedTime != null) {
                    Spacer(Modifier.height(8.dp))
                    if (isTimeSlotFullyBooked) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    PrimaryRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚠️ This time slot is fully booked. All mechanics are busy.",
                                color = PrimaryRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✓ Time slot available",
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes / Issues (Optional)",
                    modifier = Modifier.height(100.dp),
                    singleLine = false
                )
            }

            // Error message
            if (uiState is NewBookingUiState.Error) {
                Text(
                    text = (uiState as NewBookingUiState.Error).message,
                    color = PrimaryRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.weight(1f))

            // Submit Button - disabled if vehicle has active booking or slot is fully booked
            val canSubmit = !vehicleHasActiveBooking && !isTimeSlotFullyBooked
            
            RedSubmitButton(
                text = if (vehicleHasActiveBooking) "VEHICLE HAS ACTIVE BOOKING" 
                       else if (isTimeSlotFullyBooked) "TIME SLOT FULLY BOOKED"
                       else "SUBMIT BOOKING",
                onClick = {
                    // Combine date and time
                    if (selectedDate != null && selectedTime != null) {
                        val finalCalendar = Calendar.getInstance().apply {
                            time = selectedDate!!
                            set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                            set(Calendar.MINUTE, selectedTime!!.second)
                        }
                        viewModel.submitBooking(
                            vehicleModel = vehicleModel,
                            vehicleType = vehicleType,
                            plateNumber = plateNumber,
                            vehicleYear = vehicleYear,
                            serviceType = serviceType,
                            notes = notes,
                            scheduledDate = finalCalendar.time
                        )
                    } else {
                        // Show error if date/time not selected
                        viewModel.submitBooking(
                            vehicleModel = vehicleModel,
                            vehicleType = vehicleType,
                            plateNumber = plateNumber,
                            vehicleYear = vehicleYear,
                            serviceType = serviceType,
                            notes = notes,
                            scheduledDate = Date(0) // Will fail validation
                        )
                    }
                },
                isLoading = uiState is NewBookingUiState.Loading,
                enabled = canSubmit
            )
        }
    }
}

@Composable
private fun VehicleSourceToggle(
    isManualEntry: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleChip(
            selected = !isManualEntry,
            onClick = { onToggle(false) },
            icon = Icons.Default.DirectionsCar,
            label = "My Cars"
        )
        ToggleChip(
            selected = isManualEntry,
            onClick = { onToggle(true) },
            icon = Icons.Default.Edit,
            label = "Enter Manually"
        )
    }
}

@Composable
private fun ToggleChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) PrimaryRed.copy(alpha = 0.1f) else Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PrimaryRed else Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) PrimaryRed else Color(0xFF64748B),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedVehicleDropdown(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onSelect: (Vehicle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedVehicle?.let { "${it.model} - ${it.plateNumber}" } ?: "Select a vehicle",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Vehicle") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                focusedLabelColor = PrimaryRed
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = { 
                        Column {
                            Text(vehicle.model, fontWeight = FontWeight.Medium)
                            Text(
                                vehicle.plateNumber, 
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    },
                    onClick = {
                        onSelect(vehicle)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun DatePickerButton(
    selectedDate: Date?,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = selectedDate?.let { dateFormat.format(it) } ?: "Select Date",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TimePickerButton(
    selectedTime: Pair<Int, Int>?,
    onClick: () -> Unit
) {
    val timeText = selectedTime?.let {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, it.first)
            set(Calendar.MINUTE, it.second)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    } ?: "Select Time"
    
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NewBookingHeader(
    textColor: Color,
    onBackClick: () -> Unit
) {
    Column {
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

        Text(
            "NEW REQUEST",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Book a Service",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        Text(
            text = "Fill in the details below",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun RedSubmitButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed,
            contentColor = Color.White,
            disabledContainerColor = PrimaryRed.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Card displaying the estimated cost breakdown for the selected service.
 * Shows labor cost, parts cost, and total estimate.
 * Also shows warnings if some items are insufficient/out of stock.
 */
@Composable
private fun CostEstimateCard(
    costEstimate: com.example.zyberauto.domain.service.CostEstimateResult?,
    isCalculating: Boolean
) {
    val currencyFormat = remember { java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "PH")) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F9FF) // Light blue background
        ),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Estimated Cost",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1E40AF)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (isCalculating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF3B82F6),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Calculating...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            } else if (costEstimate != null) {
                // Labor Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Labor:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = currencyFormat.format(costEstimate.laborCost),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF374151)
                    )
                }
                
                // Parts Cost
                if (costEstimate.partsCost > 0) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Parts:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = currencyFormat.format(costEstimate.partsCost),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF374151)
                        )
                    }
                    
                    // Parts breakdown
                    costEstimate.partsToDeduct.forEach { part ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${part.itemName} (${part.quantityNeeded.toInt()}x)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = currencyFormat.format(part.subtotal),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFBFDBFE))
                Spacer(Modifier.height(8.dp))
                
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Estimate:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E40AF)
                    )
                    Text(
                        text = currencyFormat.format(costEstimate.totalEstimate),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E40AF)
                    )
                }
                
                // Disclaimer
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⚠️ Note: Final price may vary based on actual service needs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
                
                // Insufficient items warning
                if (costEstimate.hasInsufficientItems) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Some items may be low in stock:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF92400E)
                            )
                            costEstimate.insufficientItems.forEach { item ->
                                Text(
                                    text = "• $item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "You can still submit. The shop will contact you about availability.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78716C)
                            )
                        }
                    }
                }
            }
        }
    }
}
