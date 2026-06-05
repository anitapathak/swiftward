package com.swiftward.data.repository

import android.location.Location
import com.example.swiftward.data.local.MockData
import com.swiftward.data.api.SwiftWardApi
import com.swiftward.data.model.*
import com.swiftward.utils.LocationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class HospitalRepository @Inject constructor(
    private val api: SwiftWardApi,
    private val locationManager: LocationManager
) {
    /**
     * Fetch hospitals, compute haversine distance from [userLocation],
     * and return sorted by distance ascending.
     */
    fun getHospitalsSortedByDistance(
        userLocation: Location,
        wardTypeFilter: WardType? = null
    ): Flow<Result<List<Hospital>>> = flow {
        emit(Result.Loading)
        try {
            // Try real API first, fall back to mock data
            val hospitals: List<Hospital> = try {
                val resp = api.getHospitals(userLocation.latitude, userLocation.longitude)
                if (resp.isSuccessful) resp.body()?.data ?: MockData.getAll()
                else MockData.getAll()
            } catch (e: Exception) {
                MockData.getAll()
            }

            val withDistance = hospitals.map { h ->
                h.copy(
                    distanceKm = locationManager.distanceKm(
                        userLocation.latitude, userLocation.longitude,
                        h.latitude, h.longitude
                    )
                )
            }

            val filtered = if (wardTypeFilter != null) {
                withDistance.filter { h -> h.wards.any { it.type == wardTypeFilter && it.freeBeds > 0 } }
            } else withDistance

            emit(Result.Success(filtered.sortedBy { it.distanceKm }))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    fun getHospitalDetail(id: String, userLocation: Location?): Flow<Result<Hospital>> = flow {
        emit(Result.Loading)
        try {
            val hospital = MockData.getAll().find { it.id == id }
                ?: run { emit(Result.Error("Hospital not found")); return@flow }
            val withDist = userLocation?.let { loc ->
                hospital.copy(
                    distanceKm = locationManager.distanceKm(
                        loc.latitude, loc.longitude, hospital.latitude, hospital.longitude
                    )
                )
            } ?: hospital
            emit(Result.Success(withDist))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }
}