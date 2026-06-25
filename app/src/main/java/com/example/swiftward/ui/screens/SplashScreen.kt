package com.example.swiftward.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.swiftward.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

val NavyPrimary    = Color(0xFF1A3668)
val CreamBackground = Color(0xFFFAF6EE)
val TextSecondary  = Color(0xFF888888)

@Composable
fun SplashScreen(
    navController: NavHostController,
    // FIX: use AuthViewModel (a real ViewModel) instead of SessionManager directly
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // FIX: isLoggedIn comes from ViewModel state, not directly from SessionManager
    LaunchedEffect(state.isLoggedIn) {
        // Only auto-redirect if we already know user is logged in
        if (state.isLoggedIn) {
            delay(1500)
            navController.navigate(Screen.Hospitals.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
        // If not logged in: stay on splash so user can tap "Get Started" or "Browse"
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // TOP BLUE BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(NavyPrimary)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // APP LOGO
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

            Text("SwiftWard", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text(
                "Emergency bed booking.\nSave lives before arrival.",
                fontSize = 16.sp, textAlign = TextAlign.Center,
                color = TextSecondary, modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InfoBadge("24/7", "Live\nbeds")
                InfoBadge("48+",  "Hospitals")
                InfoBadge("<2m",  "To book")
            }

            Spacer(modifier = Modifier.height(32.dp))
            GraphicalIllustration()
            Spacer(modifier = Modifier.height(32.dp))

            // GET STARTED → Login
            Button(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Get started", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BROWSE WITHOUT LOGIN → Hospitals (payment disabled for guests)
            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Hospitals.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyPrimary)
            ) {
                Text(
                    "Browse hospitals without account",
                    color = NavyPrimary,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
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
            ) { Text("🏥") }

            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(Color.Red, Offset(0f, 0.5f), Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            }

            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFE8E8)),
                    contentAlignment = Alignment.Center
                ) { Text("🏥") }
                Box(
                    modifier = Modifier.size(10.dp).align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .border(1.dp, Color.White, CircleShape)
                        .clip(CircleShape).background(Color.Red)
                )
            }

            Canvas(modifier = Modifier.width(48.dp).height(1.dp)) {
                drawLine(Color(0xFF4CAF50), Offset(0f, 0.5f), Offset(size.width, 0.5f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            }

            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2F0D9)),
                contentAlignment = Alignment.Center
            ) { Text("🏥") }
        }
    }
}