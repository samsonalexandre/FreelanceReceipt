package com.alexandresamson.freelancereceipt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alexandresamson.freelancereceipt.ui.auth.AuthViewModel
import com.alexandresamson.freelancereceipt.ui.auth.LoginScreen
import com.alexandresamson.freelancereceipt.ui.auth.RegisterScreen
import com.alexandresamson.freelancereceipt.ui.biometric.BiometricLockScreen
import com.alexandresamson.freelancereceipt.ui.dashboard.DashboardScreen
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Login      : Screen("login")
    data object Register   : Screen("register")
    data object Biometric  : Screen("biometric")
    data object Dashboard  : Screen("dashboard")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Startpunkt je nach Login-Status
    val startDestination = if (authState.isLoggedIn) Screen.Biometric.route
    else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess  = { navController.navigate(Screen.Biometric.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Biometric.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Biometrie-Gate — nur passierbar, wenn entsperrt
        composable(Screen.Biometric.route) {
            BiometricLockScreen(
                onUnlocked = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Biometric.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddClick  = { /* später: navigate to AddReceipt */ },
                onLogout    = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}