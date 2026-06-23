package com.example.swiftward.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.swiftward.ui.screens.*
import com.example.swiftward.ui.viewmodel.AuthViewModel
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.data.model.Hospital
import com.swiftward.utils.SessionManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager
) {
    // Shared Hilt ViewModel instance to maintain local in-memory booking history across states
    val bookingViewModel: BookingViewModel = hiltViewModel()

    // UI state collection to pass complete data down to details/confirmation screens if needed
    val uiState by bookingViewModel.uiState.collectAsState()

    // Read the DataStore login session status flow asynchronously
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

    // Wait until Jetpack DataStore returns a true or false snapshot from disk
    if (isLoggedIn != null) {
        NavHost(
            navController = navController,
            // If logged in, go straight to home dashboard. If not, hit Login form
            startDestination = if (isLoggedIn == true) Screen.Hospitals.route else Screen.Login.route
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }

            // 2. Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    navController = navController,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // 3. Register Screen
            composable(Screen.Register.route) {
                RegisterScreen(
                    navController = navController,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegistrationSuccess = { phone, email ->
                        navController.navigate("otp_screen/$phone/$email") {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
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

                OtpScreen(navController = navController, phone = phone, email = email)
            }

            // 5. Hospitals Home Screen
            composable(Screen.Hospitals.route) {
                HospitalsScreen(
                    onHospitalClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onMapClick = { navController.navigate(Screen.map.route) }
                )
            }

            // 6. Map Screen
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

            // 7. Bookings List Screen (Binds local lists dynamically from your viewmodel)
            composable(Screen.MyBookings.route) {
                BookingsScreen(
                    viewModel = bookingViewModel, // ✅ Compiles perfectly now!
                    onHospitalsClick = {
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = true }
                        }
                    },
                    onMapClick = {
                        navController.navigate(Screen.map.route) {
                            popUpTo(Screen.Hospitals.route)
                        }
                    },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            // 8. Profile Screen
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
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 9. Hospital Detail Screen
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
                HospitalDetailScreen(
                    hospitalId = hospitalId,
                    onBack = { navController.popBackStack() },
                    onEmergencyBook = { id ->
                        navController.navigate(Screen.EmergencyBook.createRoute(id))
                    }
                )
            }

            // 10. Emergency Booking Screen Form
            composable(
                route = Screen.EmergencyBook.route,
                arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
                val targetedName = if (hospitalId == "h1") "Bir Hospital" else "Selected Hospital"

                EmergencyBookingScreen(
                    hospitalId = hospitalId,
                    onBack = { navController.popBackStack() },
                    onBookingConfirmed = { dynamicRequest ->

                        val hospitalPayload = Hospital(
                            id = hospitalId,
                            name = targetedName,
                            address = "Kathmandu, Nepal",
                            latitude = 27.7000,
                            longitude = 85.3200,
                            phone = "01-4221111",
                            isOpen24x7 = true,
                            wards = emptyList()
                        )

                        // Commit form entry directly into the memory system list flow
                        bookingViewModel.submitBooking(
                            request = dynamicRequest,
                            hospital = hospitalPayload
                        )

                        // Safely pull the generated ID from the viewmodel state machine logic
                        val generatedId = uiState.booking?.bookingId ?: "SW-GENERATING"

                        // Route to payment stage carrying dynamic references
                        navController.navigate("payment_screen/$generatedId/$hospitalId")
                    }
                )
            }

            // 11. Emergency Payment Selector Screen Layout
            composable(
                route = "payment_screen/{bookingId}/{hospitalId}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.StringType },
                    navArgument("hospitalId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""

                EmergencyPaymentScreen(
                    bookingId = bookingId,
                    hospitalName = if (hospitalId == "h1") "Bir Hospital" else "Selected Hospital",
                    bedType = "ICU Bed",
                    onBack = { navController.popBackStack() },
                    onPaymentFailure = {},
                    onPaymentSuccess = { generatedTxId ->
                        // Proceed directly to receipt confirmation screen
                        navController.navigate("confirmation_route/$bookingId/$generatedTxId") {
                            popUpTo(Screen.Hospitals.route)
                        }
                    }
                )
            }
// 12. Booking Confirmation Destination Screen
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
                    bookingId = bookingId,       // ✅ Pass the parsed string ID parameter
                    transactionId = transactionId, // ✅ Pass the parsed string transaction reference
                    onBackToHospitals = {          // ✅ Match the exact lambda parameter name expected by your screen
                        bookingViewModel.clearSuccess()
                        navController.navigate(Screen.Hospitals.route) {
                            popUpTo(Screen.Hospitals.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}