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

    fun login(phone: String, password: String): Flow<Result<AuthResponse>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.login(LoginRequest(phone, password))
            if (resp.isSuccessful && resp.body()?.success == true) {
                val auth = resp.body()!!.data!!
                // Save full session including email
                session.saveSession(auth.token, auth.user.id, auth.user.name, auth.user.phone, auth.user.email)
                emit(Result.Success(auth))
            } else {
                val errorMsg = resp.errorBody()?.string() ?: resp.body()?.message ?: "Login Failed"
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Result.Error("Network Error: ${e.localizedMessage ?: e.message}"))
        }
    }

    fun register(name: String, phone: String, email: String, password: String) = flow {
        emit(Result.Loading)
        try {
            val response = api.register(RegisterRequest(name, phone, email, password))
            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?: response.body()?.message
                    ?: "Registration failed"
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Check your internet connection"))
        }
    }

    fun sendOtp(phone: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val response = api.sendOtp(mapOf("phone" to phone))
            if (response.isSuccessful) emit(Result.Success(Unit))
            else emit(Result.Error(response.errorBody()?.string() ?: "Failed to send OTP"))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Network Error"))
        }
    }

    // FIX: verifyOtp does NOT save session — user must log in manually after
    fun verifyOtp(phone: String, otp: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.verifyOtp(VerifyOtpRequest(phoneNumber = phone, userOtp = otp))
            val body = resp.body()
            if (resp.isSuccessful && body?.success == true) {
                // No session saved here — user navigates to Login screen to log in
                emit(Result.Success(Unit))
            } else {
                val errorMsg = resp.errorBody()?.string() ?: body?.message ?: "Invalid OTP code"
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Result.Error("Verification failed: ${e.localizedMessage ?: e.message}"))
        }
    }

    suspend fun logout() = session.clearSession()
}