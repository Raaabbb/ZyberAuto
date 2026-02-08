package com.example.zyberauto.presentation.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zyberauto.presentation.common.components.AppHeader
import com.example.zyberauto.presentation.customer.settings.CustomerSettingsScreen
import com.example.zyberauto.ui.theme.BackgroundDark
import com.example.zyberauto.ui.theme.PrimaryRed

/**
 * Customer navigation routes.
 */
sealed class CustomerRoute(val route: String, val title: String, val icon: ImageVector) {
    object MyBookings : CustomerRoute("cus_bookings", "BOOKINGS", Icons.Default.CalendarToday)
    object MyCars : CustomerRoute("cus_cars", "MY CARS", Icons.Default.DirectionsCar)
    object MyChats : CustomerRoute("cus_chats", "CHATS", Icons.Default.Chat)
    object Settings : CustomerRoute("cus_settings", "PROFILE", Icons.Default.Person)
}

/**
 * CustomerShell - Main container for the customer interface with unified header.
 */
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

    fun navigateTo(route: String) {
        customerNavController.navigate(route) {
            popUpTo(customerNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val backgroundColor = Color(0xFFF5F5F7)
    
    // Determine if we should show header (hide on detail screens)
    val showHeader = currentRoute?.startsWith("cus_chat/") != true &&
                     currentRoute != "cus_chat_new" &&
                     currentRoute != "new_booking" &&
                     currentRoute != "add_car"

    Scaffold(containerColor = backgroundColor) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Unified AppHeader (no settings button for customer)
                if (showHeader) {
                    AppHeader(
                        subtitle = "CUSTOMER"
                        // No onSettingsClick = no settings icon
                    )
                }
                
                // Content
                Box(modifier = Modifier.weight(1f)) {
                    CustomerNavHost(
                        navController = customerNavController,
                        onLogout = onLogout
                    )
                }
            }
            
            // Bottom Nav (hide on chat and detail screens)
            if (currentRoute?.startsWith("cus_chat/") != true && currentRoute != "cus_chat_new") {
                CustomerBottomNav(
                    currentRoute = currentRoute,
                    onItemClick = { navigateTo(it.route) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/**
 * CustomerBottomNav - Dark floating bottom navigation bar.
 */
@Composable
fun CustomerBottomNav(
    currentRoute: String?,
    onItemClick: (CustomerRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = BackgroundDark,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerNavBarItem(
                selected = currentRoute == CustomerRoute.MyBookings.route,
                icon = Icons.Default.CalendarMonth,
                label = "BOOK",
                onClick = { onItemClick(CustomerRoute.MyBookings) }
            )
            
            CustomerNavBarItem(
                selected = currentRoute == CustomerRoute.MyCars.route,
                icon = Icons.Default.DirectionsCar,
                label = "CARS",
                onClick = { onItemClick(CustomerRoute.MyCars) }
            )
            
            CustomerNavBarItem(
                selected = currentRoute?.startsWith("cus_chat") == true || currentRoute == CustomerRoute.MyChats.route,
                icon = Icons.Default.Chat,
                label = "CHAT",
                onClick = { onItemClick(CustomerRoute.MyChats) }
            )
            
            CustomerNavBarItem(
                selected = currentRoute == CustomerRoute.Settings.route,
                icon = Icons.Default.Person,
                label = "ME",
                onClick = { onItemClick(CustomerRoute.Settings) }
            )
        }
    }
}

@Composable
fun CustomerNavBarItem(
    selected: Boolean, 
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit
) {
    val color = if (selected) PrimaryRed else Color.White.copy(alpha = 0.4f)
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            fontSize = 10.sp, 
            color = color
        )
    }
}

/**
 * CustomerNavHost - Navigation graph for customer screens.
 */
@Composable
fun CustomerNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = CustomerRoute.MyBookings.route
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
        
        composable(CustomerRoute.MyCars.route) {
            com.example.zyberauto.presentation.customer.cars.MyCarsScreen(
                onNavigateToAddCar = {
                    navController.navigate("add_car")
                }
            )
        }
        
        composable("add_car") {
            com.example.zyberauto.presentation.customer.cars.AddCarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(CustomerRoute.MyChats.route) {
            com.example.zyberauto.presentation.customer.chat.CustomerChatEntry(
                onNavigateToChat = { conversationId ->
                    navController.navigate("cus_chat/$conversationId") {
                         popUpTo(CustomerRoute.MyChats.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable("cus_chat_new") {
            com.example.zyberauto.presentation.customer.chat.NewConversationScreen(
                onNavigateBack = { navController.popBackStack() },
                onConversationCreated = { conversationId ->
                    navController.popBackStack()
                    navController.navigate("cus_chat/$conversationId")
                }
            )
        }
        
        composable(
            route = "cus_chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            com.example.zyberauto.presentation.customer.chat.ChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(CustomerRoute.Settings.route) {
            CustomerSettingsScreen(onLogout = onLogout)
        }
    }
}
