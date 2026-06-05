package com.example.swiftward.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onHospitalsClick: () -> Unit,
    onBookingsClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            // Reusing your existing bottom bar

        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB))
        ) {
            // --- Header Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Anita Pathak", // Mock user name
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "anita.pathak@email.com",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // --- Profile Options ---
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "ACCOUNT SETTINGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ProfileMenuItem(Icons.Default.PersonOutline, "Personal Information")
                ProfileMenuItem(Icons.Default.History, "Medical History")
                ProfileMenuItem(Icons.Default.NotificationsNone, "Notifications")
                ProfileMenuItem(Icons.Default.Shield, "Privacy & Security")

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { /* Handle Logout */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Navy, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 16.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
