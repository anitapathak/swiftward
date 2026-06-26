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
    val paymentUrl: String?     = null,
    val pidx: String?           = null,
    val transactionId: String?  = null,
    val error: String?          = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val api: SwiftWardApi,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentUiState())
    val state: StateFlow<PaymentUiState> = _state.asStateFlow()

    fun initiateKhaltiPayment(bookingId: String, hospitalName: String, wardType: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val userName  = session.userName.firstOrNull()  ?: "Patient"
                val userEmail = session.userEmail.firstOrNull() ?: "patient@swiftward.com"
                val userPhone = session.userPhone.firstOrNull() ?: "9800000000"  // real user phone

                val resp = api.initiateKhaltiPayment(
                    KhaltiInitiateRequest(
                        bookingId    = bookingId,
                        amount       = 30000L,   // Rs 300 in paisa
                        hospitalName = hospitalName,
                        wardType     = wardType,
                        userName     = userName,
                        userEmail    = userEmail,
                        userPhone    = userPhone  // now sent to backend
                    )
                )
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val data = resp.body()!!.data!!
                    _state.update { it.copy(isLoading = false, paymentUrl = data.payment_url, pidx = data.pidx) }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.body()?.message ?: "Failed to initiate payment") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

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
                        _state.update { it.copy(isLoading = false, transactionId = data.transaction_id ?: pidxToUse) }
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Payment not completed. Status: ${data.status}") }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = resp.body()?.message ?: "Verification failed") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Verify error: ${e.localizedMessage}") }
            }
        }
    }

    fun clearPaymentUrl() = _state.update { it.copy(paymentUrl = null) }
    fun clearError()      = _state.update { it.copy(error = null) }
    fun resetState()      = _state.update { PaymentUiState() }
}