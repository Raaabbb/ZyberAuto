package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zyberauto.presentation.common.components.AppHeader
import com.example.zyberauto.ui.theme.BackgroundDark
import com.example.zyberauto.ui.theme.BackgroundLight
import com.example.zyberauto.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryDashboardScreen(
    onNavigateToSchedule: () -> Unit,
    onNavigateToWalkIn: () -> Unit,
    onNavigateToPending: () -> Unit = {},
    onNavigateToInquiries: () -> Unit = {},
    viewModel: SecretaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val backgroundColor = Color(0xFFF5F5F7) 
    val textColor = Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DashboardUiState.Error -> {
                Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            }
            is DashboardUiState.Success -> {
                // Main Content Scrollable
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Text(
                                "COMMAND CENTER", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = PrimaryRed, 
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Welcome, Secretary.",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                            Text(
                                "Fleet protocols are operating at 100% capacity.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Hero Card (Full Width) - Active Operations
                    item {
                        DashboardHeroCard(
                            activeCount = state.stats.activeOperations,
                            pendingCount = state.stats.pendingRequests,
                            onManageAllClick = onNavigateToSchedule
                        )
                    }

                    // Row 1: Efficiency | Pending Requests
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EfficiencyCard(
                                rate = state.stats.efficiencyRate,
                                change = state.stats.efficiencyChange,
                                modifier = Modifier.weight(1f)
                            )
                            PendingRequestsCard(
                                count = state.stats.pendingRequests,
                                onClick = onNavigateToPending,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Row 2: Chart | Inquiries
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ChartCard(
                                data = state.stats.chartData,
                                modifier = Modifier.weight(1f)
                            )
                            InquiriesCard(
                                unreadCount = state.stats.unreadInquiries,
                                onClick = onNavigateToInquiries,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Timeline Schedule (Full Width)
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            CyberpunkTimeline(appointments = state.todaySchedule)
                        }
                    }
                }
            }
        }
        // Header is now handled by SecretaryShell
    }
}

@Composable
fun GlassHeader(modifier: Modifier = Modifier) {
    // Glassmorphism effect usually requires library or API 31+ blur. 
    // Fallback: Semi-transparent background.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.9f)) // Light semi-transparent
                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryRed.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("ZyberAuto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("SECRETARY HUB", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = PrimaryRed, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF0F172A), modifier = Modifier.size(20.dp))
                }
                // Avatar Placeholder
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray))
            }
        }
    }
}

@Composable
fun PendingRequestsStack(count: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PENDING STACK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), letterSpacing = 1.sp)
            Surface(
                color = PrimaryRed.copy(alpha = 0.2f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed)
            ) {
                Text(
                    "$count NEW",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3D Stack Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp), // Height for the stack
            contentAlignment = Alignment.TopCenter
        ) {
            // Bottom Card (Smallest, darkest)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(200.dp)
                    .offset(y = 24.dp)
                    .blur(2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.05f))
            ) {}

            // Middle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(200.dp)
                    .offset(y = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.1f))
            ) {}

            // Top Card (Active)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // White Top Card
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                     Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                         Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                             // Car Image Placeholder
                             Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray))
                             Column {
                                 Text("Model S Plaid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                 Text("Drive Unit Replacement", style = MaterialTheme.typography.bodySmall, color = PrimaryRed)
                             }
                         }
                         Icon(Icons.Default.Info, null, tint = PrimaryRed)
                     }
                     
                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray))
                         Text("Marcello G. • Priority Client", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0F172A).copy(alpha = 0.7f))
                     }

                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                         Button(
                             onClick = {},
                             modifier = Modifier.weight(1f),
                             colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.05f), contentColor = Color(0xFF0F172A)),
                             shape = RoundedCornerShape(8.dp)
                         ) { Text("DECLINE") }
                         Button(
                             onClick = {},
                             modifier = Modifier.weight(2f),
                             colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed, contentColor = Color.White),
                             shape = RoundedCornerShape(8.dp)
                         ) { Text("ACCEPT & SCHEDULE") }
                     }
                     
                     // View Details Link
                     Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                         Text(
                             "VIEW FULL DETAILS",
                             style = MaterialTheme.typography.labelSmall,
                             color = Color(0xFF0F172A).copy(alpha = 0.5f),
                             textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                             fontSize = 10.sp,
                             modifier = Modifier.clickable { }
                         )
                     }
                }
            }
        }
    }
}

@Composable
fun CyberpunkTimeline(appointments: List<com.example.zyberauto.domain.model.Appointment>) {
    val manilaTime = java.time.LocalTime.now(java.time.ZoneId.of("Asia/Manila"))
    val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    
    val currentHour = manilaTime.hour
    val hoursToShow = (currentHour - 2 .. currentHour + 4).toList()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        listState.scrollToItem(index = 2)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SCHEDULE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryRed))
                Text(manilaTime.format(formatter), style = MaterialTheme.typography.labelMedium, color = PrimaryRed, fontWeight = FontWeight.Bold)
            }
        }

        // Mechanic Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..3).forEach { mechanic ->
                    Text(
                        text = "Mechanic $mechanic",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (appointments.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No appointments scheduled for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                val centerPadding = (maxWidth - 220.dp) / 2
                
                // Central Red Line
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, PrimaryRed, Color.Transparent)
                            )
                        )
                        .zIndex(10f)
                )

                // Horizontal List
                androidx.compose.foundation.lazy.LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    contentPadding = PaddingValues(horizontal = centerPadding)
                ) {
                    items(count = hoursToShow.size) { index ->
                        val hour = hoursToShow[index]
                        val isCurrentHour = hour == currentHour
                        val displayTime = getCyberpunkTime(hour).format(java.time.format.DateTimeFormatter.ofPattern("hh:00 a"))

                        // Find appointments for this hour
                        val hourAppointments = appointments.filter { appt ->
                            try {
                                val timeSlot = appt.timeSlot // e.g., "10:00 AM"
                                val apptHour = timeSlot.split(":")[0].toIntOrNull() ?: -1
                                val isPM = timeSlot.uppercase().contains("PM")
                                val adjustedHour = when {
                                    isPM && apptHour != 12 -> apptHour + 12
                                    !isPM && apptHour == 12 -> 0
                                    else -> apptHour
                                }
                                adjustedHour == hour
                            } catch (e: Exception) {
                                false
                            }
                        }

                        // Group appointments by assigned mechanic (3 rows for Mechanic 1-3)
                        val rowEvents = (1..3).map { mechanicNum ->
                            hourAppointments.find { it.assignedMechanic == mechanicNum }?.let { appt ->
                                MechanicSlotData(
                                    vehicleModel = appt.vehicleModel,
                                    mechanicName = "Mechanic $mechanicNum",
                                    status = appt.status
                                )
                            }
                        }

                        TimelineSlot(
                            time = displayTime,
                            isCurrent = isCurrentHour,
                            mechanicSlots = rowEvents
                        )
                    }
                }
            }
        }
    }
}

// Data class for mechanic slot display
data class MechanicSlotData(
    val vehicleModel: String,
    val mechanicName: String,
    val status: String
)

// Helper function to handle hour overflow
fun getCyberpunkTime(hour: Int): java.time.LocalTime {
    val h = ((hour % 24) + 24) % 24
    return java.time.LocalTime.of(h, 0)
}

@Composable
fun TimelineSlot(time: String, isCurrent: Boolean, mechanicSlots: List<MechanicSlotData?>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(if (isCurrent) 220.dp else 160.dp)) {
        Text(
            time, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (isCurrent) PrimaryRed else Color(0xFF0F172A).copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        
        // Timeline Node
        Box(modifier = Modifier.size(if (isCurrent) 12.dp else 8.dp).clip(CircleShape).background(if (isCurrent) PrimaryRed else Color(0xFF0F172A).copy(alpha = 0.2f)))
        
        Spacer(Modifier.height(16.dp))

        // 3 Rows for Mechanic 1-3
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            mechanicSlots.forEachIndexed { i, slotData ->
                 if (slotData != null) {
                    // Active Card - appointment assigned to this mechanic
                     Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .border(if(isCurrent) 2.dp else 1.dp, if (isCurrent) PrimaryRed.copy(alpha = 0.5f) else Color.Black.copy(alpha=0.1f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = if (isCurrent) PrimaryRed.copy(alpha = 0.1f) else Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    when {
                                        slotData.status == "COMPLETED" -> "COMPLETED"
                                        isCurrent -> "IN PROGRESS"
                                        else -> "SCHEDULED"
                                    }, 
                                    color = when {
                                        slotData.status == "COMPLETED" -> Color(0xFF4CAF50)
                                        isCurrent -> PrimaryRed 
                                        else -> Color(0xFF0F172A).copy(alpha=0.5f)
                                    }, 
                                    fontWeight = FontWeight.Black, 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontSize = 8.sp
                                )
                                if(isCurrent && slotData.status != "COMPLETED") Icon(Icons.Default.Bolt, null, tint = PrimaryRed, modifier = Modifier.size(16.dp))
                            }
                            Text(slotData.vehicleModel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                            
                            if (isCurrent && slotData.status != "COMPLETED") {
                                // Mini Progress
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Black.copy(alpha=0.1f), CircleShape)) {
                                    Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(PrimaryRed, CircleShape))
                                }
                            } else {
                                // Mechanic Name
                                Text(slotData.mechanicName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF0F172A).copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                 } else {
                     // Empty Slot - no appointment for this mechanic at this hour
                     Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(110.dp)
                             .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                             .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)),
                         contentAlignment = Alignment.Center
                     ) {
                         Text(
                             "Mechanic ${i + 1}",
                             style = MaterialTheme.typography.labelSmall,
                             color = Color(0xFF94A3B8)
                         )
                     }
                 }
            }
        }
    }
}

@Composable
fun DashboardHeroCard(
    activeCount: Int,
    pendingCount: Int,
    onManageAllClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Live Status Badge
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "LIVE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Content
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Active Operations",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$activeCount Active • $pendingCount Pending",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Manage Button
            Button(
                onClick = onManageAllClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "MANAGE ALL",
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EfficiencyCard(rate: Float, change: Float = 0f, modifier: Modifier = Modifier) {
    val percentage = (rate * 100).toInt()
    val changePercent = (change * 100).toInt()
    val isPositive = change >= 0
    
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EFFICIENCY",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (percentage >= 80) Color(0xFF10B981) else if (percentage >= 50) Color(0xFFF59E0B) else PrimaryRed,
                    fontWeight = FontWeight.Bold
                )
                
                // Week comparison badge
                if (changePercent != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF10B981) else PrimaryRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}$changePercent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPositive) Color(0xFF10B981) else PrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Text(
                text = "vs last week",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun PendingRequestsCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "PENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Text(
                text = "$count",
                style = MaterialTheme.typography.displaySmall,
                color = if (count > 5) PrimaryRed else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "review",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ChartCard(data: List<Int>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            // Simple Bar Chart
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxVal = data.maxOrNull()?.coerceAtLeast(1) ?: 1
                data.forEach { value ->
                    val heightFraction = value.toFloat() / maxVal.toFloat()
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight(heightFraction.coerceIn(0.1f, 1f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(PrimaryRed.copy(alpha = 0.7f))
                    )
                }
            }
            
            Text(
                text = "last 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun InquiriesCard(unreadCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "INQUIRIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (unreadCount > 0) PrimaryRed else Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$unreadCount",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (unreadCount > 0) PrimaryRed else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "unread",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

