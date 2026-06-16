package com.alexandresamson.freelancereceipt.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.alexandresamson.freelancereceipt.ui.camera.CameraScreen
import com.alexandresamson.freelancereceipt.ui.addreceipt.AddReceiptScreen
import com.alexandresamson.freelancereceipt.ui.dashboard.DashboardScreen
import com.alexandresamson.freelancereceipt.ui.detail.DetailReceiptScreen
import com.alexandresamson.freelancereceipt.ui.export.ExportScreen
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Login      : Screen("login")
    data object Register   : Screen("register")
    data object Biometric  : Screen("biometric")
    data object Dashboard  : Screen("dashboard")
    data object Camera     : Screen("camera")
    data object AddReceipt : Screen("add_receipt") // ← kein Parameter mehr in der Route
    data object Export     : Screen("export")

    data object Detail : Screen("detail/{receiptId}") {
        fun createRoute(id: Long) = "detail/$id"
    }
}

// Einfacher In-Memory-Speicher für den OCR-Text
// Sicherer als URL-Parameter bei Sonderzeichen
object OcrResultHolder {
    var rawText: String = ""
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: return@composable
            DetailReceiptScreen(
                receiptId = receiptId,
                onBack    = { navController.popBackStack() },
                onDeleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Biometric.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Biometric.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Biometric.route) {
            BiometricLockScreen(
                onUnlocked = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Biometric.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val authViewModel: AuthViewModel = koinViewModel()
            DashboardScreen(
                onAddClick    = { navController.navigate(Screen.Camera.route) },
                onExportClick = { navController.navigate(Screen.Export.route) },
                onItemClick   = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
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
                    // Text im Holder speichern, NICHT als URL-Parameter
                    OcrResultHolder.rawText = rawText
                    navController.navigate(Screen.AddReceipt.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddReceipt.route) {
            AddReceiptScreen(
                // Text aus dem Holder lesen — keine URL-Dekodierung nötig
                rawOcrText = OcrResultHolder.rawText,
                onSaved = {
                    OcrResultHolder.rawText = "" // aufräumen
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onBack = {
                    OcrResultHolder.rawText = "" // aufräumen
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Export.route) {
            ExportScreen(onBack = { navController.popBackStack() })
        }
    }
}