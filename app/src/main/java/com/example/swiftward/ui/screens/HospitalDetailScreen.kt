package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDetailScreen(
    hospitalId: String,            // Received from NavGraph
    onBack: () -> Unit,            // Back button logic
    onEmergencyBook: (String) -> Unit // Navigates to booking form
) {
    // Note: In a real app, you would use hospitalId to fetch data from a ViewModel
    // For now, this UI matches your design requirements
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Details", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB))
        ) {
            // --- Hospital Header Info ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Hospital ID: $hospitalId", fontSize = 12.sp, color = Color.Gray)
                    Text("Current Ward Status", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(8.dp))
                    Text("Real-time bed availability for all departments.", fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            // --- Wards List ---
            Text(
                "AVAILABLE WARDS",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mock data to demonstrate the UI
                val mockWards = listOf("ICU", "HDU", "General Ward", "Pediatric", "Emergency")

                items(mockWards) { wardName ->
                    WardDetailCard(wardName)
                }
            }

            // --- Action Button ---
            Button(
                onClick = { onEmergencyBook(hospitalId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Proceed to Emergency Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WardDetailCard(name: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Updated 2 mins ago", fontSize = 12.sp, color = Color.Gray)
            }
            // Bed count pill
            Surface(
                color = if (name == "ICU") Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (name == "ICU") "2 Beds Left" else "12 Beds Free",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (name == "ICU") Color(0xFF991B1B) else Color(0xFF166534),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}