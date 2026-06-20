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
                // 💡 Extract the true backend message from errorBody if body is null
                val errorMsg = resp.errorBody()?.string() ?: resp.body()?.message ?: "Login Failed"
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Result.Error("Network Error: ${e.localizedMessage ?: e.message}"))
        }
    }

    // --- REGISTER ---
    fun register(name: String, phone: String, email: String, password: String) = flow {
        emit(Result.Loading)
        try {
            val response = api.register(RegisterRequest(name, phone, email, password))

            if (response.isSuccessful && response.body() != null) {
                emit(Result.Success(response.body()!!))
            } else {
                // 🚨 IF THIS EMITS AN ERROR, YOUR VIEWMODEL UPDATES THE STATE
                emit(Result.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            // 🚨 IF THERE IS A NETWORK/ROUTING ERROR, IT MUST BE CAUGHT AND EMITTED HERE!
            emit(Result.Error(e.localizedMessage ?: "Check your internet connection"))
        }
    }

    // --- SEND/RESEND OTP ---
    suspend fun sendOtp(phone: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val response = api.sendOtp(mapOf("phone" to phone))
            if (response.isSuccessful) {
                emit(Result.Success(Unit))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to send OTP"
                emit(Result.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: e.message ?: "Network Error"))
        }
    }

    // --- VERIFY OTP ---
    // ── VERIFY OTP ──
// Changed the Flow type parameter from OtpResponse to AuthResponse
    fun verifyOtp(phone: String, otp: String): Flow<Result<AuthResponse>> = flow {
        emit(Result.Loading)
        try {
            val resp = api.verifyOtp(VerifyOtpRequest(phoneNumber = phone, userOtp = otp))
            val body = resp.body()

            if (resp.isSuccessful && body?.success == true) {
                // Extract the actual inner user data object safely
                val authData = body.data!!

                // If you want to automatically log them in on successful OTP verification:
                session.saveSession(authData.token, authData.user.id, authData.user.name, authData.user.phone)

                emit(Result.Success(authData))
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