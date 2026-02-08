package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zyberauto.presentation.secretary.appointments.SecretaryScheduleWrapperScreen
import com.example.zyberauto.presentation.secretary.operations.SecretaryOperationsScreen

sealed class SecretaryRoute(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : SecretaryRoute("sec_dashboard", "Home", Icons.Default.Dashboard)
    object Schedule : SecretaryRoute("sec_schedule_tab", "Schedule", Icons.Default.CalendarToday)
    object Operations : SecretaryRoute("sec_operations_tab", "Operations", Icons.Default.Business)
    object Reports : SecretaryRoute("sec_reports", "Reports", Icons.Default.BarChart)
    
    // Hidden Routes (Not in Bottom Bar)
    object Customers : SecretaryRoute("sec_customers", "Customers", Icons.Default.People)
    object BookingRequests : SecretaryRoute("sec_requests", "Requests", Icons.AutoMirrored.Filled.Assignment)
    object Inquiries : SecretaryRoute("sec_inquiries", "Inquiries", Icons.AutoMirrored.Filled.Chat)
    object Inventory : SecretaryRoute("sec_inventory", "Inventory", Icons.Default.Inventory)
    object Settings : SecretaryRoute("sec_settings", "Settings", Icons.Default.Settings)
    object Appointments : SecretaryRoute("sec_appointments", "Appointments", Icons.Default.CalendarToday) // Kept for reference or internal nav
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryShell(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val secretaryNavController = rememberNavController()
    
    val navBackStackEntry by secretaryNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val bottomNavItems = listOf(
        SecretaryRoute.Dashboard,
        SecretaryRoute.Schedule,
        SecretaryRoute.Operations,
        SecretaryRoute.Settings
    )

    fun navigateTo(route: String) {
        secretaryNavController.navigate(route) {
            popUpTo(secretaryNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val backgroundColor = Color(0xFFF5F5F7)
    
    // Determine if header should be shown (hide on settings and detail screens)
    val showHeader = currentRoute != SecretaryRoute.Settings.route &&
                     currentRoute != "sec_walkin" &&
                     !currentRoute.orEmpty().startsWith("sec_booking_details")

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Unified AppHeader (Settings button removed from header)
                if (showHeader) {
                    com.example.zyberauto.presentation.common.components.AppHeader(
                        subtitle = "SECRETARY HUB"
                    )
                }
                
                // Content
                Box(modifier = Modifier.weight(1f)) {
                    SecretaryNavHost(
                        navController = secretaryNavController, 
                        paddingValues = PaddingValues(bottom = 100.dp),
                        onLogout = onLogout
                    )
                }
            }
            
            // Fixed Bottom Nav with embedded FAB (hide on settings)
            if (currentRoute != SecretaryRoute.Settings.route) {
                FixedBottomNav(
                    currentRoute = currentRoute,
                    onItemClick = { navigateTo(it.route) },
                    onWalkInClick = { secretaryNavController.navigate("sec_walkin") },
                    onNewBookingClick = { secretaryNavController.navigate("sec_schedule_appointment") },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun SpeedDialFab(
    onWalkInClick: () -> Unit,
    onNewBookingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Animation values
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 1.15f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "fabScale"
    )
    
    val actionOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 80f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "actionOffset"
    )
    
    val actionAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "actionAlpha"
    )
    
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        // Backdrop when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { isExpanded = false }
                    .zIndex(10f)
            )
        }
        
        // Action buttons container - positioned above bottom nav
        Box(
            modifier = Modifier
                .padding(bottom = 48.dp)
                .zIndex(if (isExpanded) 15f else 3f),
            contentAlignment = Alignment.Center
        ) {
            // Walk-in action (upper-left diagonal)
            if (actionOffset > 0f) {
                SpeedDialAction(
                    icon = Icons.Default.PersonAdd,
                    label = "Walk-in",
                    onClick = {
                        isExpanded = false
                        onWalkInClick()
                    },
                    alpha = actionAlpha,
                    modifier = Modifier.offset(
                        x = (-actionOffset * 0.85f).dp,
                        y = (-actionOffset).dp
                    )
                )
            }
            
            // New Booking action (upper-right diagonal)
            if (actionOffset > 0f) {
                SpeedDialAction(
                    icon = Icons.Default.CalendarToday,
                    label = "Booking",
                    onClick = {
                        isExpanded = false
                        onNewBookingClick()
                    },
                    alpha = actionAlpha,
                    modifier = Modifier.offset(
                        x = (actionOffset * 0.85f).dp,
                        y = (-actionOffset).dp
                    )
                )
            }
            
            // Main FAB
            Box(contentAlignment = Alignment.Center) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size((64 * scale).dp)
                        .background(
                            com.example.zyberauto.ui.theme.PrimaryRed.copy(alpha = 0.4f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .blur(16.dp)
                )
                
                // Main button with long-press detection
                Box(
                    modifier = Modifier
                        .size((64 * scale).dp)
                        .background(
                            com.example.zyberauto.ui.theme.PrimaryRed,
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.1f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    isExpanded = true
                                },
                                onTap = {
                                    if (isExpanded) {
                                        isExpanded = false
                                    } else {
                                        // Quick tap goes to walk-in
                                        onWalkInClick()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Actions",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedDialAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clickable(enabled = alpha > 0.5f) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    com.example.zyberauto.ui.theme.BackgroundDark,
                    androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.2f),
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
fun FixedBottomNav(
    currentRoute: String?,
    onItemClick: (SecretaryRoute) -> Unit,
    onWalkInClick: () -> Unit,
    onNewBookingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    
    // Animation for FAB expansion
    val fabScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFabExpanded) 1.1f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
        ),
        label = "fabScale"
    )
    
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
        ),
        label = "fabRotation"
    )
    
    val actionOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFabExpanded) 72f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "actionOffset"
    )
    
    val actionAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFabExpanded) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "actionAlpha"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Backdrop when expanded
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .offset(y = (-200).dp)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { isFabExpanded = false }
            )
        }
        
        // Speed dial action buttons (positioned above the nav bar)
        if (actionOffset > 0f) {
            // Walk-in action (upper-left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        x = (-actionOffset * 0.9f).dp,
                        y = (-actionOffset + 20).dp
                    )
                    .graphicsLayer { alpha = actionAlpha }
                    .clickable(enabled = actionAlpha > 0.5f) {
                        isFabExpanded = false
                        onWalkInClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(com.example.zyberauto.ui.theme.BackgroundDark, androidx.compose.foundation.shape.CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, "Walk-in", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Walk-in", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 10.sp)
                }
            }
            
            // New Booking action (upper-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        x = (actionOffset * 0.9f).dp,
                        y = (-actionOffset + 20).dp
                    )
                    .graphicsLayer { alpha = actionAlpha }
                    .clickable(enabled = actionAlpha > 0.5f) {
                        isFabExpanded = false
                        onNewBookingClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(com.example.zyberauto.ui.theme.BackgroundDark, androidx.compose.foundation.shape.CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, "Booking", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Booking", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 10.sp)
                }
            }
        }
        
        // Bottom Nav Bar
        Surface(
            modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter),
            color = com.example.zyberauto.ui.theme.BackgroundDark.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dashboard (FLOW)
                NavBarItem(
                    selected = currentRoute == SecretaryRoute.Dashboard.route,
                    icon = Icons.Default.Dashboard,
                    label = "FLOW",
                    onClick = { onItemClick(SecretaryRoute.Dashboard) }
                )
                
                // Schedule (BOOK)
                NavBarItem(
                    selected = currentRoute == SecretaryRoute.Schedule.route,
                    icon = Icons.Default.CalendarMonth,
                    label = "BOOK",
                    onClick = { onItemClick(SecretaryRoute.Schedule) }
                )
                
                // CENTER FAB - inline with other items
                Box(
                    modifier = Modifier
                        .size((52 * fabScale).dp)
                        .background(com.example.zyberauto.ui.theme.PrimaryRed, androidx.compose.foundation.shape.CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { isFabExpanded = true },
                                onTap = {
                                    if (isFabExpanded) {
                                        isFabExpanded = false
                                    } else {
                                        onWalkInClick()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Actions",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
                 
                // Operations (TECHS)
                NavBarItem(
                    selected = currentRoute == SecretaryRoute.Operations.route,
                    icon = Icons.Default.PrecisionManufacturing,
                    label = "TECHS",
                    onClick = { onItemClick(SecretaryRoute.Operations) }
                )
                
                // Reports (STATS) -> Replaced by Settings
                NavBarItem(
                    selected = currentRoute == SecretaryRoute.Settings.route,
                    icon = Icons.Default.Settings,
                    label = "SETTINGS",
                    onClick = { onItemClick(SecretaryRoute.Settings) }
                )
            }
        }
    }
}

@Composable
fun NavBarItem(selected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    val color = if (selected) com.example.zyberauto.ui.theme.PrimaryRed else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f)
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 10.sp, color = color)
    }
}

@Composable
fun SecretaryNavHost(
    navController: NavHostController, 
    paddingValues: PaddingValues,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SecretaryRoute.Dashboard.route
    ) {
        composable(SecretaryRoute.Dashboard.route) {
            SecretaryDashboardScreen(
                onNavigateToSchedule = { 
                    navController.navigate(SecretaryRoute.Schedule.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToWalkIn = { navController.navigate("sec_walkin") },
                // Navigate to Schedule tab with "Pending" filter instead of separate route
                onNavigateToPending = { 
                    navController.navigate("${SecretaryRoute.Schedule.route}?filter=Pending") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToInquiries = { navController.navigate(SecretaryRoute.Inquiries.route) }
            )
        }
        
        // Schedule Tab
        composable(
            route = "${SecretaryRoute.Schedule.route}?filter={filter}",
            arguments = listOf(androidx.navigation.navArgument("filter") { 
                defaultValue = "All"
                nullable = true 
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: "All"
            SecretaryScheduleWrapperScreen(
                onNavigateToSchedule = { /* Already here */ },
                onNavigateToWalkIn = { navController.navigate("sec_walkin") },
                onNavigateToBookingDetails = { bookingId ->
                    navController.navigate("sec_booking_details/$bookingId")
                },
                onMessageCustomer = { customerId, customerName ->
                    navController.navigate("sec_chat_direct/$customerId/$customerName")
                },
                initialFilter = filter
            )
        }

        // Operations Tab
        composable(SecretaryRoute.Operations.route) {
            SecretaryOperationsScreen(
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        // Reports Tab
        composable(SecretaryRoute.Reports.route) {
             com.example.zyberauto.presentation.secretary.reports.ReportsScreen(
                 onNavigateToRevenue = {
                     navController.navigate("sec_reports_revenue")
                 }
             )
        }
        
        // --- Nested / Hidden Routes ---

        composable(SecretaryRoute.Customers.route) {
            com.example.zyberauto.presentation.secretary.customers.CustomersScreen()
        }
        composable(SecretaryRoute.BookingRequests.route) {
            // Accessed via Schedule Tab now, but keeping route if needed for direct link
             com.example.zyberauto.presentation.secretary.bookings.BookingRequestsScreen(
                onNavigateToDetails = { bookingId ->
                    navController.navigate("sec_booking_details/$bookingId")
                }
            )
        }
        composable(
            route = "sec_booking_details/{bookingId}",
            arguments = listOf(androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.StringType })
        ) {
            com.example.zyberauto.presentation.secretary.bookings.BookingDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(SecretaryRoute.Inquiries.route) {
             com.example.zyberauto.presentation.secretary.chat.SecretaryChatListScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onNavigateToChat = { conversationId ->
                     navController.navigate("sec_chat/$conversationId")
                 }
             )
        }
        
        // Secretary Chat Route (real-time chat with customer)
        composable(
            route = "sec_chat/{conversationId}",
            arguments = listOf(androidx.navigation.navArgument("conversationId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            com.example.zyberauto.presentation.secretary.chat.SecretaryChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Direct chat route - finds or creates conversation with customer
        composable(
            route = "sec_chat_direct/{customerId}/{customerName}",
            arguments = listOf(
                androidx.navigation.navArgument("customerId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("customerName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            val customerName = backStackEntry.arguments?.getString("customerName") ?: ""
            
            com.example.zyberauto.presentation.secretary.chat.SecretaryChatDirectScreen(
                customerId = customerId,
                customerName = customerName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { conversationId ->
                    navController.navigate("sec_chat/$conversationId") {
                        popUpTo("sec_chat_direct/$customerId/$customerName") { inclusive = true }
                    }
                }
            )
        }

        composable("sec_reports_revenue") {
            com.example.zyberauto.presentation.secretary.reports.RevenueReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("sec_schedule_appointment") {
            com.example.zyberauto.presentation.secretary.appointments.ScheduleAppointmentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("sec_walkin") {
            com.example.zyberauto.presentation.secretary.walkin.WalkInScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(SecretaryRoute.Inventory.route) {
             com.example.zyberauto.presentation.secretary.inventory.InventoryScreen()
        }
        composable(SecretaryRoute.Settings.route) {
            com.example.zyberauto.presentation.secretary.settings.SecretarySettingsScreen(
                onLogout = onLogout,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

