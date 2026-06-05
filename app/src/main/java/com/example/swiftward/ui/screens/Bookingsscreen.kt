package com.example.swiftward.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.data.model.*
import com.swiftward.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: BookingViewModel = hiltViewModel(),
    onHospitalsClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBookings() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Bookings", fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Your bed reservation history",
                            fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        bottomBar = {
            SwiftWardBottomBar(
                selected         = 2,
                onHospitalsClick = onHospitalsClick, // Changed from onHospitals
                onMapClick       = onMapClick,       // Changed from onMap
                onBookingsClick  = {},               // Changed from onBookings
                onProfileClick   = onProfileClick    // Changed from onProfile
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy)
                }
            }
            state.bookings.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null,
                            modifier = Modifier.size(64.dp), tint = Color(0xFFD1D5DB))
                        Spacer(Modifier.height(16.dp))
                        Text("No bookings yet", fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(
                            "Your emergency bed bookings will appear here once you make a reservation.",
                            fontSize   = 13.sp,
                            color      = TextSecond,
                            textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier   = Modifier.padding(top = 8.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick  = onHospitalsClick,
                            colors   = ButtonDefaults.buttonColors(containerColor = Navy),
                            shape    = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Find nearby hospitals")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${state.bookings.size} BOOKING${if (state.bookings.size != 1) "S" else ""}",
                            fontSize     = 11.sp,
                            fontWeight   = FontWeight.SemiBold,
                            color        = TextSecond,
                            letterSpacing = 0.5.sp,
                            modifier     = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(state.bookings) { booking ->
                        BookingCard(booking)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking) {
    val sdf    = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val dateStr = sdf.format(Date(booking.createdAt))

    val (statusBg, statusFg, statusText) = when (booking.status) {
        BookingStatus.CONFIRMED  -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), "Confirmed")
        BookingStatus.PENDING    -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "Pending")
        BookingStatus.PREPARING  -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), "Preparing")
        BookingStatus.ARRIVED    -> Triple(Color(0xFFEFF6FF), Navy,              "Arrived")
        BookingStatus.CANCELLED  -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Cancelled")
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(booking.hospitalName, fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(dateStr, fontSize = 11.sp, color = TextSecond,
                        modifier = Modifier.padding(top = 2.dp))
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color(0xFFF3F4F6))

            // Booking details grid
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookingDetailItem(Icons.Default.Person,
                    "${booking.patient.name}, ${booking.patient.age}")
                BookingDetailItem(Icons.Default.Bed, booking.wardType.displayName)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BookingDetailItem(Icons.Default.MedicalServices, booking.condition)
                BookingDetailItem(Icons.Default.Bloodtype, booking.patient.bloodGroup)
            }

            Spacer(Modifier.height(8.dp))

            // Booking ID + payment
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                    Text(
                        booking.bookingId,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF92400E),
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    "Rs ${booking.feePaid} paid",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF166534)
                )
            }
        }
    }
}

@Composable
private fun BookingDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null,
            modifier = Modifier.size(14.dp), tint = TextSecond)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextPrimary)
    }
}