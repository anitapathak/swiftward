package com.example.swiftward.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.data.model.Hospital

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
    // Shared Hilt ViewModel instance to submit local state updates dynamically
    val bookingViewModel: BookingViewModel = hiltViewModel()

    // Collect the updated UI state to get access to the rich structured Booking data
    val uiState by bookingViewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(EmergencyFlowStep.BOOKING_FORM) }

    // State tracking reference for the transaction token gateway ID key string
    var gatewayTransactionId by remember { mutableStateOf("") }

    // Dynamic metadata fallback configuration matching your UI display components
    val targetedName = if (hospitalId == "h1") "Bir Hospital" else "Grande International"
    val targetedPhone = if (hospitalId == "h1") "01-4221111" else "01-4371234"

    when (currentStep) {
        // Step 1: Form Inputs (Completely Dynamic Workflow Integration)
        EmergencyFlowStep.BOOKING_FORM -> {
            EmergencyBookingScreen(
                hospitalId = hospitalId,
                onBack = onExitWorkflow,
                onBookingConfirmed = { dynamicBookingRequest ->

                    // Build minimal mock initialization payload matching your exact data schema constraints
                    val hospitalPayload = Hospital(
                        id = hospitalId,
                        name = targetedName,
                        address = "Kathmandu, Nepal",
                        latitude = 27.7000,
                        longitude = 85.3200,
                        phone = targetedPhone,
                        isOpen24x7 = true,
                        wards = emptyList()
                    )

                    // Commit form entries dynamically directly into the application memory pipeline
                    // This handles object creation safely inside your ViewModel
                    bookingViewModel.submitBooking(
                        request = dynamicBookingRequest,
                        hospital = hospitalPayload
                    )

                    // Safely advance step to the payment gate selection stage
                    currentStep = EmergencyFlowStep.PAYMENT_GATEWAY
                }
            )
        }

        // Step 2: Pay Rs. 300 via eSewa or Khalti
        EmergencyFlowStep.PAYMENT_GATEWAY -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                EmergencyPaymentScreen(
                    // Extracts real data safely directly out of the active generated state memory layout
                    bookingId = uiState.booking?.bookingId ?: "SW-GENERATING",
                    bedType = uiState.booking?.wardType?.name ?: "ICU Bed",
                    hospitalName = uiState.booking?.hospitalName ?: targetedName,
                    onBack = {
                        currentStep = EmergencyFlowStep.BOOKING_FORM
                    },
                    onPaymentSuccess = { txId ->
                        gatewayTransactionId = txId
                        currentStep = EmergencyFlowStep.CONFIRMATION // Jump to confirmation receipt details layout
                    },
                    onPaymentFailure = { errorMsg ->
                        // Optional: handle payment failures here
                    }
                )
            }
        }

        // Step 3: Render Confirmed View Receipt Landing Screen (Figure 3 matching layout design style specs)
// Step 3: Render Confirmed View Receipt Landing Screen (Figure 3 matching layout design style specs)
        EmergencyFlowStep.CONFIRMATION -> {
            BookingConfirmScreen(
                bookingId = uiState.booking?.bookingId ?: "SW-GENERATING", // ✅ Passed string ID
                transactionId = gatewayTransactionId,                    // ✅ Passed real gateway token string
                onBackToHospitals = {                                      // ✅ Matched exact parameter name
                    bookingViewModel.clearSuccess() // Reset flow tracker state machine flags
                    onWorkflowComplete()            // Safely route exit stack array pop operations back home!
                }
            )
        }
    }
}