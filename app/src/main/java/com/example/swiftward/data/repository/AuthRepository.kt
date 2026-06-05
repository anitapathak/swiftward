package com.swiftward.data.repository

import com.swiftward.data.api.SwiftWardApi
import com.swiftward.data.model.*
import com.swiftward.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthRepository @Inject constructor(
    private val api: SwiftWardApi,
    private val session: SessionManager
) {
    val isLoggedIn: Flow<Boolean> = session.isLoggedIn
    val userName: Flow<String?> = session.userName

    // --- LOGIN ---
    fun login(phone: String, password: String): Flow<Result<AuthResponse>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.login(LoginRequest(phone, password))
            if (resp.isSuccessful && resp.body()?.success == true) {
                val auth = resp.body()!!.data!!
                session.saveSession(auth.token, auth.user.id, auth.user.name, auth.user.phone)
                emit(Result.Success(auth))
            } else {
                emit(Result.Error(resp.body()?.message ?: "Login Failed"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Network Error: ${e.message}"))
        }
    }

    // --- REGISTER ---
    fun register(name: String, phone: String, password: String): Flow<Result<AuthResponse>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.register(RegisterRequest(name, phone, password))
            if (resp.isSuccessful && resp.body()?.success == true) {
                val auth = resp.body()!!.data!!
                session.saveSession(auth.token, auth.user.id, auth.user.name, auth.user.phone)
                emit(Result.Success(auth))
            } else {
                emit(Result.Error(resp.body()?.message ?: "Registration Failed"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Network Error: ${e.message}"))
        }
    }

    // --- SEND/RESEND OTP ---
    // Uses ResendOtpRequest which maps to your Node.js backend
    // Inside AuthRepository.kt
    suspend fun sendOtp(phone: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            // Change "phone" to "phoneNumber" if your Node.js uses req.body.phoneNumber
            val response = api.sendOtp(mapOf("phoneNumber" to phone))

            if (response.isSuccessful) emit(Result.Success(Unit))
            else emit(Result.Error("Failed to send OTP"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Network Error"))
        }
    }

    // --- VERIFY OTP ---
    // Matches your Node.js: const { phoneNumber, userOtp } = req.body;
    fun verifyOtp(phone: String, otp: String): Flow<Result<OtpResponse>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.verifyOtp(VerifyOtpRequest(phoneNumber = phone, userOtp = otp))
            val body = resp.body();
            if (resp.isSuccessful && resp.body()?.success == true) {
                // If verification is successful, you might want to save a temporary token
                // or proceed to profile completion
                emit(Result.Success(body))
            } else {
                emit(Result.Error(resp.body()?.message ?: "Invalid OTP code"))
            }
        } catch (e: Exception) {
            emit(Result.Error("Verification failed: ${e.message}"))
        }
    }

    private fun emit(value: Result.Success<ApiResponse<AuthResponse>?>) {}


    suspend fun logout() = session.clearSession()
}