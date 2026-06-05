package com.swiftward.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swiftward.data.local.MockData
import com.example.swiftward.utils.LocationHelper
import com.swiftward.data.model.*

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HospitalUiState(
    val isLoading: Boolean     = false,
    val isLocating: Boolean    = true,
    val hospitals: List<Hospital> = emptyList(),
    val selectedHospital: Hospital? = null,
    val userLocation: Location? = null,
    val cityName: String        = "",
    val locationError: String?  = null,
    val searchQuery: String     = "",
    val activeFilter: WardType? = null,
    val sortMode: SortMode      = SortMode.DISTANCE_THEN_WARD
)

enum class SortMode {
    DISTANCE_THEN_WARD,   // Primary: distance  / Secondary: ward criticality
    WARD_THEN_DISTANCE    // Primary: ward type  / Secondary: distance
}

@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _state = MutableStateFlow(HospitalUiState())
    val state: StateFlow<HospitalUiState> = _state.asStateFlow()

    // ── Filtered + sorted list for UI ─────────────────────────────────────────
    val filteredHospitals: StateFlow<List<Hospital>> = _state
        .map { s ->
            var list = s.hospitals

            // Apply ward type filter
            if (s.activeFilter != null) {
                list = list.filter { h ->
                    h.wards.any { it.type == s.activeFilter && it.freeBeds > 0 }
                }
            }

            // Apply search query
            if (s.searchQuery.isNotBlank()) {
                val q = s.searchQuery.lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                            it.address.lowercase().contains(q) ||
                            it.wards.any { w -> w.type.displayName.lowercase().contains(q) }
                }
            }

            list
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Initialise: request location then load + sort hospitals ───────────────
    fun initLocation(permissionGranted: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLocating = true) }

            val location = if (permissionGranted) {
                locationHelper.fetchLocation() ?: locationHelper.fallbackLocation()
            } else {
                locationHelper.fallbackLocation()
            }

            val cityName = locationHelper.getCityName(location.latitude, location.longitude)

            _state.update { it.copy(
                userLocation = location,
                cityName     = cityName,
                isLocating   = false
            )}

            loadAndSortHospitals(location)
        }
    }

    // ── Core dual-sort algorithm ──────────────────────────────────────────────
    private fun loadAndSortHospitals(location: Location) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val raw = MockData.getAll()

            // Step 1: compute haversine distance for every hospital
            val withDistance = raw.map { h ->
                h.copy(
                    distanceKm = locationHelper.distanceKm(
                        location.latitude, location.longitude,
                        h.latitude, h.longitude
                    )
                )
            }

            // Step 2: dual sort based on current mode
            val sorted = when (_state.value.sortMode) {
                SortMode.DISTANCE_THEN_WARD -> HospitalSorter.sort(withDistance)
                SortMode.WARD_THEN_DISTANCE -> HospitalSorter.sortByWardFirst(withDistance)
            }

            _state.update { it.copy(isLoading = false, hospitals = sorted) }
        }
    }

    fun loadHospitalDetail(id: String) {
        viewModelScope.launch {
            val hospital = _state.value.hospitals.find { it.id == id }
                ?: MockData.getAll().find { it.id == id }
            hospital?.let { h ->
                val loc = _state.value.userLocation ?: locationHelper.fallbackLocation()
                val withDist = h.copy(
                    distanceKm = locationHelper.distanceKm(
                        loc.latitude, loc.longitude, h.latitude, h.longitude
                    )
                )
                _state.update { it.copy(selectedHospital = withDist) }
            }
        }
    }

    fun setFilter(type: WardType?) = _state.update { it.copy(activeFilter = type) }

    fun setSearch(query: String) = _state.update { it.copy(searchQuery = query) }

    fun setSortMode(mode: SortMode) {
        _state.update { it.copy(sortMode = mode) }
        _state.value.userLocation?.let { loadAndSortHospitals(it) }
    }

    fun formatDistance(km: Double) = locationHelper.formatDistance(km)

    fun refresh() {
        val loc = _state.value.userLocation ?: locationHelper.fallbackLocation()
        loadAndSortHospitals(loc)
    }
}