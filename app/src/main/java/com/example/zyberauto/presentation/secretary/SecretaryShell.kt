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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
        SecretaryRoute.Reports
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

    Scaffold(
        topBar = {
            if (currentRoute != SecretaryRoute.Settings.route && currentRoute != SecretaryRoute.Dashboard.route) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(bottomNavItems.find { it.route == currentRoute }?.title ?: "Secretary Portal")
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = { 
                            if (currentRoute == SecretaryRoute.Settings.route) {
                                secretaryNavController.popBackStack()
                            } else {
                                secretaryNavController.navigate(SecretaryRoute.Settings.route) {
                                    launchSingleTop = true 
                                }
                            }
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        // Removed standard bottomBar
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content
            SecretaryNavHost(
                navController = secretaryNavController, 
                paddingValues = PaddingValues(bottom = 100.dp), // Add space for floating bar
                onLogout = onLogout
            )
            
            // Fixed Bottom Nav + FAB
            if (currentRoute != SecretaryRoute.Settings.route) {
                // FAB
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp) // Sits above the bar (height 80dp approx)
                        .zIndex(2f)
                ) {
                    CenterFab(onClick = { secretaryNavController.navigate("sec_walkin") })
                }
                
                // Bottom Bar
                FixedBottomNav(
                    currentRoute = currentRoute,
                    onItemClick = { navigateTo(it.route) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun CenterFab(onClick: () -> Unit) {
    // Red Glow Shadow effect handled by Box or custom modifier if needed, simplifying for standard FAB first
    // User asked for "Shadow-[0_0_30px_#d41111]"
    // We can simulate with a Box behind it.
    Box(contentAlignment = Alignment.Center) {
        // Glow
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(com.example.zyberauto.ui.theme.PrimaryRed.copy(alpha=0.4f), androidx.compose.foundation.shape.CircleShape)
                .blur(16.dp)
        )
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(com.example.zyberauto.ui.theme.PrimaryRed, androidx.compose.foundation.shape.CircleShape)
                .border(2.dp, androidx.compose.ui.graphics.Color.White.copy(alpha=0.1f), androidx.compose.foundation.shape.CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun FixedBottomNav(
    currentRoute: String?,
    onItemClick: (SecretaryRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(96.dp), // h-24 = 96px ~ 96dp? Actually h-24 is 6rem = 96px.
        color = com.example.zyberauto.ui.theme.BackgroundDark.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f)), // Border Top conceptually
        // Real border only top? For now full border is fine or drawBehind.
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp), // pb-6
            horizontalArrangement = Arrangement.SpaceAround, // Space Around handles the gaps
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
            
            // GAP
             Spacer(modifier = Modifier.width(64.dp))
             
            // Operations (TECHS)
            NavBarItem(
                selected = currentRoute == SecretaryRoute.Operations.route,
                icon = Icons.Default.PrecisionManufacturing,
                label = "TECHS",
                onClick = { onItemClick(SecretaryRoute.Operations) }
            )
            
            // Reports (CLIENTS/STATS)
            NavBarItem(
                selected = currentRoute == SecretaryRoute.Reports.route,
                icon = Icons.Default.Analytics, // Or SettingsAccessibility if mapped to clients
                label = "STATS",
                onClick = { onItemClick(SecretaryRoute.Reports) }
            )
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
                onNavigateToPending = { navController.navigate(SecretaryRoute.BookingRequests.route) },
                onNavigateToInquiries = { navController.navigate(SecretaryRoute.Inquiries.route) }
            )
        }
        
        // Schedule Tab
        composable(SecretaryRoute.Schedule.route) {
            SecretaryScheduleWrapperScreen(
                onNavigateToSchedule = { /* Already here */ },
                onNavigateToWalkIn = { navController.navigate("sec_walkin") },
                onNavigateToBookingDetails = { bookingId ->
                    navController.navigate("sec_booking_details/$bookingId")
                }
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
             com.example.zyberauto.presentation.secretary.inquiries.InquiriesScreen()
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

