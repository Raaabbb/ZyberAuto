package com.example.zyberauto.presentation.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zyberauto.presentation.customer.settings.CustomerSettingsScreen

sealed class CustomerRoute(val route: String, val title: String, val icon: ImageVector) {
    object MyBookings : CustomerRoute("cus_bookings", "My Bookings", Icons.Default.CalendarToday)
    object MyInquiries : CustomerRoute("cus_inquiries", "My Inquiries", Icons.Default.QuestionAnswer)
    object Settings : CustomerRoute("cus_settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerShell(
    rootNavController: NavHostController,
    onLogout: () -> Unit
) {
    val customerNavController = rememberNavController()
    
    val navBackStackEntry by customerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val navigationItems = listOf(
        CustomerRoute.MyBookings,
        CustomerRoute.MyInquiries,
        CustomerRoute.Settings
    )

    fun navigateTo(route: String) {
        customerNavController.navigate(route) {
            popUpTo(customerNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(navigationItems.find { it.route == currentRoute }?.title ?: "Customer Portal")
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Clean look
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    // Optional: Logout or other actions in top bar if needed, 
                    // or keep logout in Settings.
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                navigationItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = { navigateTo(item.route) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        CustomerNavHost(customerNavController, paddingValues, onLogout)
    }
}

@Composable
fun CustomerNavHost(
    navController: NavHostController, 
    paddingValues: PaddingValues, 
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = CustomerRoute.MyBookings.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(CustomerRoute.MyBookings.route) {
            com.example.zyberauto.presentation.customer.bookings.CustomerBookingsScreen(
                onNavigateToNewBooking = {
                    navController.navigate("new_booking")
                }
            )
        }
        composable("new_booking") {
            com.example.zyberauto.presentation.customer.bookings.NewBookingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(CustomerRoute.MyInquiries.route) {
            com.example.zyberauto.presentation.customer.inquiries.CustomerInquiriesScreen(
                onNavigateToFileInquiry = {
                    navController.navigate("file_inquiry")
                }
            )
        }
        composable("file_inquiry") {
            com.example.zyberauto.presentation.customer.inquiries.FileInquiryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(CustomerRoute.Settings.route) {
            CustomerSettingsScreen(onLogout = onLogout)
        }
    }
}

