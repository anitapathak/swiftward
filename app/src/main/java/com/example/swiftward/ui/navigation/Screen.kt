package com.example.swiftward.ui.navigation


sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Register  : Screen("register")
    object Otp       : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/$phone"
    }
    object Hospitals : Screen("hospitals")
    //object Detail    : Screen("hospital/{id}") {
        //fun createRoute(id: String) = "hospital/$id"
    //}
    object EmergencyBook : Screen("emergency_book/{hospitalId}") {
        fun createRoute(id: String) = "emergency_book/$id"
    }
    object BookingConfirm : Screen("booking_confirm/{bookingId}") {
        fun createRoute(id: String) = "booking_confirm/$id"
    }
    object MyBookings : Screen("my_bookings")
    object Profile    : Screen("profile")
    object  map: Screen("map_screen")

    object Detail : Screen("detail_screen/{hospitalId}") {
        fun createRoute(hospitalId: String) = "detail_screen/$hospitalId"
    }
}
