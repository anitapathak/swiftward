package com.swiftward.data.api

import com.swiftward.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SwiftWardApi {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/resend-otp")
    suspend fun sendOtp(@Body body: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("api/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): Response<OtpResponse>

    // FIX: verifyOtp now returns ApiResponse<Unit> — no auto auth data returned
    @POST("api/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<Unit>>

    // Khalti payment endpoints
    @POST("api/khalti/initiate")
    suspend fun initiateKhaltiPayment(@Body request: KhaltiInitiateRequest): Response<ApiResponse<KhaltiInitiateResponse>>

    @POST("api/khalti/verify")
    suspend fun verifyKhaltiPayment(@Body request: KhaltiVerifyRequest): Response<ApiResponse<KhaltiVerifyResponse>>
}