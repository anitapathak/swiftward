package com.example.swiftward

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import com.example.swiftward.ui.screens.RegisterScreen
import com.example.swiftward.ui.screens.OtpScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwiftWardAppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()

    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

    if (isLoggedIn != null) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) Screen.Hospitals.route else Screen.Splash.route
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }

            // 2. Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    onNavigateToRegister = { navController.navigate("register_route") }
                )
            }

            // 3. Register Screen
            composable(route = "register_route") {
                RegisterScreen(
                    navController = navController,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // 4. OTP Screen
            composable(
                route = "otp_screen/{phone}/{email}",
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                val email = backStackEntry.arguments?.getString("email") ?: ""

                // phone verifies the OTP against the backend; email is shown to the user
                OtpScreen(navController = navController, phone = phone, email = email)
            }

            // 5. Main Browser / Hospital List
            composable(Screen.Hospitals.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                HospitalsScreen(
                    onHospitalClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onMapClick = { navController.navigate(Screen.map.route) }
                )
            }

            // 5b. Map Screen Destination
            composable(Screen.map.route) {
                MapScreen(
                    onHospitalClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onHospitalsClick = {
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = true }
                        }
                    },
                    onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            // 5c. My Bookings Screen Destination
            composable(Screen.MyBookings.route) {
                BookingsScreen(
                    onHospitalsClick = {
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = true }
                        }
                    },
                    onMapClick = { navController.navigate(Screen.map.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            // 5d. Profile Screen Destination (with working Logout)
            composable(Screen.Profile.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val profileName by sessionManager.userName.collectAsState(initial = "")
                val profilePhone by sessionManager.userPhone.collectAsState(initial = "")
                ProfileScreen(
                    userName = profileName ?: "",
                    userPhone = profilePhone ?: "",
                    onBack = { navController.popBackStack() },
                    onHospitalsClick = {
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = true }
                        }
                    },
                    onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            // Wipe the whole back stack so back-button can't return to a logged-in screen
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 6. Hospital Detail Screen Destination
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
                HospitalDetailScreen(
                    hospitalId = hospitalId,
                    onBack = { navController.popBackStack() },
                    onEmergencyBook = { id -> navController.navigate("booking_route/$id") }
                )
            }

            // 7. Emergency Booking Screen Destination
            composable(
                route = "booking_route/{hospitalId}",
                arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""

                EmergencyBookingScreen(
                    hospitalId = hospitalId,
                    onBack = { navController.popBackStack() },
                    onBookingConfirmed = { bookingId ->
                        // Dynamically route forward to the payment terminal, passing along the generated booking reference
                        navController.navigate("payment_route/$bookingId/$hospitalId")
                    }
                )
            }

            // 8. NEW: Emergency Payment Screen Destination
            composable(
                route = "payment_route/{bookingId}/{hospitalId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("hospitalId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""

                EmergencyPaymentScreen(
                    bookingId = bookingId,
                    bedType = "General Ward", // Or pass dynamically based on selection states
                    hospitalName = if (hospitalId == "h1") "Bir Hospital" else "Hospital Partner",
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = { referenceTxId ->
                        // Drop both the booking form and the payment view from backstack, then transition to confirmation
                        navController.navigate("confirmation_route/$bookingId/$referenceTxId") {
                            popUpTo("booking_route/{hospitalId}") { inclusive = true }
                        }
                    },
                    onPaymentFailure = { errorMsg ->
                        // Optional handler context logic (e.g. tracking or custom notifications)
                    }
                )
            }

            // 9. Booking Confirmation Screen Destination
            composable(
                route = "confirmation_route/{bookingId}/{transactionId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("transactionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""

                BookingConfirmScreen(
                    bookingId = bookingId,
                    transactionId = transactionId,
                    onBackToHospitals = {
                        // Clear out tracking targets and safely return home to the main tracking view dashboard
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}