package com.example.swiftward.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.*

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    // ── Fetch current GPS location ────────────────────────────────────────────
    @SuppressLint("MissingPermission")
    suspend fun fetchLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    _location.value = loc
                    cont.resume(loc)
                }
                .addOnFailureListener { cont.resume(null) }
            cont.invokeOnCancellation { cts.cancel() }
        }

    // ── Haversine formula ─────────────────────────────────────────────────────
    // Returns distance in km between two lat/lng coordinates
    fun distanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun formatDistance(km: Double): String = when {
        km < 0.1  -> "< 100 m"
        km < 1.0  -> "${(km * 1000).toInt()} m"
        else      -> "%.1f km".format(km)
    }

    // ── Reverse-geocode to city name ──────────────────────────────────────────
    @Suppress("DEPRECATION")
    fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.ENGLISH)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                buildString {
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    if (locality != null) append(locality)
                    val country = addr.countryName
                    if (country != null) {
                        if (isNotEmpty()) append(", ")
                        append(country)
                    }
                }.ifBlank { "%.4f, %.4f".format(lat, lon) }
            } else {
                "%.4f, %.4f".format(lat, lon)
            }
        } catch (e: Exception) {
            "Kathmandu, Nepal"
        }
    }

    // ── Fallback location: Kathmandu centre ───────────────────────────────────
    fun fallbackLocation(): Location = Location("fallback").apply {
        latitude  = 27.7172
        longitude = 85.3240
    }
}