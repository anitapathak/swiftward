package com.example.swiftward.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.swiftward.data.model.Hospital
import com.swiftward.viewmodel.HospitalViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

val Navy = Color(0xFF1E3A8A)
val TextSecond = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: HospitalViewModel = hiltViewModel(),
    onHospitalClick: (String) -> Unit,
    onHospitalsClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val state    by viewModel.state.collectAsState()
    val filtered by viewModel.filteredHospitals.collectAsState()
    val context  = LocalContext.current

    // Which hospital the user tapped the navigate arrow on — draw route to it
    var routeTarget by remember { mutableStateOf<Hospital?>(null) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
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
                        Text("Map", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            state.cityName.ifBlank { "Getting location…" },
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        bottomBar = {
            SwiftWardBottomBar(
                selected        = 1,
                onHospitalsClick = onHospitalsClick,
                onMapClick      = {},
                onBookingsClick = onBookingsClick,
                onProfileClick  = onProfileClick
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── OSMDroid Map ─────────────────────────────────────────────────
            Box(Modifier.fillMaxWidth().weight(1f)) {

                val userLat = state.userLocation?.latitude  ?: 27.7172
                val userLon = state.userLocation?.longitude ?: 85.3240

                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(13.5)
                            controller.setCenter(GeoPoint(userLat, userLon))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()

                        // ── User location marker ──────────────────────────
                        Marker(mapView).apply {
                            position = GeoPoint(userLat, userLon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "📍 Your Location"
                            mapView.overlays.add(this)
                        }

                        // ── Hospital markers ──────────────────────────────
                        filtered.forEach { hospital ->
                            Marker(mapView).apply {
                                position = GeoPoint(hospital.latitude, hospital.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title    = hospital.name
                                snippet  = if (hospital.isFull) "⛔ FULL"
                                else "🛏 ${hospital.totalFreeBeds} beds · ${viewModel.formatDistance(hospital.distanceKm)}"
                                setOnMarkerClickListener { _, _ ->
                                    onHospitalClick(hospital.id)
                                    true
                                }
                                mapView.overlays.add(this)
                            }
                        }

                        // ── Straight-line route from user → selected hospital ──
                        // (A real road route requires a routing API; we draw a
                        //  straight dashed polyline which is the standard approach
                        //  for offline/OSM apps without a paid routing service.)
                        routeTarget?.let { dest ->
                            val routeLine = Polyline(mapView).apply {
                                addPoint(GeoPoint(userLat, userLon))
                                addPoint(GeoPoint(dest.latitude, dest.longitude))
                                outlinePaint.color = android.graphics.Color.parseColor("#1E3A8A")
                                outlinePaint.strokeWidth = 6f
                                outlinePaint.pathEffect = android.graphics.DashPathEffect(
                                    floatArrayOf(20f, 12f), 0f
                                )
                            }
                            mapView.overlays.add(routeLine)

                            // Zoom to fit both points
                            val box = org.osmdroid.util.BoundingBox.fromGeoPoints(
                                listOf(
                                    GeoPoint(userLat, userLon),
                                    GeoPoint(dest.latitude, dest.longitude)
                                )
                            )
                            mapView.zoomToBoundingBox(box.increaseByScale(1.3f), true)
                        } ?: run {
                            mapView.controller.setCenter(GeoPoint(userLat, userLon))
                        }

                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Hospital count badge
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Navy
                ) {
                    Text(
                        "${filtered.size} hospitals",
                        fontSize = 11.sp, color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // "Clear route" button when route is shown
                if (routeTarget != null) {
                    Surface(
                        Modifier.align(Alignment.TopStart).padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDC2626)
                    ) {
                        Row(
                            Modifier
                                .clickable { routeTarget = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear route", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (state.isLocating) {
                    CircularProgressIndicator(color = Navy, modifier = Modifier.align(Alignment.Center))
                }
            }

            // ── Nearby hospitals list ────────────────────────────────────────
            Column(Modifier.fillMaxWidth().background(Color.White)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby Hospitals", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (routeTarget != null) {
                        Text(
                            "Route → ${routeTarget!!.name}",
                            fontSize = 12.sp, color = Navy, fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text("${filtered.size} found", fontSize = 12.sp, color = TextSecond)
                    }
                }

                LazyColumn(
                    Modifier.heightIn(max = 300.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.take(10)) { hospital ->
                        MapHospitalRow(
                            hospital     = hospital,
                            distanceText = viewModel.formatDistance(hospital.distanceKm),
                            isRouteActive = routeTarget?.id == hospital.id,
                            onClick      = { onHospitalClick(hospital.id) },
                            // Blue arrow: draw route on map + open Google Maps / OsmAnd
                            // Source = user GPS, Destination = hospital coords
                            onNavigate   = { h ->
                                // 1. Draw route on the in-app map
                                routeTarget = h

                                // 2. Open external navigation app with SOURCE → DEST
                                val userLat = state.userLocation?.latitude  ?: 27.7172
                                val userLon = state.userLocation?.longitude ?: 85.3240

                                // Try Google Maps turn-by-turn first
                                val googleUri = Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1" +
                                            "&origin=$userLat,$userLon" +
                                            "&destination=${h.latitude},${h.longitude}" +
                                            "&travelmode=driving"
                                )
                                val googleIntent = Intent(Intent.ACTION_VIEW, googleUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (googleIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(googleIntent)
                                } else {
                                    // Fallback: OpenStreetMap in browser showing source → dest
                                    val osmUri = Uri.parse(
                                        "https://www.openstreetmap.org/directions" +
                                                "?engine=fossgis_osrm_car" +
                                                "&route=$userLat,$userLon;${h.latitude},${h.longitude}"
                                    )
                                    context.startActivity(Intent(Intent.ACTION_VIEW, osmUri))
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MapHospitalRow(
    hospital: Hospital,
    distanceText: String,
    isRouteActive: Boolean,
    onClick: () -> Unit,
    onNavigate: (Hospital) -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRouteActive) Color(0xFFEFF6FF) else Color(0xFFF9FAFB)
        ),
        border = if (isRouteActive) BorderStroke(1.5.dp, Navy) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (hospital.isFull) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalHospital, null,
                    tint = if (hospital.isFull) Color(0xFFDC2626) else Navy,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(hospital.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(hospital.address, fontSize = 11.sp, color = TextSecond)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (hospital.isFull) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFEE2E2)) {
                            Text("FULL", fontSize = 10.sp, color = Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFDCFCE7)) {
                            Text("${hospital.totalFreeBeds} beds", fontSize = 10.sp, color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFEFF6FF)) {
                        Text(distanceText, fontSize = 10.sp, color = Navy,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            // Blue navigation arrow — shows route on map + opens external navigation
            IconButton(onClick = { onNavigate(hospital) }) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(if (isRouteActive) Color(0xFF166534) else Navy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isRouteActive) Icons.Default.Check else Icons.Default.Navigation,
                        contentDescription = "Navigate to ${hospital.name}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}