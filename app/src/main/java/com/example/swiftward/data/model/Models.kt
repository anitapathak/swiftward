package com.swiftward.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ── Hospital ──────────────────────────────────────────────────────────────────

data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val isOpen24x7: Boolean,
    val wards: List<Ward>,
    val distanceKm: Double = 0.0,       // computed locally
    val avgWaitMinutes: Int = 8
) {
    val totalFreeBeds: Int get() = wards.sumOf { it.freeBeds }
    val isFull: Boolean get() = totalFreeBeds == 0
}

data class Ward(
    val id: String,
    val type: WardType,
    val totalBeds: Int,
    val freeBeds: Int
)

enum class WardType(val displayName: String, val colorHex: String) {
    GENERAL("General ward", "#1D9E75"),
    ICU("ICU", "#D85A30"),
    HDU("HDU / Step-down", "#BA7517"),
    PEDIATRIC("Pediatric", "#D4537E"),
    MATERNITY("Maternity", "#7F77DD"),
    EMERGENCY("Emergency", "#E24B4A")
}

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val phone: String,
    val password: String,
    val email: String = ""
)

data class OtpRequest(
    val phone: String,
    val otp: String
)

data class AuthResponse(
    val token: String,
    val user: User,
    val message: String = ""
)

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val savedPatients: List<PatientProfile> = emptyList()
)

// ── Patient & Booking ─────────────────────────────────────────────────────────

data class PatientProfile(
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val knownConditions: List<String> = emptyList()
)

data class BookingRequest(
    val hospitalId: String,
    val wardType: WardType,
    val patient: PatientProfile,
    val condition: String,
    val etaMinutes: Int,
    val notes: String,
    val isEmergency: Boolean
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
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus { PENDING, CONFIRMED, PREPARING, ARRIVED, CANCELLED }

// ── API wrappers ──────────────────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String = ""
)