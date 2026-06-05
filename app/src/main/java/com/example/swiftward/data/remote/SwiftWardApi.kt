package com.swiftward.data.api

import com.swiftward.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SwiftWardApi {

    // ── Auth ──────────────────────────────────────────────────────────────────

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/send-otp")
    suspend fun sendOtp(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("api/resend-otp")
    suspend fun resendOtp(
        @Body request: ResendOtpRequest
    ): Response<OtpResponse>

    @POST("api/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<AuthResponse>>


    // ── Hospitals ─────────────────────────────────────────────────────────────

    @GET("hospitals")
    suspend fun getHospitals(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Double = 50.0
    ): Response<ApiResponse<List<Hospital>>>

    @GET("hospitals/{id}")
    suspend fun getHospital(
        @Path("id") id: String
    ): Response<ApiResponse<Hospital>>

    @GET("hospitals/search")
    suspend fun searchHospitals(
        @Query("q") query: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<ApiResponse<List<Hospital>>>

    // ── Bookings ──────────────────────────────────────────────────────────────

    @POST("bookings")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body request: BookingRequest
    ): Response<ApiResponse<Booking>>

    @GET("bookings")
    suspend fun getUserBookings(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Booking>>>

    @GET("bookings/{id}")
    suspend fun getBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: String
    ): Response<ApiResponse<Booking>>

    @PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: String
    ): Response<ApiResponse<Unit>>
}

