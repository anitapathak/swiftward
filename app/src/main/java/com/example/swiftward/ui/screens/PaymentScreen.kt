package com.example.swiftward.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PaymentScreen(
    bookingId: String,
    onPaymentSuccess: (String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("eSewa") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Your payment elements (Rs 300, eSewa and Khalti selectable custom list items) Go Here...

            Text("Select Payment Option", style = MaterialTheme.typography.titleMedium)

            // Example method switcher components:
            Button(onClick = { selectedMethod = "eSewa" }) { Text("Select eSewa") }
            Button(onClick = { selectedMethod = "Khalti" }) { Text("Select Khalti") }
        }

        // Action Payment button matching your screen layout
        Button(
            onClick = {
                // Generate a mockup transaction ID for validation tracking
                val mockTxId = "TXN-${(100000..999999).random()}"
                onPaymentSuccess(mockTxId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMethod == "eSewa") Color(0xFF4CAF50) else Color(0xFF5E35B1)
            )
        ) {
            Text(text = "Pay Rs 300 via $selectedMethod")
        }
    }
}