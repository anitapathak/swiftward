package com.example.swiftward.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

enum class PaymentMethod { ESEWA, KHALTI }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyPaymentScreen(
    bookingId: String,
    bedType: String = "ICU bed",
    hospitalName: String = "Bir Hospital",
    onBack: () -> Unit,
    onPaymentSuccess: (String) -> Unit,
    onPaymentFailure: (error: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.ESEWA) }

    val esewaGreen = Color(0xFF55B540)
    val khaltiPurple = Color(0xFF5C2D91)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Dark Navy Top Header (Matching Reference Screenshot) ---
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
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "7 · payment",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bed reservation fee",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Non-refundable booking deposit",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Center Total Fee Card Display ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Reservation fee",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs 300",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$bedType · $hospitalName",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Text(
                text = "CHOOSE PAYMENT METHOD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )

            // --- eSewa Option Row ---
            PaymentSelectorRow(
                title = "eSewa",
                subtitle = "Digital wallet",
                logoText = "eSewa",
                logoBg = esewaGreen,
                isSelected = selectedMethod == PaymentMethod.ESEWA,
                selectedBorderColor = esewaGreen,
                onClick = { selectedMethod = PaymentMethod.ESEWA }
            )

            // --- Khalti Option Row ---
            PaymentSelectorRow(
                title = "Khalti",
                subtitle = "Digital wallet",
                logoText = "Khalti",
                logoBg = khaltiPurple,
                isSelected = selectedMethod == PaymentMethod.KHALTI,
                selectedBorderColor = khaltiPurple,
                onClick = { selectedMethod = PaymentMethod.KHALTI }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))

            // --- Pricing Itemization Breakdowns ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Booking fee", color = Color.Gray, fontSize = 15.sp)
                Text(text = "Rs 300", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Service charge", color = Color.Gray, fontSize = 15.sp)
                Text(text = "Rs 0", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = "Rs 300", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Navy)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Dynamic Unified Pay CTA Action Button ---
            Button(
                onClick = {
                    // Generate a local mock transaction reference ID to jump straight to the booking success screen
                    val mockTxId = "TXN-${(100000..999999).random()}"
                    onPaymentSuccess(mockTxId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMethod == PaymentMethod.ESEWA) esewaGreen else khaltiPurple
                )
            ) {
                Text(
                    text = "Pay Rs 300 via ${if (selectedMethod == PaymentMethod.ESEWA) "eSewa" else "Khalti"}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Secured by ${if (selectedMethod == PaymentMethod.ESEWA) "eSewa" else "Khalti"} · Payment is non-refundable once approved",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PaymentSelectorRow(
    title: String,
    subtitle: String,
    logoText: String,
    logoBg: Color,
    isSelected: Boolean,
    selectedBorderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedBorderColor else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 75.dp, height = 38.dp)
                    .background(logoBg, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = logoText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = selectedBorderColor)
            )
        }
    }
}