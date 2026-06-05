package com.example.swiftward.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.swiftward.R // Assuming you add the asset here
import com.example.swiftward.ui.navigation.Screen
import com.swiftward.ui.theme.SwiftWardTheme

// --- Define your theme colors here or import them ---
val NavyPrimary = Color(0xFF1A3668) // The Deep Navy from your design
val CreamBackground = Color(0xFFFAF6EE) // The light background from your design
val TextSecondary = Color(0xFF888888) // Light Gray for subtitles

@Composable
fun SplashScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. TOP BLUE BAR (Rounded Bottom Corners)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Height matching the status bar
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(NavyPrimary)
        )

        // MAIN CONTENT (Scaffold can cause issues with top bar padding, using simple Column)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(24.dp), // Main content padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Padding to account for the top bar
            Spacer(modifier = Modifier.height(72.dp)) // (Top bar height + internal spacing)

            // APPLOGO ICON
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                // Ensure you have an R.drawable.hospital_building icon
                Icon(
                    painter = painterResource(id = R.drawable.hospital_building),
                    contentDescription = null,
                    tint = Color.Unspecified, // Keep original colors
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TITLE AND SUBTITLE
            Text(
                text = "SwiftWard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Text(
                text = "Emergency bed booking.\nSave lives before arrival.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // INFO BADGES ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoBadge(title = "24/7", subtitle = "Live\nbeds")
                InfoBadge(title = "48+", subtitle = "Hospitals")
                InfoBadge(title = "<2m", subtitle = "To book")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. THE GRAPHICAL ILLUSTRATION (Hospital map with dotted line)
            GraphicalIllustration()

            Spacer(modifier = Modifier.height(32.dp))

            // 3. GET STARTED BUTTON (Matches your implementation, just adding elevation)
            Button(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Get started", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. BROWSE HOSPITALS BUTTON (Rounded, Clickable Box style)
            Button(
                onClick = { navController.navigate(Screen.Hospitals.route) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                // FIX: Light gray background for this button
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(
                    "Browse hospitals\nwithout account",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Helper: INFO BADGE
@Composable
fun InfoBadge(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEAE2)) // Matching the other boxes
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 18.sp)
        Text(subtitle, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

// Helper: GRAPHICAL ILLUSTRATION (Replicates the dotted line map)
@Composable
fun GraphicalIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEEEAE2)), // Main area background
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Light Blue Icon
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFD9E7FF)),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder, replace with actual R.drawable.hospital_outline
                Text("🏥", color = Color(0xFF6A99E5))
            }

            // RED DOTTED LINE
            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Central Hospital Icon with Red Plus
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFE8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥", color = Color.Red)
                }
                // Red circular pulse dot
                Box(
                    modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).offset(x=4.dp, y=4.dp)
                        .border(1.dp, Color.White, CircleShape).clip(CircleShape).background(Color.Red)
                )
            }

            // GREEN DOTTED LINE
            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Light Green Icon
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2F0D9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏥", color = Color(0xFF7CB342))
            }
        }
    }
}
