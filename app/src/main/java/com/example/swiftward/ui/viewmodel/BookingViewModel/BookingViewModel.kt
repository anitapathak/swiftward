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
    val bookings: List<Booking>  = emptyList(),
    val booking: Booking?        = null,
    val error: String?           = null,
    val success: Boolean         = false
)

@HiltViewModel
class BookingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    // In-memory store (replace with Room or API in production)
    private val localBookings = mutableListOf<Booking>()

    fun loadBookings() {
        _state.update { it.copy(bookings = localBookings.toList().reversed()) }
    }

    fun submitBooking(request: BookingRequest, hospital: com.swiftward.data.model.Hospital) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val booking = Booking(
                bookingId     = "SW-${(1000..9999).random()}",
                hospitalId    = request.hospitalId,
                hospitalName  = hospital.name,
                wardType      = request.wardType,
                patient       = request.patient,
                condition     = request.condition,
                etaMinutes    = request.etaMinutes,
                notes         = request.notes,
                isEmergency   = request.isEmergency,
                status        = BookingStatus.CONFIRMED,
                assignedDoctor = "Dr. On-duty",
                hospitalPhone  = hospital.phone,
                feePaid        = 200
            )
            localBookings.add(booking)
            _state.update { it.copy(isLoading = false, booking = booking, success = true) }
        }
    }

    fun clearSuccess() = _state.update { it.copy(success = false) }
}