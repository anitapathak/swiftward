package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.data.model.Booking
import com.swiftward.data.model.BookingStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: BookingViewModel,
    onHospitalsClick: () -> Unit,
    onMapClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val bookings by viewModel.bookings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Bookings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Your bed reservation history", fontSize = 13.sp, color = Color.White.copy(0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A237E))
            )
        },
        bottomBar = {
            SwiftWardBottomBar(
                selected = 2,
                onHospitalsClick = onHospitalsClick,
                onMapClick = onMapClick,
                onBookingsClick = {},
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(innerPadding)
        ) {
            if (bookings.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🗓️", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No bookings yet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your emergency bed reservations will appear here after payment.",
                        fontSize = 14.sp, color = Color.Gray,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onHospitalsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Find nearby hospitals")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${bookings.size} BOOKINGS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Newest first", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    items(bookings) { booking ->
                        BookingHistoryCard(booking = booking)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingHistoryCard(booking: Booking) {
    val dateString = remember(booking.createdAt) {
        SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault()).format(Date(booking.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(booking.hospitalName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(booking.condition, fontSize = 13.sp, color = Color.Gray)
                    Text(dateString, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                }
                Surface(
                    color = if (booking.status == BookingStatus.CONFIRMED) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        booking.status.name.lowercase().replaceFirstChar { it.titlecase() },
                        color = if (booking.status == BookingStatus.CONFIRMED) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        booking.bookingId,
                        color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(color = Color(0xFFE8EAF6), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        booking.wardType.displayName,
                        color = Color(0xFF3F51B5), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (booking.transactionId.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF166534), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Payment verified · TX: ${booking.transactionId.take(16)}…",
                            color = Color(0xFF166534), fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.4f))
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${booking.patient.name}, ${booking.patient.age} · ${booking.patient.gender}", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💧", fontSize = 11.sp, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Blood group: ${booking.patient.bloodGroup}", fontSize = 14.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🕒", fontSize = 11.sp, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ETA ${booking.etaMinutes} min", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(0.4f))
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🩺", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(booking.assignedDoctor, fontSize = 14.sp, color = Color.DarkGray)
                }
                Text(
                    if (booking.feePaid > 0) "Rs ${booking.feePaid} paid" else "Unpaid",
                    color = if (booking.feePaid > 0) Color(0xFF2E7D32) else Color.Gray,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}