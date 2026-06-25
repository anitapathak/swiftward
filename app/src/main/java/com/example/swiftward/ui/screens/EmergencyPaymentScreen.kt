package com.example.swiftward.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.viewmodel.PaymentViewModel
import com.swiftward.ui.theme.Navy

// eSewa removed — Khalti only as requested
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyPaymentScreen(
    bookingId: String,
    bedType: String = "ICU bed",
    hospitalName: String = "Bir Hospital",
    isLoggedIn: Boolean = true,           // controls whether Pay button is enabled
    onBack: () -> Unit,
    onPaymentSuccess: (txId: String) -> Unit,
    onPaymentFailure: (error: String) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val payState by viewModel.state.collectAsState()

    val khaltiPurple = Color(0xFF5C2D91)

    // When backend returns payment_url, open Khalti in browser
    LaunchedEffect(payState.paymentUrl) {
        payState.paymentUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    }

    // When payment verified → notify parent
    LaunchedEffect(payState.transactionId) {
        payState.transactionId?.let { txId ->
            onPaymentSuccess(txId)
        }
    }

    LaunchedEffect(payState.error) {
        payState.error?.let { err -> onPaymentFailure(err) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text("Payment", color = Color.White.copy(0.8f), fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Bed reservation fee", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Non-refundable booking deposit", color = Color.White.copy(0.7f), fontSize = 14.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Reservation fee", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    Text("Rs 300", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(4.dp))
                    Text("$bedType · $hospitalName", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Booking ID: $bookingId",
                        fontSize = 12.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Payment method: Khalti only (eSewa removed)
            Text("PAYMENT METHOD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(2.dp, khaltiPurple, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 40.dp)
                            .background(khaltiPurple, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Khalti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Khalti", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Digital wallet · Nepal", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.Lock, null, tint = khaltiPurple)
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Booking fee", color = Color.Gray, fontSize = 15.sp)
                Text("Rs 300", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Service charge", color = Color.Gray, fontSize = 15.sp)
                Text("Rs 0", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Rs 300", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy)
            }

            // Guest user lock message
            if (!isLoggedIn) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF92400E), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "You must be logged in to make a payment. Please register or log in first.",
                            color = Color(0xFF92400E), fontSize = 13.sp
                        )
                    }
                }
            }

            if (payState.error != null) {
                Text(payState.error!!, color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(8.dp))

            // Pay button — disabled for guests
            Button(
                onClick = {
                    viewModel.initiateKhaltiPayment(
                        bookingId = bookingId,
                        hospitalName = hospitalName,
                        wardType = bedType
                    )
                },
                enabled = isLoggedIn && !payState.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoggedIn) khaltiPurple else Color.Gray
                )
            ) {
                if (payState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (isLoggedIn) "Pay Rs 300 via Khalti" else "Login required to pay",
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            // Show "Verify Payment" button after Khalti redirect
            if (payState.pidx != null && payState.transactionId == null) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.verifyKhaltiPayment(
                            bookingId = bookingId,
                            hospitalName = hospitalName,
                            wardType = bedType
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, khaltiPurple),
                    enabled = !payState.isLoading
                ) {
                    Text("I've paid — Verify Payment", color = khaltiPurple, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                "Secured by Khalti · Payment is non-refundable once approved",
                fontSize = 11.sp, color = Color.Gray,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}