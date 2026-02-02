package com.example.zyberauto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zyberauto.presentation.auth.LoginScreen
import com.example.zyberauto.presentation.auth.RegisterScreen

import com.example.zyberauto.presentation.secretary.SecretaryDashboardScreen

sealed class Route(val route: String) {
    object Login : Route("login")
    object Register : Route("register")
    object CustomerDashboard : Route("customer_dashboard")
    object CustomerNewBooking : Route("customer_new_booking")
    object CustomerFileComplaint : Route("customer_file_complaint")
    object SecretaryDashboard : Route("secretary_dashboard")
    object DebugSeeder : Route("debug_seeder")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "SECRETARY") {
                        navController.navigate(Route.SecretaryDashboard.route) {
                            popUpTo(Route.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.CustomerDashboard.route) {
                            popUpTo(Route.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Route.DebugSeeder.route)
                }
            )
        }
        
        composable(Route.DebugSeeder.route) {
            com.example.zyberauto.presentation.debug.DebugSeederScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Route.CustomerDashboard.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Route.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Route.SecretaryDashboard.route) {
            com.example.zyberauto.presentation.secretary.SecretaryShell(
                rootNavController = navController,
                onLogout = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
        
        composable(Route.CustomerDashboard.route) {
           com.example.zyberauto.presentation.customer.CustomerShell(
               rootNavController = navController,
               onLogout = {
                   navController.navigate(Route.Login.route) {
                       popUpTo(0) // Clear back stack
                   }
               }
           )
        }
    }
}
