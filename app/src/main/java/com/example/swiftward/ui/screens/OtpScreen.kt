package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.swiftward.ui.viewmodel.OtpViewModel

@Composable
fun OtpScreen(
    navController: NavHostController,
    email: String,
    viewModel: OtpViewModel = hiltViewModel() // Injected ViewModel
) {
    // State for the 6 OTP digits
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    var otpError by remember { mutableStateOf<String?>(null) }

    // Observe timer and resend state from ViewModel
    val timerValue by viewModel.timerValue.collectAsState()
    val canResend by viewModel.canResend.collectAsState()

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
                "OTP verification",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, top = 20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Phone Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFE8F0FE)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = Color(0xFF1A3668)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Verify your account", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Displays the email passed down cleanly via navigation
            Text(
                text = "We sent a 6-digit code to\n$email",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- OTP Input Boxes ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                otpValues.forEachIndexed { index, value ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                otpValues[index] = newValue
                                otpError = null
                            }
                        },
                        modifier = Modifier.width(45.dp).height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A3668),
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }
            }

            if (otpError != null) {
                Text(text = otpError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Verify & Continue Button ---
            Button(
                onClick = {
                    val fullOtp = otpValues.joinToString("")
                    if (fullOtp.length < 6) {
                        otpError = "Please enter all 6 digits"
                    } else {
                        // 👈 Note: This routes straight to dashboard on local UI check.
                        // If you add backend verification methods to OtpViewModel, you would trigger them here.
                        navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3668)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Verify & continue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Resend Logic ---
            if (canResend) {
                TextButton(onClick = { viewModel.resendOtp(email) }) {
                    Text("Didn't receive? Resend OTP", color = Color(0xFF1A3668), fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Resend in 0:${timerValue.toString().padStart(2, '0')}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Emergency Information Box ---
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = Color(0xFFE8F0FE),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "In an emergency you can skip login and browse hospitals freely.",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF1A3668),
                    fontSize = 14.sp
                )
            }
        }
    }
}