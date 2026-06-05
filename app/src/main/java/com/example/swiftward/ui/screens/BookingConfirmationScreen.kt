package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

@Composable
fun BookingConfirmScreen(
    bookingId: String,               // Passed from NavGraph
    onBackToHospitals: () -> Unit    // Navigates back to the main list
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Success Icon ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color(0xFF166534),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // --- Confirmation Text ---
        Text(
            text = "Booking Confirmed!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Your emergency request has been sent to the hospital. Please proceed to the emergency ward immediately.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(40.dp))

        // --- Booking ID Card ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF3F4F6),
            border = null
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BOOKING REFERENCE ID",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bookingId,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Navy,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // --- Navigation Button ---
        Button(
            onClick = onBackToHospitals,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy)
        ) {
            Text(
                "Back to Home",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}