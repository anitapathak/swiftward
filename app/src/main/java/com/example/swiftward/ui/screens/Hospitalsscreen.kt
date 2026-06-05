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
import androidx.navigation.NavHostController
import com.example.swiftward.data.local.MockData
import com.example.swiftward.ui.FilterChipsRow
import com.example.swiftward.ui.SearchBarPlaceholder
import com.example.swiftward.ui.WardSmallPill
import com.example.swiftward.ui.navigation.Screen
// Change this at the top of HospitalsScreen.kt
import com.swiftward.data.model.Hospital
import com.swiftward.data.model.Ward
import com.swiftward.data.model.WardType
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalsScreen(
    onHospitalClick: (String) -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit // Added to match your bottom bar items
) {
    val hospitals = MockData.getAll()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SwiftWard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Kathmandu, Nepal", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        bottomBar = {
            SwiftWardBottomBar(
                selected = 0,
                onHospitalsClick = { /* Already here */ },
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
                SearchBarPlaceholder()
                FilterChipsRow()
                Text(
                    "SORTED BY DISTANCE · ${hospitals.size} HOSPITALS",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            itemsIndexed(hospitals) { index, hospital ->
                HospitalListItem(
                    hospital = hospital,
                    rank = index + 1,
                    onClick = { onHospitalClick(hospital.id.toString()) }
                )
            }
        }
    }
}

@Composable
fun HospitalListItem(hospital: Hospital, rank: Int, onClick: () -> Unit) {
    // Using your model's built-in properties
    val isFull = hospital.isFull
    val totalFree = hospital.totalFreeBeds

    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            // Rank Circle
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
                    // Using your 'shortName' property from the WardType enum
                    hospital.wards.take(2).forEach { ward ->
                        WardSmallPill("${ward.type.shortName} ${ward.freeBeds}")
                    }
                    if (hospital.wards.isNotEmpty()) {
                        Text("beds free", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Using distanceKm from your model
                Text(
                    text = if (isFull) "FULL" else "${hospital.distanceKm} km",
                    color = if (isFull) Color.Red else Color(0xFF166534),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (!isFull) {
                    Text("$totalFree", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
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
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple(Icons.Default.GridView, "Hospitals", onHospitalsClick),
            Triple(Icons.Default.Map, "Map", onMapClick),
            Triple(Icons.Default.EventNote, "Bookings", onBookingsClick),
            Triple(Icons.Default.Person, "Profile", onProfileClick)
        )

        items.forEachIndexed { idx, (icon, label, action) ->
            NavigationBarItem(
                selected = selected == idx,
                onClick = action,
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