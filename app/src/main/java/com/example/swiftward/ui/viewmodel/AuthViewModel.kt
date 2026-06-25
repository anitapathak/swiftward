package com.example.swiftward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.repository.AuthRepository
import com.swiftward.data.model.Result
import com.swiftward.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean  = false,
    val isLoggedIn: Boolean = false,
    val error: String?      = null,
    val success: Boolean    = false,
    val phone: String       = "",
    val otpSent: Boolean    = false,
    val timerValue: Int     = 60,
    val canResend: Boolean  = false
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

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            repo.login(phone, password).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _state.update { it.copy(isLoading = false, success = true) }
                    is Result.Error   -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            repo.register(name, phone, email, password).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _state.update { it.copy(isLoading = false, success = true) }
                    is Result.Error   -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun startTimer() {
        _state.update { it.copy(timerValue = 60, canResend = false) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerValue > 0) {
                kotlinx.coroutines.delay(1000L)
                _state.update { it.copy(timerValue = it.timerValue - 1) }
            }
            _state.update { it.copy(canResend = true) }
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            repo.sendOtp(phone).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false, otpSent = true, phone = phone) }
                        startTimer()
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun resendOtp(phone: String) {
        viewModelScope.launch {
            repo.sendOtp(phone).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true) }
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        startTimer()
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
                        timerJob?.cancel()
                        _state.update { it.copy(isLoading = false, success = true) }
                    }
                    is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _state.update { AuthUiState() }
        }
    }

    fun clearError()    = _state.update { it.copy(error = null) }
    fun resetSuccess()  = _state.update { it.copy(success = false) }
}