package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.viewmodel.PaymentViewModel

/**
 * This screen is shown after Khalti redirects back via the deep-link:
 *   swiftward://payment/callback?pidx=xxx&status=Completed&purchase_order_id=SW-xxx
 *
 * It auto-calls /api/khalti/verify on mount, shows a spinner,
 * then calls onSuccess(txId) or onFailure().
 */
@Composable
fun KhaltiCallbackScreen(
    pidx: String,
    bookingId: String,
    hospitalName: String,
    wardType: String,
    onSuccess: (txId: String) -> Unit,
    onFailure: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Auto-verify as soon as we land here
    LaunchedEffect(pidx) {
        if (pidx.isNotBlank()) {
            viewModel.verifyKhaltiPayment(
                bookingId    = bookingId,
                hospitalName = hospitalName,
                wardType     = wardType
            )
        }
    }

    // Navigate on result
    LaunchedEffect(state.transactionId) {
        state.transactionId?.let { txId -> onSuccess(txId) }
    }
    LaunchedEffect(state.error) {
        state.error?.let { onFailure() }
    }

    // UI — shown while verifying (usually just 1-2 seconds)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            if (state.isLoading || (!state.isLoading && state.transactionId == null && state.error == null)) {
                CircularProgressIndicator(
                    color = Color(0xFF5C2D91),
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 5.dp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Verifying your payment…",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3668)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Please wait, do not close the app.",
                    fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(10.dp)) {
                    Text(
                        "Booking ID: $bookingId",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Medium
                    )
                }
            } else if (state.error != null) {
                Box(
                    Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFDC2626), modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Payment Verification Failed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                Spacer(Modifier.height(8.dp))
                Text(state.error ?: "Unknown error", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onFailure,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3668)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Go back to hospitals", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
