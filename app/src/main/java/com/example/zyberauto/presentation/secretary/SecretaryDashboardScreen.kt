package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun SecretaryDashboardScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToWalkIn: () -> Unit,
    viewModel: SecretaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Welcome back, Secretary",
                style = MaterialTheme.typography.headlineMedium
            )

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Text("Error loading dashboard: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is DashboardUiState.Success -> {
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DashboardStatCard(
                            title = "Today's Appts",
                            value = state.stats.appointmentsToday.toString(),
                            icon = Icons.Default.CalendarToday,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Pending",
                            value = state.stats.pendingRequests.toString(),
                            icon = Icons.Default.Info,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                        DashboardStatCard(
                            title = "Customers",
                            value = state.stats.totalCustomers.toString(),
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick Actions
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = onNavigateToWalkIn,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Walk-In")
                            }
                            OutlinedButton(
                                onClick = onNavigateToSchedule,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Icon(Icons.Default.Event, null)
                                Spacer(Modifier.width(8.dp))
                                Text("New Appointment")
                            }
                        }
                    }

                    // Today's Schedule
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Today's Schedule", style = MaterialTheme.typography.titleMedium)
                        
                        if (state.todaySchedule.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                            ) {
                                Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No appointments scheduled for today.")
                                }
                            }
                        } else {
                            // Using a LazyColumn if there are many, but inside a Column we need constrained height or use LazyColumn as parent.
                            // Since this is a dashboard, better to use LazyColumn as the main container for the screen.
                            // But for now, let's just emit items in a simple column for simplicity as usually there aren't thousands per day.
                            // Update: Refactoring to use LazyColumn for the whole screen would be best practice, but keeping simple Column structure 
                            // as requested in the plan (Screen with components).
                            
                            state.todaySchedule.forEach { appointment ->
                                ScheduleItemCard(appointment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun ScheduleItemCard(appointment: com.example.zyberauto.domain.model.Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.timeSlot, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(appointment.customerName, style = MaterialTheme.typography.titleMedium)
                Text("${appointment.vehicleModel} • ${appointment.serviceType}", style = MaterialTheme.typography.bodySmall)
            }
            // Use StatusBadge logic or simple text
            Text(
                appointment.status, 
                style = MaterialTheme.typography.labelSmall,
                color = if (appointment.status == "IN_PROGRESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
