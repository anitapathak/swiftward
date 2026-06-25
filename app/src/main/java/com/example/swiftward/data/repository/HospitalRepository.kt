package com.swiftward.data.repository

import android.location.Location
import com.example.swiftward.data.local.MockData
import com.example.swiftward.utils.LocationHelper
import com.swiftward.data.model.*
// NOTE: Result sealed class lives in Models.kt — do NOT redeclare it here
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HospitalRepository @Inject constructor(
    private val locationHelper: LocationHelper
    // SwiftWardApi removed — hospitals come from MockData only (no /hospitals backend endpoint)
) {
    fun getHospitalsSortedByDistance(
        userLocation: Location,
        wardTypeFilter: WardType? = null
    ): Flow<Result<List<Hospital>>> = flow {
        emit(Result.Loading)
        try {
            val hospitals = MockData.getAll()

            val withDistance = hospitals.map { h ->
                h.copy(
                    distanceKm = locationHelper.distanceKm(
                        userLocation.latitude, userLocation.longitude,
                        h.latitude, h.longitude
                    )
                )
            }

            val filtered = if (wardTypeFilter != null) {
                withDistance.filter { h -> h.wards.any { it.type == wardTypeFilter && it.freeBeds > 0 } }
            } else withDistance

            val sorted = HospitalSorter.sort(filtered)
            emit(Result.Success(sorted))
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
                    distanceKm = locationHelper.distanceKm(
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