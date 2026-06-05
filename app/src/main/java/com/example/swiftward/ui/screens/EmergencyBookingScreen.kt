package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyBookingScreen(
    hospitalId: String,                // Ensure this matches your NavGraph val
    onBack: () -> Unit,                // For navController.popBackStack()
    onBookingConfirmed: (String) -> Unit // Navigates to confirmation with bookingId
) {
    var patientName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Booking", color = Color.White) },
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
                .padding(16.dp)
        ) {
            Text(
                "Booking for Hospital ID: $hospitalId",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Patient Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Emergency Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    // In a real app, you'd call a ViewModel here.
                    // For now, we pass a mock booking ID.
                    onBookingConfirmed("BK-${(1000..9999).random()}")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                enabled = patientName.isNotBlank() && contactNumber.isNotBlank()
            ) {
                Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}