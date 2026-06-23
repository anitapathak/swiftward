package com.example.swiftward.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.swiftward.R
import com.example.swiftward.ui.navigation.Screen
import com.swiftward.utils.SessionManager
import kotlinx.coroutines.delay

val NavyPrimary = Color(0xFF1A3668)
val CreamBackground = Color(0xFFFAF6EE)
val TextSecondary = Color(0xFF888888)

@Composable
fun SplashScreen(
    navController: NavHostController,
    // ✅ ADDED: Injecting your utility SessionManager instance here via Hilt
    sessionManager: SessionManager = hiltViewModel()
) {
    // ✅ ADDED: Collect the asynchronous DataStore flow status
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

    // ✅ ADDED: Handle the routing check automatically on launch
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null) {
            // Give the user a brief moment to look at your beautiful illustration details
            delay(2000)

            if (isLoggedIn == true) {
                // Persistent token exists -> Route over to the dashboard layout immediately
                navController.navigate(Screen.Hospitals.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            // If isLoggedIn == false, we don't auto-redirect; we let them manually tap the buttons below!
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. TOP BLUE BAR (Rounded Bottom Corners)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(NavyPrimary)
        )

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(72.dp))

            // APPLOGO ICON
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.hospital_building),
                    contentDescription = null,
                    tint = Color.Unspecified,
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

            // 3. GET STARTED BUTTON (Wipes the Splash view so users cannot back track into it)
            Button(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Get started", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. BROWSE HOSPITALS BUTTON (Guest entry point clear logic)
            Button(
                onClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
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

@Composable
fun InfoBadge(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEAE2))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 18.sp)
        Text(subtitle, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
fun GraphicalIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEEEAE2)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFD9E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏥", color = Color(0xFF6A99E5))
            }

            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

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
                Box(
                    modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).offset(x=4.dp, y=4.dp)
                        .border(1.dp, Color.White, CircleShape).clip(CircleShape).background(Color.Red)
                )
            }

            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(0f, 0.5f),
                    end = Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2F0D9)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏥", color = Color(0xFF7CB342))
            }
        }
    }
}