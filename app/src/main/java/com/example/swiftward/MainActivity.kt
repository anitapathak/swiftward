package com.example.swiftward

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.navigation.Screen
import com.example.swiftward.ui.viewmodel.AuthViewModel
import com.example.swiftward.ui.screens.*
import com.swiftward.ui.theme.SwiftWardTheme
import com.swiftward.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// Import your screens if they are in a different package
import com.example.swiftward.ui.screens.RegisterScreen
import com.example.swiftward.ui.screens.OtpScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwiftWardTheme {
                SwiftWardAppNavigation(sessionManager)
            }
        }
    }
}

@Composable
fun SwiftWardAppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()

    // Collect the login state from DataStore
    // initial = null helps us wait for the real value from disk
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

    // Wait until we know the login status before setting up the NavHost
    if (isLoggedIn != null) {
        NavHost(
            navController = navController,
            // Logic: If token exists, go straight to Hospitals. Otherwise, show Splash/Login.
            startDestination = if (isLoggedIn == true) Screen.Hospitals.route else Screen.Splash.route
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }

            // 2. Login Screen
            composable(Screen.Login.route) {
                // Using hiltViewModel() ensures the ViewModel is lifecycle-aware
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    navController = navController,
                    onNavigateToRegister = { navController.navigate("register_route") }
                )
            }

            // 3. Register Screen
            composable(route = "register_route") {
                RegisterScreen(
                    navController = navController,
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // 4. OTP Screen
            composable(
                route = "otp_screen/{phone}", // The /{phone} is a variable
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                // Extract the phone number passed from the Register screen
                val phone = backStackEntry.arguments?.getString("phone") ?: ""

                OtpScreen(
                    navController = navController,
                    phoneNumber = phone
                )
            }

            // 5. Main Browser / Hospital List (The screen after Login)
            // 5. Main Browser / Hospital List
            composable(Screen.Hospitals.route) {
                // 1. Initialize the ViewModel here
                val authViewModel: AuthViewModel = hiltViewModel()

                // 2. Now you can pass it
                // CORRECT (Line 110): Match the new signature
                HospitalsScreen(
                    onHospitalClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onMapClick = { navController.navigate(Screen.map.route) }
                )
            }
        }
    }
}