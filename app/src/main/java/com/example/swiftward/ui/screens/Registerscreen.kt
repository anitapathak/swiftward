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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.swiftward.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // --- State Management ---
    var fullName by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }

    // Local validation error message holder (Frontend only)
    var localValidationError by remember { mutableStateOf<String?>(null) }

    // Observe unified backend state (Loading, Success, Error)
    val state by viewModel.state.collectAsState()

    // Listen for backend registration success, then trigger navigation
    LaunchedEffect(state.success) {
        if (state.success) {
            val cleanEmail = emailValue.trim()
            val cleanPhone = phoneValue.trim()
            viewModel.resetSuccess() // Reset success flag in ViewModel
            // Pass phone (used to verify the OTP) and email (shown to the user)
            navController.navigate("otp_screen/$cleanPhone/$cleanEmail") {
                // Clear registration page out of backstack history
                popUpTo("register_route") { inclusive = true }
            }
        }
    }

    // Determine what error message string to render (Prefer local validation, fallback to backend)
    val displayError = localValidationError ?: state.error

    val fieldBg = Color(0xFFF7F4F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Dark Blue Header ---
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
            CustomTextField(fullName, { fullName = it; localValidationError = null }, "Your full name")

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
                CustomTextField(
                    value = phoneValue,
                    onValueChange = { phoneValue = it; localValidationError = null },
                    placeholder = "98XXXXXXXX",
                    modifier = Modifier.weight(1f)
                )
            }

            RegisterLabel("EMAIL")
            CustomTextField(emailValue, { emailValue = it; localValidationError = null }, "your@email.com")

            RegisterLabel("PASSWORD")
            CustomTextField(passwordValue, { passwordValue = it; localValidationError = null }, "Min 8 characters", isPassword = true)

            RegisterLabel("CONFIRM PASSWORD")
            CustomTextField(confirmPasswordValue, { confirmPasswordValue = it; localValidationError = null }, "Re-enter password", isPassword = true)

            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it; localValidationError = null },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1A3668))
                )
                Text("I agree to the Terms of Service and Privacy Policy", fontSize = 12.sp)
            }

            // Consolidated Error Message Field Display
            if (displayError != null) {
                Text(
                    text = displayError,
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
                        fullName.isBlank() || phoneValue.isBlank() || emailValue.isBlank() || passwordValue.isBlank() -> {
                            localValidationError = "Required fields cannot be empty"
                        }
                        (!emailValue.contains("@") || !emailValue.contains(".")) -> {
                            localValidationError = "Please enter a valid email address"
                        }
                        passwordValue.length < 8 -> {
                            localValidationError = "Password must be at least 8 characters"
                        }
                        passwordValue != confirmPasswordValue -> {
                            localValidationError = "Passwords do not match"
                        }
                        !isAgreed -> {
                            localValidationError = "You must agree to the terms"
                        }
                        else -> {
                            localValidationError = null
                            viewModel.register(
                                name = fullName.trim(),
                                phone = phoneValue.trim(),
                                email = emailValue.trim(),
                                password = passwordValue
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1A3668))
                } else {
                    Text("Create account", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
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