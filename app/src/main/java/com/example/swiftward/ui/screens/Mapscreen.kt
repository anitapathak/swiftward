package com.example.swiftward.ui.screens


import android.content.Intent
import android.net.Uri
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.swiftward.data.model.Hospital
import com.swiftward.ui.theme.*
import com.swiftward.viewmodel.HospitalViewModel

// NOTE: For production, add these to build.gradle:
//   implementation("org.osmdroid:osmdroid-android:6.1.18")
//   implementation("com.google.android.gms:play-services-maps:18.2.0")
// Then replace the map placeholder with actual OSMDroid or Google Maps composable.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: HospitalViewModel = hiltViewModel(),
    onHospitalClick: (String) -> Unit,
    onHospitalsClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val state     by viewModel.state.collectAsState()
    val filtered  by viewModel.filteredHospitals.collectAsState()
    val context   = LocalContext.current

    var selectedHospital by remember { mutableStateOf<Hospital?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Map", fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            state.cityName.ifBlank { "Getting location..." },
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },

        bottomBar = {
            SwiftWardBottomBar(
                selected         = 2,
                onHospitalsClick = onHospitalsClick, // Changed from onHospitals
                onMapClick       = {},       // Changed from onMap
                onBookingsClick  = onBookingsClick,               // Changed from onBookings
                onProfileClick   = onProfileClick    // Changed from onProfile
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── Map placeholder with Google Maps deep-link ────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE8F4F8))
            ) {
                // ── Map visual representation ─────────────────────────────────
                // Integration point: replace this Box content with:
                //   AndroidView(factory = { MapView(it).apply { ... } })
                //   or GoogleMap composable from Maps Compose library

                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // User location pin
                    state.userLocation?.let { loc ->
                        Box(
                            Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Navy),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MyLocation, null,
                                tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Navy
                        ) {
                            Text(
                                "📍 Your location",
                                fontSize  = 13.sp,
                                color     = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier  = Modifier.padding(8.dp, 4.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Lat: %.4f  Lon: %.4f".format(loc.latitude, loc.longitude),
                            fontSize = 11.sp, color = TextSecond
                        )
                    } ?: run {
                        CircularProgressIndicator(color = Navy)
                        Spacer(Modifier.height(8.dp))
                        Text("Getting your location...", color = TextSecond)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Open in Google Maps button
                    state.userLocation?.let { loc ->
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse(
                                    "geo:${loc.latitude},${loc.longitude}?q=hospitals+near+me"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            shape  = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Navy)
                        ) {
                            Icon(Icons.Default.Map, null,
                                tint = Navy, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open in Google Maps", color = Navy, fontSize = 13.sp)
                        }
                    }
                }

                // ── Hospital count badge ──────────────────────────────────────
                Surface(
                    Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Navy
                ) {
                    Text(
                        "${filtered.size} hospitals",
                        fontSize  = 11.sp,
                        color     = Color.White,
                        modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Nearby hospitals horizontal list ──────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Nearby Hospitals",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("${filtered.size} found",
                        fontSize = 12.sp, color = TextSecond)
                }

                LazyColumn(
                    Modifier.heightIn(max = 300.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.take(8)) { hospital ->
                        MapHospitalRow(
                            hospital     = hospital,
                            distanceText = viewModel.formatDistance(hospital.distanceKm),
                            onClick      = { onHospitalClick(hospital.id) },
                            onNavigate   = { h ->
                                // Deep-link to Google Maps navigation
                                val uri = Uri.parse(
                                    "google.navigation:q=${h.latitude},${h.longitude}"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    // Fallback: browser maps
                                    val web = Uri.parse(
                                        "https://www.google.com/maps/dir/?api=1&destination=${h.latitude},${h.longitude}"
                                    )
                                    context.startActivity(Intent(Intent.ACTION_VIEW, web))
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
    onClick: () -> Unit,
    onNavigate: (Hospital) -> Unit
) {
    Card(
        onClick    = onClick,
        shape      = RoundedCornerShape(10.dp),
        colors     = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier   = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hospital icon
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hospital.isFull) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalHospital, null,
                    tint     = if (hospital.isFull) Color(0xFFDC2626) else Navy,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(hospital.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(hospital.address, fontSize = 11.sp, color = TextSecond)
                Row(
                    Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (hospital.isFull) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFEE2E2)) {
                            Text("FULL", fontSize = 10.sp, color = Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFDCFCE7)) {
                            Text("${hospital.totalFreeBeds} beds", fontSize = 10.sp,
                                color = Color(0xFF166534), fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFEFF6FF)) {
                        Text(distanceText, fontSize = 10.sp, color = Navy,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Navigate button
            IconButton(onClick = { onNavigate(hospital) }) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Navy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Navigation, null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}