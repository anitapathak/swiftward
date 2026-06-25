package com.example.swiftward.ui.viewmodel.BookingViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.model.*
import com.swiftward.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean  = false,
    val booking: Booking?   = null,
    val error: String?      = null,
    val success: Boolean    = false
)

// Global in-memory list persists across ViewModel re-instantiations within the same process
private val globalLocalBookings   = mutableListOf<Booking>()
private val _globalBookingsFlow   = MutableStateFlow<List<Booking>>(emptyList())

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    val bookings: StateFlow<List<Booking>> = _globalBookingsFlow.asStateFlow()

    fun submitBooking(request: BookingRequest, hospital: Hospital) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val doctorMap = mapOf(
                "h1" to "Dr. Sita Sharma",    "h2" to "Dr. Ramesh Adhikari",
                "h3" to "Dr. Priya Thapa",    "h4" to "Dr. Bikash Karki",
                "h5" to "Dr. Anita Shrestha", "h6" to "Dr. Sunita Rai",
                "h7" to "Dr. Mohan Gurung",   "h8" to "Dr. Prem Shrestha",
                "h9" to "Dr. Kamala Joshi",   "h10" to "Dr. Deepak Tamang",
                "h11" to "Dr. Sabina Magar",  "h12" to "Dr. Roshan Thapa"
            )

            val newBooking = Booking(
                bookingId      = "SW-${(100000..999999).random()}",
                hospitalId     = hospital.id,
                hospitalName   = hospital.name,
                wardType       = request.wardType,
                patient        = request.patient,
                condition      = request.condition,
                etaMinutes     = request.etaMinutes,
                notes          = request.notes,
                isEmergency    = request.isEmergency,
                status         = BookingStatus.CONFIRMED,
                assignedDoctor = doctorMap[hospital.id] ?: "Dr. On-duty",
                hospitalPhone  = hospital.phone,
                feePaid        = 300,
                createdAt      = System.currentTimeMillis()
            )

            globalLocalBookings.add(0, newBooking)  // newest first
            _globalBookingsFlow.value = globalLocalBookings.toList()
            _uiState.update { it.copy(isLoading = false, booking = newBooking, success = true) }
        }
    }

    /** Called after Khalti payment verified — attach the real transaction ID */
    fun attachTransactionId(bookingId: String, txId: String) {
        val idx = globalLocalBookings.indexOfFirst { it.bookingId == bookingId }
        if (idx >= 0) {
            globalLocalBookings[idx] = globalLocalBookings[idx].copy(transactionId = txId)
            _globalBookingsFlow.value = globalLocalBookings.toList()
        }
    }

    fun clearSuccess() = _uiState.update { it.copy(success = false) }
}