package com.example.swiftward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.api.SwiftWardApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // FIXED: Changed from jakarta to javax
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


@HiltViewModel
class OtpViewModel @Inject constructor(private val api: SwiftWardApi) : ViewModel() {

    private val _timerValue = MutableStateFlow(45)
    val timerValue: StateFlow<Int> = _timerValue

    private val _canResend = MutableStateFlow(false)
    val canResend: StateFlow<Boolean> = _canResend

    init {
        // Start the countdown automatically when the ViewModel is created
        startTimer()
    }

    fun startTimer() {
        viewModelScope.launch {
            _canResend.value = false
            for (i in 45 downTo 0) {
                _timerValue.value = i
                delay(1000)
            }
            _canResend.value = true
        }
    }

    fun resendOtp(email: String) {
        // Here you would call your actual SMS API service
        startTimer()
    }
}