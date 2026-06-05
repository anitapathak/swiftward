package com.example.swiftward.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.swiftward.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Hospitals.route
    ) {
        // 1. Hospitals Home Screen
        composable(Screen.Hospitals.route) {
            HospitalsScreen(
                onHospitalClick = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                },
                onBookingsClick = {
                    navController.navigate(Screen.MyBookings.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onMapClick = {
                    navController.navigate(Screen.map.route)
                }
            )
        }

        // 2. Map Screen (ADDED & CONNECTED)
        composable(Screen.map.route) {
            MapScreen(
                onHospitalClick = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                },
                onHospitalsClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                },
                onBookingsClick = {
                    navController.navigate(Screen.MyBookings.route) {
                        popUpTo(Screen.Hospitals.route)
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Hospitals.route)
                    }
                }
            )
        }

        // 3. Bookings List
        composable(Screen.MyBookings.route) {
            BookingsScreen(
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

        // 4. Profile
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onHospitalsClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                },
                onBookingsClick = { navController.navigate(Screen.MyBookings.route) }
            )
        }

        // 5. Hospital Detail
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

        // 6. Emergency Booking Form
        composable(
            route = Screen.EmergencyBook.route,
            arguments = listOf(navArgument("hospitalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val hospitalId = backStackEntry.arguments?.getString("hospitalId") ?: ""
            EmergencyBookingScreen(
                hospitalId = hospitalId,
                onBack = { navController.popBackStack() },
                onBookingConfirmed = { bookingId ->
                    navController.navigate(Screen.BookingConfirm.createRoute(bookingId)) {
                        popUpTo(Screen.Hospitals.route)
                    }
                }
            )
        }

        // 7. Booking Confirmation
        composable(
            route = Screen.BookingConfirm.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingConfirmScreen(
                bookingId = bookingId,
                onBackToHospitals = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Hospitals.route) { inclusive = true }
                    }
                }
            )
        }
    }
}