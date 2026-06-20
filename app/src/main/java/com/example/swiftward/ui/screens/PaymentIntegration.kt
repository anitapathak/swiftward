package com.example.swiftward.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * eSewa Generation API (eSewa Protocol 2024+)
 * Formats data and appends the required cryptographic signature.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun initiateEsewaPaymentPayload(bookingId: String, amount: String): String {
    val merchantCode = "EPAYTEST" // Replace with production merchant code
    val secretKey = "8g8MMg8MMg9M" // Replace with production secret key

    val totalAmount = amount
    val transactionUuid = "${bookingId}-${System.currentTimeMillis()}"
    val productCode = "EPAYTEST"

    // Order matters exactly: total_amount,transaction_uuid,product_code
    val signatureMessage = "total_amount=$totalAmount,transaction_uuid=$transactionUuid,product_code=$productCode"

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secretKey.toByteArray(), "HmacSHA256"))
    val rawSignature = mac.doFinal(signatureMessage.toByteArray())
    val base64Signature = Base64.getEncoder().encodeToString(rawSignature)
    // Construct standard web payment endpoint form parameters
    return "https://rc-epay.esewa.com.np/api/epay/main/v2/form?" +
            "amount=$amount" +
            "&tax_amount=0" +
            "&total_amount=$totalAmount" +
            "&transaction_uuid=$transactionUuid" +
            "&product_code=$productCode" +
            "&product_service_charge=0" +
            "&product_delivery_charge=0" +
            "&success_url=https://swiftward.com.np/payment-success" +
            "&failure_url=https://swiftward.com.np/payment-failed" +
            "&signed_field_names=total_amount,transaction_uuid,product_code" +
            "&signature=$base64Signature"
}

/**
 * Khalti Merchant Checkout initiation API.
 * Calls the `v2/epayment/initiate/` endpoint to receive a checkout dynamic web URL.
 */
fun initiateKhaltiPaymentApi(
    bookingId: String,
    amountInPaisa: Long,
    onSuccess: (paymentUrl: String) -> Unit,
    onFailure: (String) -> Unit
) {
    // In production, move network calls to an asynchronous OkHttp/Retrofit repository architecture
    Thread {
        try {
            // Live/Sandbox target url context setup
            val url = java.net.URL("https://a.khalti.com/api/v2/epayment/initiate/")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Key live_secret_key_xxxx") // Replace with secure Khalti Secret Key
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonPayload = """
                {
                    "return_url": "https://swiftward.com.np/payment-success",
                    "website_url": "https://swiftward.com.np",
                    "amount": $amountInPaisa,
                    "purchase_order_id": "$bookingId",
                    "purchase_order_name": "SwiftWard Bed Reservation Deposit"
                }
            """.trimIndent()

            conn.outputStream.use { os -> os.write(jsonPayload.toByteArray()) }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                // Parse the response string to find the payment URL
                val paymentUrl = response.substringAfter("\"payment_url\":\"").substringBefore("\"")
                onSuccess(paymentUrl)
            } else {
                onFailure("Khalti server error code: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            onFailure(e.localizedMessage ?: "Connection error occured.")
        }
    }.start()
}