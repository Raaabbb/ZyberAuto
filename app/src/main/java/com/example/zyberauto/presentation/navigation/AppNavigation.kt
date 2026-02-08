package com.example.zyberauto.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zyberauto.presentation.auth.LoginScreen
import com.example.zyberauto.presentation.auth.RegisterScreen
import com.example.zyberauto.presentation.splash.SplashScreen

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Login : Route("login")
    object Register : Route("register")
    object CustomerDashboard : Route("customer_dashboard")
    object SecretaryDashboard : Route("secretary_dashboard")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Splash.route) {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Route.CustomerDashboard.route) {
           com.example.zyberauto.presentation.customer.CustomerShell(
               rootNavController = navController,
               onLogout = {
                   navController.navigate(Route.Login.route) {
                       popUpTo(navController.graph.id) { inclusive = true }
                   }
               }
           )
        }
    }
}
