package com.example.zyberauto.presentation.secretary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

sealed class SecretaryRoute(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : SecretaryRoute("sec_dashboard", "Dashboard", Icons.Default.Dashboard)
    object Appointments : SecretaryRoute("sec_appointments", "Appointments", Icons.Default.CalendarToday)
    object Customers : SecretaryRoute("sec_customers", "Customers", Icons.Default.People)
    object BookingRequests : SecretaryRoute("sec_requests", "Requests", Icons.Default.Assignment)
    object Inquiries : SecretaryRoute("sec_inquiries", "Inquiries", Icons.Default.Chat)
    object Reports : SecretaryRoute("sec_reports", "Reports", Icons.Default.BarChart)
    object Inventory : SecretaryRoute("sec_inventory", "Inventory", Icons.Default.Inventory)
    object Settings : SecretaryRoute("sec_settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryShell(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val secretaryNavController = rememberNavController()
    
    val navBackStackEntry by secretaryNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        SecretaryRoute.Dashboard,
        SecretaryRoute.Appointments,
        SecretaryRoute.Customers,
        SecretaryRoute.Inquiries,
        SecretaryRoute.Reports,
        SecretaryRoute.Inventory,
        SecretaryRoute.Settings
    )

    fun navigateTo(route: String) {
        secretaryNavController.navigate(route) {
            popUpTo(secretaryNavController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    val drawerContent: @Composable () -> Unit = {
        ModalDrawerSheet {
            Spacer(Modifier.height(12.dp))
            NavigationDrawerItem(
                label = { Text("ZyberAuto Staff") },
                selected = false,
                onClick = { },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            navigationItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    onClick = {
                        navigateTo(item.route)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            
            Spacer(Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ExitToApp, "Log Out") },
                label = { Text("Log Out") },
                selected = false,
                onClick = onLogout,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp.dp >= 600.dp

    if (isWideScreen) {
        // Tablet/Desktop: Permanent Sidebar
        Row(modifier = Modifier.fillMaxSize()) {
            PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "ZyberAuto Staff",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                navigationItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = { navigateTo(item.route) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, "Log Out") },
                    label = { Text("Log Out") },
                    selected = false,
                    onClick = onLogout,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(Modifier.height(12.dp))
            }
            
            // Content Content
            Scaffold(
                topBar = {
                    // Optional: No hamburger menu needed for wide screen
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(navigationItems.find { it.route == currentRoute }?.title ?: "Secretary Portal")
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            ) { paddingValues ->
                SecretaryNavHost(secretaryNavController, paddingValues)
            }
        }
    } else {
        // Mobile: Modal Drawer with Hamburger
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = drawerContent
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(navigationItems.find { it.route == currentRoute }?.title ?: "Secretary Portal")
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            ) { paddingValues ->
                SecretaryNavHost(secretaryNavController, paddingValues)
            }
        }
    }
}

@Composable
fun SecretaryNavHost(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = SecretaryRoute.Dashboard.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(SecretaryRoute.Dashboard.route) {
            SecretaryDashboardScreen(
                onNavigateToSchedule = { navController.navigate("sec_schedule_appointment") },
                onNavigateToWalkIn = { navController.navigate("sec_walkin") }
            )
        }
        composable(SecretaryRoute.Appointments.route) {
            com.example.zyberauto.presentation.secretary.SecretaryAppointmentsScreen(
                 onNavigateToSchedule = { navController.navigate("sec_schedule_appointment") },
                 onNavigateToWalkIn = { navController.navigate("sec_walkin") }
            )
        }
        composable(SecretaryRoute.Customers.route) {
            com.example.zyberauto.presentation.secretary.customers.CustomersScreen()
        }
        composable(SecretaryRoute.BookingRequests.route) {
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
        composable(SecretaryRoute.Reports.route) {
             com.example.zyberauto.presentation.secretary.reports.ReportsScreen(
                 onNavigateToRevenue = {
                     navController.navigate("sec_reports_revenue")
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
            com.example.zyberauto.presentation.secretary.settings.SecretarySettingsScreen()
        }
    }
}
