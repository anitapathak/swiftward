package com.example.swiftward.ui.screens

import android.os.Build
import androidx.compose.runtime.*

enum class EmergencyFlowStep {
    BOOKING_FORM,
    PAYMENT_GATEWAY,
    CONFIRMATION
}

@Composable
fun EmergencyWorkflowController(
    hospitalId: String,
    onWorkflowComplete: () -> Unit,
    onExitWorkflow: () -> Unit
) {
    var currentStep by remember { mutableStateOf(EmergencyFlowStep.BOOKING_FORM) }

    // States to pass tracking data through the wizard screens
    var activeBookingId by remember { mutableStateOf("") }
    var gatewayTransactionId by remember { mutableStateOf("") }

    when (currentStep) {
        // Step 1: Form Inputs
        EmergencyFlowStep.BOOKING_FORM -> {
            EmergencyBookingScreen(
                hospitalId = hospitalId,
                onBack = onExitWorkflow,
                onBookingConfirmed = { generatedBookingId ->
                    activeBookingId = generatedBookingId
                    currentStep = EmergencyFlowStep.PAYMENT_GATEWAY
                }
            )
        }

        // Step 2: Pay Rs. 300 via eSewa or Khalti
        EmergencyFlowStep.PAYMENT_GATEWAY -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                EmergencyPaymentScreen(
                    bookingId = activeBookingId,
                    bedType = "ICU bed",
                    hospitalName = if (hospitalId == "h1") "Bir Hospital" else "Hospital Details",
                    onBack = {
                        currentStep = EmergencyFlowStep.BOOKING_FORM
                    },
                    onPaymentSuccess = { txId ->
                        gatewayTransactionId = txId
                        currentStep = EmergencyFlowStep.CONFIRMATION // Jump to confirmation on success
                    },
                    onPaymentFailure = { errorMsg ->
                        // Optional: handle payment failures here
                    }
                )
            } // <-- THIS CLOSES EmergencyPaymentScreen
        } // <-- THIS CLOSES EmergencyFlowStep.PAYMENT_GATEWAY block

        // Step 3: Render Confirmed View
        EmergencyFlowStep.CONFIRMATION -> {
            BookingConfirmScreen(
                bookingId = activeBookingId,
                transactionId = gatewayTransactionId,
                onBackToHospitals = {
                    onWorkflowComplete() // Exit flow back to main dashboard
                }
            )
        }
    }
}