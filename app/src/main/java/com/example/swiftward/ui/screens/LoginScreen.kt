package com.example.swiftward.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.swiftward.ui.navigation.Screen
import com.example.swiftward.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // --- State ---
    var phoneValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // Backend-driven state (loading / error / success)
    val state by viewModel.state.collectAsState()

    // Prefer a local validation message, otherwise show the backend error
    val errorMessage = localError ?: state.error

    // On successful login the repository has already saved the session.
    // Clear the auth screens off the backstack and go to the hospital list.
    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.resetSuccess()
            navController.navigate(Screen.Hospitals.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val fieldBackgroundColor = Color(0xFFF7F4F0) // Matching the off-white/cream fields in screenshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // --- Logo and Title ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1A3668)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock, // Replace with your actual building icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SwiftWard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Welcome back", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Sign in to pre-book beds and save patient profiles.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Form Fields ---
        Text("PHONE NUMBER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            // Country Code
            OutlinedTextField(
                value = "+977",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = fieldBackgroundColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Phone Number Input
            OutlinedTextField(
                value = phoneValue,
                onValueChange = { phoneValue = it; localError = null },
                placeholder = { Text("98XXXXXXXX") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = fieldBackgroundColor),
                isError = errorMessage != null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("PASSWORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        OutlinedTextField(
            value = passwordValue,
            onValueChange = { passwordValue = it; localError = null },
            placeholder = { Text("••••••••") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = fieldBackgroundColor),
            isError = errorMessage != null
        )

        // Forgot Password
        Text(
            text = "Forgot password?",
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            textAlign = TextAlign.End,
            color = Color(0xFF1A3668),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        // Error Message Pop-out
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Sign In Button ---
        Button(
            onClick = {
                if (phoneValue.isBlank() || passwordValue.isBlank()) {
                    localError = "Please enter your phone and password"
                } else {
                    localError = null
                    viewModel.login(phoneValue.trim(), passwordValue)
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1A3668))
            } else {
                Text("Sign in", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- Divider ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(" or continue with ", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        // --- Social Buttons ---
        Row(modifier = Modifier.fillMaxWidth()) {
            SocialButton(text = "Google", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            SocialButton(text = "GitHub", modifier = Modifier.weight(1f))
        }

        // --- Footer ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("No account? ", color = Color.Gray)
            Text(
                text = "Register here",
                color = Color(0xFF1A3668),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun SocialButton(text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { /* Handle Social Login */ },
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(text, color = Color.Black)
    }
}