package com.example.swiftward.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.api.SwiftWardApi
import com.swiftward.data.model.KhaltiInitiateRequest
import com.swiftward.data.model.KhaltiVerifyRequest
import com.swiftward.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean      = false,
    val paymentUrl: String?     = null,   // Khalti checkout URL → open in browser
    val pidx: String?           = null,   // stored for verification step
    val transactionId: String?  = null,   // set after successful verify
    val error: String?          = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val api: SwiftWardApi,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentUiState())
    val state: StateFlow<PaymentUiState> = _state.asStateFlow()

    // Called from EmergencyPaymentScreen when user taps "Pay Rs 300 via Khalti"
    fun initiateKhaltiPayment(bookingId: String, hospitalName: String, wardType: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val userName  = session.userName.firstOrNull()  ?: "Patient"
                val userEmail = session.userEmail.firstOrNull() ?: "patient@swiftward.com"

                val resp = api.initiateKhaltiPayment(
                    KhaltiInitiateRequest(
                        bookingId    = bookingId,
                        amount       = 30000L,      // Rs 300 in paisa
                        hospitalName = hospitalName,
                        wardType     = wardType,
                        userName     = userName,
                        userEmail    = userEmail
                    )
                )
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val data = resp.body()!!.data!!
                    _state.update {
                        it.copy(isLoading = false, paymentUrl = data.payment_url, pidx = data.pidx)
                    }
                } else {
                    val msg = resp.body()?.message ?: resp.errorBody()?.string() ?: "Failed to initiate payment"
                    _state.update { it.copy(isLoading = false, error = msg) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

    // Called from KhaltiCallbackScreen (after deep-link redirect) OR
    // manually from EmergencyPaymentScreen "I've paid — Verify" button
    fun verifyKhaltiPayment(bookingId: String, hospitalName: String, wardType: String, forcePidx: String? = null) {
        val pidxToUse = forcePidx ?: _state.value.pidx
        if (pidxToUse.isNullOrBlank()) {
            _state.update { it.copy(error = "No payment session found. Please try again.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val userName  = session.userName.firstOrNull()  ?: "Patient"
                val userEmail = session.userEmail.firstOrNull() ?: "patient@swiftward.com"

                val resp = api.verifyKhaltiPayment(
                    KhaltiVerifyRequest(
                        pidx         = pidxToUse,
                        bookingId    = bookingId,
                        hospitalName = hospitalName,
                        wardType     = wardType,
                        userEmail    = userEmail,
                        userName     = userName
                    )
                )
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val data = resp.body()!!.data!!
                    if (data.isCompleted) {
                        val txId = data.transaction_id ?: pidxToUse
                        _state.update { it.copy(isLoading = false, transactionId = txId) }
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Payment not completed. Status: ${data.status}") }
                    }
                } else {
                    val msg = resp.body()?.message ?: resp.errorBody()?.string() ?: "Verification failed"
                    _state.update { it.copy(isLoading = false, error = msg) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Verify error: ${e.localizedMessage}") }
            }
        }
    }

    fun clearPaymentUrl() = _state.update { it.copy(paymentUrl = null) }
    fun resetState()      = _state.update { PaymentUiState() }
}
