package com.example.swiftward.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyPaymentScreen(
    bookingId: String,
    bedType: String = "ICU Bed",
    hospitalName: String = "Hospital",
    isLoggedIn: Boolean = true,
    onBack: () -> Unit,
    onPaymentSuccess: (txId: String) -> Unit,
    onPaymentFailure: (error: String) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val state    by viewModel.state.collectAsState()
    val khaltiPurple = Color(0xFF5C2D91)

    // Track whether user has opened Khalti — show "I've paid" button after
    var khaltiOpened by remember { mutableStateOf(false) }

    // Open Khalti payment URL in browser when received
    LaunchedEffect(state.paymentUrl) {
        state.paymentUrl?.let { url ->
            khaltiOpened = true
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.clearPaymentUrl()
        }
    }

    // Track when app resumes after user visits Khalti
    var appResumed by remember { mutableStateOf(false) }

    // Detect when user comes back to this screen from Khalti browser
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { }
    }
    LaunchedEffect(khaltiOpened) {
        if (khaltiOpened) {
            // Wait 8 seconds after opening Khalti before starting to poll
            // This gives user time to pay and return to app
            delay(8000L)
            appResumed = true
        }
    }

    // AUTO-POLL: only after user has had time to pay (8s delay + every 5s)
    LaunchedEffect(appResumed, state.pidx) {
        if (appResumed && state.pidx != null) {
            // Poll up to 36 times (3 min) — stops when payment confirmed
            repeat(36) {
                if (state.transactionId == null) {
                    viewModel.verifyKhaltiPayment(
                        bookingId    = bookingId,
                        hospitalName = hospitalName,
                        wardType     = bedType
                    )
                    delay(5000L)
                }
            }
        }
    }

    // Clear "Pending" error when Khalti page opens — status will update after payment
    LaunchedEffect(khaltiOpened) {
        if (khaltiOpened) viewModel.clearError()
    }

    // On success navigate to confirmation
    LaunchedEffect(state.transactionId) {
        state.transactionId?.let { txId -> onPaymentSuccess(txId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A3668))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Amount card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Reservation fee", fontSize = 15.sp, color = Color.Gray)
                    Text("Rs 300", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3668))
                    Text("$bedType · $hospitalName", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(6.dp))
                    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "Booking ID: $bookingId",
                            color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Khalti payment method
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, khaltiPurple)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 36.dp)
                            .background(khaltiPurple, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("Khalti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Khalti", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Digital wallet · Nepal", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.Lock, null, tint = khaltiPurple)
                }
            }

            // Summary row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Rs 300", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1A3668))
            }

            // Guest lock message
            if (!isLoggedIn) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFEF3C7), shape = RoundedCornerShape(10.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color(0xFF92400E), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Login required to make a payment.", color = Color(0xFF92400E), fontSize = 13.sp)
                    }
                }
            }

            // Error display
            state.error?.let {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFEE2E2), shape = RoundedCornerShape(10.dp)) {
                    Text(it, color = Color(0xFFDC2626), fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }

            // Auto-polling status
            if (khaltiOpened && state.transactionId == null && state.error == null) {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFEFF6FF), shape = RoundedCornerShape(10.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF1A3668))
                        Spacer(Modifier.width(10.dp))
                        Text("Waiting for payment confirmation…", color = Color(0xFF1A3668), fontSize = 13.sp)
                    }
                }
            }

            // PAY BUTTON
            Button(
                onClick = {
                    viewModel.initiateKhaltiPayment(
                        bookingId    = bookingId,
                        hospitalName = hospitalName,
                        wardType     = bedType
                    )
                },
                enabled = isLoggedIn && !state.isLoading && !khaltiOpened,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoggedIn && !khaltiOpened) khaltiPurple else Color.Gray
                )
            ) {
                if (state.isLoading && !khaltiOpened) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        when {
                            !isLoggedIn   -> "Login required to pay"
                            khaltiOpened  -> "Payment in progress…"
                            else          -> "Pay Rs 300 via Khalti"
                        },
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            // MANUAL VERIFY BUTTON — shown after Khalti opened (fallback if auto-poll misses)
            if (khaltiOpened && state.transactionId == null) {
                OutlinedButton(
                    onClick = {
                        viewModel.verifyKhaltiPayment(
                            bookingId    = bookingId,
                            hospitalName = hospitalName,
                            wardType     = bedType
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, khaltiPurple),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = khaltiPurple)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("I've paid — Verify Payment", color = khaltiPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Text(
                "Secured by Khalti · Payment is non-refundable once approved",
                fontSize = 11.sp, color = Color.Gray,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}