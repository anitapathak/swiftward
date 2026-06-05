package com.example.swiftward.data.model

import com.swiftward.data.model.WardType


data class PatientProfile(
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val knownConditions: List<String> = emptyList()
)

data class Booking(
    val bookingId: String,
    val hospitalId: String,
    val hospitalName: String,
    val wardType: WardType,
    val patient: PatientProfile,
    val condition: String,
    val etaMinutes: Int,
    val notes: String,
    val isEmergency: Boolean,
    val status: BookingStatus,
    val assignedDoctor: String,
    val hospitalPhone: String,
    val feePaid: Int = 200,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus { PENDING, CONFIRMED, PREPARING, ARRIVED, CANCELLED }

data class BookingRequest(
    val hospitalId: String,
    val wardType: WardType,
    val patient: PatientProfile,
    val condition: String,
    val etaMinutes: Int,
    val notes: String,
    val isEmergency: Boolean
)