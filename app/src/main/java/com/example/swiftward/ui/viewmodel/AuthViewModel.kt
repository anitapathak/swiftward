package com.example.swiftward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.repository.AuthRepository
import com.swiftward.data.repository.Result
import com.swiftward.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val phone: String = "",
    val otpSent: Boolean = false,
    val timerValue: Int = 60,      // New: Tracks the 0:42 countdown
    val canResend: Boolean = false  // New: Enables the Resend button
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
    }
    // --- LOGIN LOGIC ---
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            repo.login(phone, password).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        // The repository should have already called sessionManager.saveSession()
                        _state.update { it.copy(isLoading = false, success = true) }
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    // --- REGISTRATION LOGIC ---
    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            repo.register(name, phone, email, password).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        // After registration, we don't log in automatically yet.
                        // We set success = true so the UI can navigate back to Login.
                        _state.update { it.copy(isLoading = false, success = true) }
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    // --- OTP LOGIC ---
    fun startTimer() {
        _state.update { it.copy(timerValue = 60, canResend = false) }
        timerJob?.cancel() // Reset any previous timer
        timerJob = viewModelScope.launch {
            while (_state.value.timerValue > 0) {
                kotlinx.coroutines.delay(1000L)
                _state.update { it.copy(timerValue = it.timerValue - 1) }
            }
            _state.update { it.copy(canResend = true) }
        }
    }

    // --- OTP LOGIC ---
    fun sendOtp(phone: String) {
        viewModelScope.launch {
            repo.sendOtp(phone).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false, otpSent = true, phone = phone) }
                        startTimer() // Start the 60s countdown immediately after sending
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun resendOtp(phone: String) {
        viewModelScope.launch {
            // Re-use the sendOtp logic but specifically for the resend trigger
            repo.sendOtp(phone).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true) }
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        startTimer() // Restart timer on success
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            repo.verifyOtp(phone, otp).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        timerJob?.cancel() // Stop the timer if verification is successful
                        _state.update { it.copy(isLoading = false, success = true) }
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    // --- LOGOUT LOGIC ---
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession() // Clears DataStore
            _state.update { AuthUiState() } // Resets UI state
        }
    }

    // --- UI HELPERS ---
    fun clearError() = _state.update { it.copy(error = null) }
    fun resetSuccess() = _state.update { it.copy(success = false) }
}