package com.example.swiftward.ui.navigation

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.swiftward.ui.screens.*
import com.example.swiftward.ui.viewmodel.AuthViewModel
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.data.model.Hospital
import com.swiftward.utils.SessionManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    sessionManager: SessionManager,
    khaltiCallbackIntent: Intent? = null
) {
    val bookingViewModel: BookingViewModel = hiltViewModel()
    val uiState    by bookingViewModel.uiState.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)
    val userPhone  by sessionManager.userPhone.collectAsState(initial = "")
    val userName   by sessionManager.userName.collectAsState(initial = "")

    if (isLoggedIn == null) return   // wait for DataStore

    // ── Handle Khalti deep-link return ────────────────────────────────────────
    // URL: swiftward://payment/callback?pidx=xxx&status=Completed&purchase_order_id=SW-xxx
    LaunchedEffect(khaltiCallbackIntent) {
        khaltiCallbackIntent?.data?.takeIf { it.scheme == "swiftward" && it.host == "payment" }?.let { uri ->
            val pidx      = uri.getQueryParameter("pidx")               ?: return@let
            val status    = uri.getQueryParameter("status")             ?: ""
            val bookingId = uri.getQueryParameter("purchase_order_id")  ?: ""
            if (status == "Completed" && bookingId.isNotBlank()) {
                navController.navigate("khalti_callback/$pidx/$bookingId") {
                    popUpTo(Screen.Hospitals.route)
                }
            } else {
                navController.navigate(Screen.Hospitals.route) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    NavHost(
        navController  = navController,
        startDestination = Screen.Splash.route
    ) {

        // ── Splash ───────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // ── Login ────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                navController        = navController,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // ── Register ─────────────────────────────────────────────────────────
        composable(Screen.Register.route) {
            RegisterScreen(
                navController     = navController,
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

        // ── OTP — on success → Login (NOT dashboard) ─────────────────────────
        composable(
            route = "otp_screen/{phone}/{email}",
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType }
            )
        ) { back ->
            OtpScreen(
                navController = navController,
                phone = back.arguments?.getString("phone") ?: "",
                email = back.arguments?.getString("email") ?: ""
            )
        }

        // ── Hospitals ─────────────────────────────────────────────────────────
        composable(Screen.Hospitals.route) {
            HospitalsScreen(
                onHospitalClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) },
                onMapClick      = { navController.navigate(Screen.map.route) }
            )
        }

        // ── Map ───────────────────────────────────────────────────────────────
        composable(Screen.map.route) {
            MapScreen(
                onHospitalClick  = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onHospitalsClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                },
                onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) }
            )
        }

        // ── My Bookings ───────────────────────────────────────────────────────
        composable(Screen.MyBookings.route) {
            BookingsScreen(
                viewModel        = bookingViewModel,
                onHospitalsClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                },
                onMapClick     = { navController.navigate(Screen.map.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ProfileScreen(
                userName  = userName ?: "",
                userPhone = userPhone ?: "",
                onBack    = { navController.popBackStack() },
                onHospitalsClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                },
                onBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ── Hospital Detail ───────────────────────────────────────────────────
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
        ) { back ->
            HospitalDetailScreen(
                hospitalId = back.arguments?.getString("hospitalId") ?: "",
                onBack     = { navController.popBackStack() },
                onEmergencyBook = { id ->
                    if (isLoggedIn == true) navController.navigate(Screen.EmergencyBook.createRoute(id))
                    else navController.navigate(Screen.Login.route)
                }
            )
        }

        // ── Emergency Booking Form ────────────────────────────────────────────
        composable(
            route = Screen.EmergencyBook.route,
            arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
        ) { back ->
            val hospitalId = back.arguments?.getString("hospitalId") ?: ""
            EmergencyBookingScreen(
                hospitalId = hospitalId,
                onBack     = { navController.popBackStack() },
                onBookingConfirmed = { request ->
                    val hospital = Hospital(
                        id = hospitalId, name = "Selected Hospital",
                        address = "Kathmandu, Nepal",
                        latitude = 27.7000, longitude = 85.3200,
                        phone = "01-4221111", isOpen24x7 = true, wards = emptyList()
                    )
                    bookingViewModel.submitBooking(request = request, hospital = hospital)
                    val bookingId = uiState.booking?.bookingId ?: "SW-PENDING"
                    navController.navigate("payment_screen/$bookingId/$hospitalId")
                }
            )
        }

        // ── Payment Screen ────────────────────────────────────────────────────
        composable(
            route = "payment_screen/{bookingId}/{hospitalId}",
            arguments = listOf(
                navArgument("bookingId")  { type = NavType.StringType },
                navArgument("hospitalId") { type = NavType.StringType }
            )
        ) { back ->
            val bookingId  = back.arguments?.getString("bookingId")  ?: ""
            val hospitalId = back.arguments?.getString("hospitalId") ?: ""
            EmergencyPaymentScreen(
                bookingId    = bookingId,
                hospitalName = uiState.booking?.hospitalName ?: "Selected Hospital",
                bedType      = uiState.booking?.wardType?.displayName ?: "ICU Bed",
                isLoggedIn   = isLoggedIn == true,
                onBack       = { navController.popBackStack() },
                onPaymentFailure = { /* error shown inside screen */ },
                onPaymentSuccess = { txId ->
                    bookingViewModel.attachTransactionId(bookingId, txId)
                    navController.navigate("confirmation_route/$bookingId/$txId") {
                        popUpTo(Screen.Hospitals.route)
                    }
                }
            )
        }

        // ── Khalti Callback (deep-link return) ────────────────────────────────
        // Khalti redirects to swiftward://payment/callback after user pays.
        // We extract pidx + bookingId and verify with /api/khalti/verify.
        composable(
            route = "khalti_callback/{pidx}/{bookingId}",
            arguments = listOf(
                navArgument("pidx")      { type = NavType.StringType },
                navArgument("bookingId") { type = NavType.StringType }
            )
        ) { back ->
            val pidx      = back.arguments?.getString("pidx")      ?: ""
            val bookingId = back.arguments?.getString("bookingId") ?: ""
            val booking   = uiState.booking

            KhaltiCallbackScreen(
                pidx         = pidx,
                bookingId    = bookingId,
                hospitalName = booking?.hospitalName ?: "Hospital",
                wardType     = booking?.wardType?.displayName ?: "Ward",
                onSuccess    = { txId ->
                    bookingViewModel.attachTransactionId(bookingId, txId)
                    navController.navigate("confirmation_route/$bookingId/$txId") {
                        popUpTo(Screen.Hospitals.route)
                    }
                },
                onFailure = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Booking Confirmation ──────────────────────────────────────────────
        composable(
            route = "confirmation_route/{bookingId}/{transactionId}",
            arguments = listOf(
                navArgument("bookingId")     { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { back ->
            BookingConfirmScreen(
                bookingId     = back.arguments?.getString("bookingId")     ?: "",
                transactionId = back.arguments?.getString("transactionId") ?: "",
                onBackToHospitals = {
                    bookingViewModel.clearSuccess()
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                }
            )
        }
    }
}