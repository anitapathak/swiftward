package com.swiftward.data.model

import com.google.gson.annotations.SerializedName

// ── Hospital ──────────────────────────────────────────────────────────────────
enum class WardType(val displayName: String, val shortName: String) {
    EMERGENCY  ("Emergency",          "Emergency"),
    ICU        ("ICU",                "ICU"),
    HDU        ("HDU / Step-down",    "HDU"),
    GENERAL    ("General Ward",       "General"),
    PEDIATRIC  ("Pediatric",          "Pediatric"),
    MATERNITY  ("Maternity",          "Maternity"),
    BURN       ("Burn Unit",          "Burns"),
    ORTHOPEDIC ("Orthopedic",         "Ortho"),
    CARDIAC    ("Cardiac / CICU",     "Cardiac"),
    NEUROLOGY  ("Neurology",          "Neuro")
}


data class Hospital(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val isOpen24x7: Boolean,
    val wards: List<Ward>,
    val distanceKm: Double = 0.0,
    val avgWaitMinutes: Int = 8
) {
    val totalFreeBeds: Int get() = wards.sumOf { it.freeBeds }
    val isFull: Boolean get() = totalFreeBeds == 0

    // Most critical ward with available beds
    val topAvailableWard: Ward?
        get() = wards
            .filter { it.freeBeds > 0 }
            .minByOrNull { wardPriorityScore(it.type) }

    private fun wardPriorityScore(type: WardType) = when (type) {
        WardType.EMERGENCY   -> 1
        WardType.ICU         -> 2
        WardType.HDU         -> 3
        WardType.GENERAL     -> 4
        WardType.PEDIATRIC   -> 5
        WardType.MATERNITY   -> 6
        WardType.BURN        -> 7
        WardType.ORTHOPEDIC  -> 8
        WardType.CARDIAC     -> 9
        WardType.NEUROLOGY   -> 10
    }

}

data class Ward(
    val id: String,
    val type: WardType,
    val totalBeds: Int,
    val freeBeds: Int
)



// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val phone: String,
    val password: String
)
data class RegisterRequest(
    @SerializedName("name") // Backend destructures req.body.name
    val name: String,
    val phone: String,
    val email: String,
    val password: String

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
    val createdAt: Long = System.currentTimeMillis(),
    val feePaid: Int
)

enum class BookingStatus { PENDING, CONFIRMED, PREPARING, ARRIVED, CANCELLED }

// ── API wrappers ──────────────────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String = ""
)
data class VerifyOtpRequest(
    // Backend reads req.body.phone and req.body.otp
    @SerializedName("phone")
    val phoneNumber: String,
    @SerializedName("otp")
    val userOtp: String
)

data class ResendOtpRequest(
    @SerializedName("phone")
    val phoneNumber: String
)

data class OtpResponse(
    val success: Boolean,
    val message: String
)

// ── Dual sort comparator ──────────────────────────────────────────────────────
// Primary: distance ascending
// Secondary: most critical available ward type ascending
// Full hospitals always last
object HospitalSorter {

    fun sort(hospitals: List<Hospital>): List<Hospital> {
        val (available, full) = hospitals.partition { !it.isFull }

        val sorted = available.sortedWith(
            compareBy<Hospital> { it.distanceKm }
                .thenBy  { it.topAvailableWard?.let { w -> wardScore(w.type) } ?: 99 }
                .thenByDescending { it.totalFreeBeds }
        )

        return sorted + full.sortedBy { it.distanceKm }
    }

    fun sortByWardFirst(hospitals: List<Hospital>): List<Hospital> {
        val (available, full) = hospitals.partition { !it.isFull }
        val sorted = available.sortedWith(
            compareBy<Hospital> { it.topAvailableWard?.let { w -> wardScore(w.type) } ?: 99 }
                .thenBy { it.distanceKm }
                .thenByDescending { it.totalFreeBeds }
        )
        return sorted + full.sortedBy { it.distanceKm }
    }

    private fun wardScore(type: WardType) = when (type) {
        WardType.EMERGENCY   -> 1
        WardType.ICU         -> 2
        WardType.HDU         -> 3
        WardType.GENERAL     -> 4
        WardType.PEDIATRIC   -> 5
        WardType.MATERNITY   -> 6
        WardType.BURN        -> 7
        WardType.ORTHOPEDIC  -> 8
        WardType.CARDIAC     -> 9
        WardType.NEUROLOGY   -> 10
    }
}

