package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun RegisterScreen(
    navController: NavHostController,
    onNavigateToLogin: () -> Unit
) {
    // --- State Management ---
    var fullName by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fieldBg = Color(0xFFF7F4F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Dark Blue Header (Matches Screenshot) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF1A3668)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "Create account",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp)
            )
        }

        // --- Form Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("Join SwiftWard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Create an account to pre-book emergency beds.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            RegisterLabel("FULL NAME")
            CustomTextField(fullName, { fullName = it }, "Your full name")

            RegisterLabel("PHONE NUMBER")
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = "+977",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.width(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = fieldBg)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Note: weight(1f) is valid here because it's inside a Row
                CustomTextField(
                    value = phoneValue,
                    onValueChange = { phoneValue = it },
                    placeholder = "98XXXXXXXX",
                    modifier = Modifier.weight(1f)
                )
            }

            RegisterLabel("EMAIL (optional)")
            CustomTextField(emailValue, { emailValue = it }, "your@email.com")

            RegisterLabel("PASSWORD")
            CustomTextField(passwordValue, { passwordValue = it }, "Min 8 characters", isPassword = true)

            RegisterLabel("CONFIRM PASSWORD")
            CustomTextField(confirmPasswordValue, { confirmPasswordValue = it }, "Re-enter password", isPassword = true)

            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1A3668))
                )
                Text("I agree to the Terms of Service and Privacy Policy", fontSize = 12.sp)
            }

            // Error Message Pop-out
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Create Account Button ---
            Button(
                onClick = {
                    when {
                        fullName.isBlank() || phoneValue.isBlank() || passwordValue.isBlank() -> {
                            errorMessage = "Required fields cannot be empty"
                        }
                        passwordValue.length < 8 -> {
                            errorMessage = "Password must be at least 8 characters"
                        }
                        passwordValue != confirmPasswordValue -> {
                            errorMessage = "Passwords do not match"
                        }
                        !isAgreed -> {
                            errorMessage = "You must agree to the terms"
                        }
                        else -> {
                            errorMessage = null
                            // Pass the number to the OTP screen
                            navController.navigate("otp_screen/$phoneValue")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create account", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color.Gray)
                Text(
                    "Sign in",
                    color = Color(0xFF1A3668),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

// --- Helper UI Components ---

@Composable
fun RegisterLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.DarkGray,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF7F4F0),
            focusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF1A3668),
            unfocusedBorderColor = Color.LightGray
        ),
        singleLine = true
    )
}