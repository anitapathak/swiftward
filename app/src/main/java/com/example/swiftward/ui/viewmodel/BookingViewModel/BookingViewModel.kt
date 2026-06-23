package com.example.swiftward.ui.viewmodel.BookingViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftward.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean       = false,
    val booking: Booking?        = null,
    val error: String?           = null,
    val success: Boolean         = false
)

// Global app-wide in-memory list to keep records alive across screens/ViewModel re-instantiations
private val globalLocalBookings = mutableListOf<Booking>()

// ✅ Initialized as an empty list so "No Bookings Yet" shows up until a reservation is made
private val _globalBookingsFlow = MutableStateFlow<List<Booking>>(globalLocalBookings.toList())

@HiltViewModel
class BookingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // Exposing this stream fixes the "Create property 'bookings'" compilation error!
    val bookings: StateFlow<List<Booking>> = _globalBookingsFlow.asStateFlow()

    fun submitBooking(request: BookingRequest, hospital: com.swiftward.data.model.Hospital) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val newBooking = Booking(
                bookingId      = "SW-${(100000..999999).random()}", // Generates clean 6-digit tokens like SW-483921
                hospitalId     = request.hospitalId,
                hospitalName   = hospital.name,
                wardType       = request.wardType,
                patient        = request.patient,
                condition      = request.condition,
                etaMinutes     = request.etaMinutes,
                notes          = request.notes,
                isEmergency    = request.isEmergency,
                status         = BookingStatus.CONFIRMED,
                assignedDoctor = "Dr. Sita Sharma", // Populates your assigned staff row layout
                hospitalPhone  = hospital.phone,
                feePaid        = 300, // Matches your billing gateway fee payment configuration
                createdAt      = System.currentTimeMillis()
            )

            // 1. Persist directly to the local memory layer safely
            globalLocalBookings.add(newBooking)

            // 2. Alert the active global UI state stream flow
            _globalBookingsFlow.value = globalLocalBookings.toList()

            // 3. Mark action completed successfully inside workflow step machine
            _uiState.update {
                it.copy(
                    isLoading = false,
                    booking = newBooking,
                    success = true
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(success = false) }
    }
}