package com.example.swiftward.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.FilterChipsRow
import com.example.swiftward.ui.SearchBarPlaceholder
import com.example.swiftward.ui.WardSmallPill
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import com.swiftward.data.model.Hospital
import com.swiftward.data.model.WardType
import com.swiftward.viewmodel.HospitalViewModel
import com.swiftward.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HospitalsScreen(
    onHospitalClick: (String) -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
    viewModel: HospitalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val hospitals by viewModel.filteredHospitals.collectAsState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Request GPS permission and initialise hospitals on first launch
    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }
    LaunchedEffect(locationPermission.status.isGranted) {
        viewModel.initLocation(locationPermission.status.isGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SwiftWard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                state.cityName.ifBlank { "Getting location…" },
                                color = Color.White.copy(0.7f), fontSize = 12.sp
                            )
                        }
                    }
                },
                actions = {
                    // Sort toggle button
                    IconButton(onClick = {
                        viewModel.setSortMode(
                            if (state.sortMode == SortMode.DISTANCE_THEN_WARD)
                                SortMode.WARD_THEN_DISTANCE
                            else
                                SortMode.DISTANCE_THEN_WARD
                        )
                    }) {
                        Icon(Icons.Default.Sort, null, tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        bottomBar = {
            SwiftWardBottomBar(
                selected = 0,
                onHospitalsClick = {},
                onMapClick = onMapClick,
                onBookingsClick = onBookingsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Search bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    placeholder = { Text("Search hospitals or ward types…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
                )

                // Ward filter chips
                WardFilterChips(
                    activeFilter = state.activeFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )

                // Sort label
                val sortLabel = if (state.sortMode == SortMode.DISTANCE_THEN_WARD)
                    "NEAREST FIRST · ${hospitals.size} HOSPITALS"
                else
                    "MOST WARDS FIRST · ${hospitals.size} HOSPITALS"

                Text(
                    sortLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray
                )
            }

            if (state.isLocating || state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1E3A8A))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (state.isLocating) "Getting your location…" else "Loading hospitals…",
                                color = Color.Gray, fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(hospitals) { index, hospital ->
                    HospitalListItem(
                        hospital = hospital,
                        rank = index + 1,
                        distanceText = viewModel.formatDistance(hospital.distanceKm),
                        onClick = { onHospitalClick(hospital.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WardFilterChips(activeFilter: WardType?, onFilterSelected: (WardType?) -> Unit) {
    val wards = listOf(null, WardType.EMERGENCY, WardType.ICU, WardType.GENERAL, WardType.PEDIATRIC, WardType.MATERNITY)
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(wards) { ward ->
            val isSelected = activeFilter == ward
            Surface(
                onClick = { onFilterSelected(ward) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFF1E3A8A) else Color.White,
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF1E3A8A) else Color.LightGray)
            ) {
                Text(
                    ward?.shortName ?: "All",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = if (isSelected) Color.White else Color.DarkGray,
                    fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun HospitalListItem(hospital: Hospital, rank: Int, distanceText: String, onClick: () -> Unit) {
    val isFull = hospital.isFull
    val totalFree = hospital.totalFreeBeds

    Card(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = Color(0xFFEFF6FF), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$rank", color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(hospital.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1F2937))
                Text(hospital.address, color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    hospital.wards.filter { it.freeBeds > 0 }.take(3).forEach { ward ->
                        WardSmallPill("${ward.type.shortName} ${ward.freeBeds}")
                    }
                    if (isFull) {
                        Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(20.dp)) {
                            Text(
                                "FULL", color = Color(0xFFDC2626), fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = distanceText,
                    color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
                if (!isFull) {
                    Text("$totalFree", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                    Text("beds free", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SwiftWardBottomBar(
    selected: Int,
    onHospitalsClick: () -> Unit,
    onMapClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        val items = listOf(
            Triple(Icons.Default.GridView, "Hospitals", onHospitalsClick),
            Triple(Icons.Default.Map, "Map", onMapClick),
            Triple(Icons.Default.EventNote, "Bookings", onBookingsClick),
            Triple(Icons.Default.Person, "Profile", onProfileClick)
        )
        items.forEachIndexed { idx, (icon, label, action) ->
            NavigationBarItem(
                selected = selected == idx, onClick = action,
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E3A8A),
                    selectedTextColor = Color(0xFF1E3A8A),
                    indicatorColor = Color(0xFFEFF6FF),
                    unselectedIconColor = Color(0xFF6B7280),
                    unselectedTextColor = Color(0xFF6B7280)
                )
            )
        }
    }
}



