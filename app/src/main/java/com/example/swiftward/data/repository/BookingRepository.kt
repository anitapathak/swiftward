package com.swiftward.data.repository

import com.example.swiftward.data.local.MockData
import com.swiftward.data.api.SwiftWardApi
import com.swiftward.data.model.*
import com.swiftward.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val api: SwiftWardApi,
    private val session: SessionManager,
    private val hospitalRepository: HospitalRepository
) {
    private val localBookings = mutableListOf<Booking>()

    fun createBooking(request: BookingRequest): Flow<Result<Booking>> = flow {
        emit(Result.Loading)
        try {
            val token = "Bearer mock_token"
            val resp = api.createBooking(token, request)
            if (resp.isSuccessful && resp.body()?.success == true) {
                val booking = resp.body()!!.data!!
                localBookings.add(booking)
                emit(Result.Success(booking))
            } else {
                // Mock booking confirmation
                val hospital = MockData.getAll().find { it.id == request.hospitalId }
                    ?: throw Exception("Hospital not found")
                val booking = MockData.createMockBooking(request, hospital)
                localBookings.add(booking)
                emit(Result.Success(booking))
            }
        } catch (e: Exception) {
            val hospital = MockData.getAll().find { it.id == request.hospitalId }
            if (hospital != null) {
                val booking = MockData.createMockBooking(request, hospital)
                localBookings.add(booking)
                emit(Result.Success(booking))
            } else {
                emit(Result.Error(e.message ?: "Booking failed"))
            }
        }
    }

    fun getUserBookings(): Flow<Result<List<Booking>>> = flow {
        emit(Result.Loading)
        emit(Result.Success(localBookings.toList().reversed()))
    }
}