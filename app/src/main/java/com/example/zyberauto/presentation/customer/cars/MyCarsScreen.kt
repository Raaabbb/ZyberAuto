package com.example.zyberauto.presentation.customer.cars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.domain.model.Vehicle
import com.example.zyberauto.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * MyCarsScreen - Displays list of customer's registered vehicles.
 * 
 * UI Features:
 * - Light gray background matching unified design
 * - Section header with red label
 * - White cards for each vehicle with delete option
 * - Red FAB with glow to add new vehicle
 * 
 * @param onNavigateToAddCar Callback to navigate to add car form
 * @param viewModel ViewModel providing vehicle data
 */
@Composable
fun MyCarsScreen(
    onNavigateToAddCar: () -> Unit,
    viewModel: MyCarsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Unified color scheme
    val backgroundColor = Color(0xFFF5F5F7)
    val textColor = Color(0xFF0F172A)
    val subtextColor = Color(0xFF64748B)
    
    // Delete confirmation dialog state
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }

    // Delete confirmation dialog
    if (vehicleToDelete != null) {
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            containerColor = Color.White,
            title = { Text("Delete Vehicle?", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Are you sure you want to remove ${vehicleToDelete?.model} (${vehicleToDelete?.plateNumber})?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vehicleToDelete?.let { viewModel.deleteVehicle(it.id) }
                        vehicleToDelete = null
                    }
                ) {
                    Text("Delete", color = PrimaryRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = uiState) {
            is MyCarsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryRed
                )
            }
            is MyCarsUiState.Error -> {
                Text(
                    text = state.message,
                    color = PrimaryRed,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is MyCarsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 100.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Header
                    item {
                        CarsSectionHeader(
                            carCount = state.vehicles.size,
                            textColor = textColor
                        )
                    }
                    
                    // Empty state or car cards
                    if (state.vehicles.isEmpty()) {
                        item {
                            EmptyCarsCard(subtextColor = subtextColor)
                        }
                    } else {
                        items(state.vehicles) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                onDelete = { vehicleToDelete = vehicle }
                            )
                        }
                    }
                }
            }
        }
        
        // Floating Action Button with red glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
        ) {
            RedGlowFab(onClick = onNavigateToAddCar)
        }
    }
}

/**
 * Section header with red label and title.
 */
@Composable
private fun CarsSectionHeader(
    carCount: Int,
    textColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            "GARAGE",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (carCount > 0) "My Vehicles" else "No Vehicles Yet",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
        Text(
            text = if (carCount > 0) "$carCount registered vehicle${if (carCount > 1) "s" else ""}" else "Add your first car",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

/**
 * Empty state card when no vehicles registered.
 */
@Composable
private fun EmptyCarsCard(subtextColor: Color) {
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
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = subtextColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tap the + button to add your first vehicle",
                style = MaterialTheme.typography.bodyMedium,
                color = subtextColor
            )
        }
    }
}

/**
 * Individual vehicle card with white background.
 */
@Composable
fun VehicleCard(
    vehicle: Vehicle,
    textColor: Color,
    subtextColor: Color,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Vehicle details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.model,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Pin,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = subtextColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = vehicle.plateNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtextColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (vehicle.year.isNotEmpty()) {
                        Text(
                            text = " • ${vehicle.year}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = subtextColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added ${dateFormat.format(Date(vehicle.dateRegistered))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor.copy(alpha = 0.7f)
                )
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = subtextColor
                )
            }
        }
    }
}

/**
 * Floating Action Button with red glow effect.
 */
@Composable
private fun RedGlowFab(onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(PrimaryRed.copy(alpha = 0.4f), CircleShape)
                .blur(16.dp)
        )
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(PrimaryRed, CircleShape)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Vehicle",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
