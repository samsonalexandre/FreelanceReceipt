package com.alexandresamson.freelancereceipt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alexandresamson.freelancereceipt.ui.auth.AuthViewModel
import com.alexandresamson.freelancereceipt.ui.auth.LoginScreen
import com.alexandresamson.freelancereceipt.ui.auth.RegisterScreen
import com.alexandresamson.freelancereceipt.ui.biometric.BiometricLockScreen
import com.alexandresamson.freelancereceipt.ui.dashboard.DashboardScreen
import com.alexandresamson.freelancereceipt.ui.camera.CameraScreen
import com.alexandresamson.freelancereceipt.ui.addreceipt.AddReceiptScreen
import com.alexandresamson.freelancereceipt.ui.export.ExportScreen // NEU
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Login      : Screen("login")
    data object Register   : Screen("register")
    data object Biometric  : Screen("biometric")
    data object Dashboard  : Screen("dashboard")
    data object Camera     : Screen("camera")
    data object Export     : Screen("export") // NEU

    data object AddReceipt : Screen("add_receipt/{rawText}") {
        fun createRoute(rawText: String) =
            "add_receipt/${URLEncoder.encode(rawText, "UTF-8")}"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

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

        composable(Screen.Biometric.route) {
            BiometricLockScreen(
                onUnlocked = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Biometric.route) { inclusive = true }
                }}
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddClick    = { navController.navigate(Screen.Camera.route) },
                onExportClick = { navController.navigate(Screen.Export.route) }, // NEU
                onLogout      = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onTextRecognized = { rawText ->
                    navController.navigate(Screen.AddReceipt.createRoute(rawText))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddReceipt.route,
            arguments = listOf(navArgument("rawText") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("rawText") ?: ""
            val rawText = URLDecoder.decode(encoded, "UTF-8")

            AddReceiptScreen(
                rawOcrText = rawText,
                onSaved = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // NEU: Export Route
        composable(Screen.Export.route) {
            ExportScreen(onBack = { navController.popBackStack() })
        }
    }
}