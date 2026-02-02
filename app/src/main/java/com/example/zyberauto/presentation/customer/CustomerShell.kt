package com.example.zyberauto.presentation.customer

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
import com.example.zyberauto.presentation.customer.settings.CustomerSettingsScreen
import kotlinx.coroutines.launch

sealed class CustomerRoute(val route: String, val title: String, val icon: ImageVector) {
    object MyBookings : CustomerRoute("cus_bookings", "My Bookings", Icons.Default.CalendarToday)
    object MyComplaints : CustomerRoute("cus_complaints", "My Complaints", Icons.Default.Warning)
    object Settings : CustomerRoute("cus_settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerShell(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val customerNavController = rememberNavController()
    
    val navBackStackEntry by customerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        CustomerRoute.MyBookings,
        CustomerRoute.MyComplaints,
        CustomerRoute.Settings
    )

    fun navigateTo(route: String) {
        customerNavController.navigate(route) {
            popUpTo(customerNavController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    val drawerContent: @Composable () -> Unit = {
        ModalDrawerSheet {
            Spacer(Modifier.height(12.dp))
            NavigationDrawerItem(
                label = { Text("Customer Portal") },
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
        Row(modifier = Modifier.fillMaxSize()) {
            PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Customer Portal",
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
            
            Scaffold(
                topBar = {
                     CenterAlignedTopAppBar(
                        title = { 
                            Text(navigationItems.find { it.route == currentRoute }?.title ?: "Customer Portal")
                        },
                         colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            ) { paddingValues ->
                CustomerNavHost(customerNavController, paddingValues)
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = drawerContent
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(navigationItems.find { it.route == currentRoute }?.title ?: "Customer Portal")
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            ) { paddingValues ->
                CustomerNavHost(customerNavController, paddingValues)
            }
        }
    }
}

@Composable
fun CustomerNavHost(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = CustomerRoute.MyBookings.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(CustomerRoute.MyBookings.route) {
            com.example.zyberauto.presentation.customer.bookings.CustomerBookingsScreen(
                onNavigateToNewBooking = {
                    // Navigate to root nav controller for full screen or nested?
                    // Let's use root nav for now or handle inside shell if we want to hide bottom bar
                    // For simplicity in shell, we can just push to shell nav, but shell has tabs.
                    // Better to put "New Booking" inside the shell nav graph but maybe hide drawer?
                    // Or keep it simple: Replace current view.
                    navController.navigate("new_booking")
                }
            )
        }
        composable("new_booking") {
            com.example.zyberauto.presentation.customer.bookings.NewBookingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(CustomerRoute.MyComplaints.route) {
            com.example.zyberauto.presentation.customer.complaints.CustomerComplaintsScreen(
                onNavigateToFileComplaint = {
                    navController.navigate("file_complaint")
                }
            )
        }
        composable("file_complaint") {
            com.example.zyberauto.presentation.customer.complaints.FileComplaintScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(CustomerRoute.Settings.route) {
            CustomerSettingsScreen()
        }
    }
}
