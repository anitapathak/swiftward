package com.swiftward.data.repository

import com.example.swiftward.data.local.MockData
import com.swiftward.data.model.*
import com.swiftward.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val session: SessionManager
    // NOTE: api.createBooking removed — bookings are managed locally in BookingViewModel
    // using the in-memory globalLocalBookings list. No backend booking endpoint exists yet.
) {
    private val localBookings = mutableListOf<Booking>()

    fun createBooking(request: BookingRequest): Flow<Result<Booking>> = flow {
        emit(Result.Loading)
        try {
            val hospital = MockData.getAll().find { it.id == request.hospitalId }
                ?: throw Exception("Hospital not found: ${request.hospitalId}")
            val booking = MockData.createMockBooking(request, hospital)
            localBookings.add(booking)
            emit(Result.Success(booking))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Booking failed"))
        }
    }

    fun getUserBookings(): Flow<Result<List<Booking>>> = flow {
        emit(Result.Loading)
        emit(Result.Success(localBookings.toList().reversed()))
    }
}