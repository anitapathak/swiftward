package com.example.swiftward.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swiftward.ui.viewmodel.BookingViewModel.BookingViewModel
import com.swiftward.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onHospitalsClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onLogout: () -> Unit = {},
    userName: String = "",
    userPhone: String = "",
    userEmail: String = "",
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    val bookings by bookingViewModel.bookings.collectAsState()

    // Which dialog is open
    var openDialog by remember { mutableStateOf<String?>(null) }

    // Dialog handler
    when (openDialog) {
        "personal"  -> PersonalInfoDialog(userName, userPhone, userEmail) { openDialog = null }
        "medical"   -> MedicalHistoryDialog(bookings.size) { openDialog = null }
        "notif"     -> NotificationsDialog { openDialog = null }
        "privacy"   -> PrivacyDialog { openDialog = null }
    }

    Scaffold(
        bottomBar = {
            SwiftWardBottomBar(
                selected = 3,
                onHospitalsClick = onHospitalsClick,
                onMapClick = {},
                onBookingsClick = onBookingsClick,
                onProfileClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(Navy).padding(vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userName.ifBlank { "SwiftWard User" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(if (userPhone.isBlank()) "" else "+977 $userPhone", color = Color.White.copy(0.75f), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    // Verified badge
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF22C55E).copy(0.2f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Verified Account", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Bookings", bookings.size.toString(), Icons.Default.EventNote, Modifier.weight(1f))
                StatCard("Paid", "Rs ${bookings.sumOf { it.feePaid }}", Icons.Default.Payments, Modifier.weight(1f))
                StatCard("Status", "Active", Icons.Default.CheckCircle, Modifier.weight(1f))
            }

            // Account settings
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("ACCOUNT SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = Color.Gray, modifier = Modifier.padding(bottom = 10.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Person, title = "Personal Information",
                    subtitle = "${userName.ifBlank { "Not set" }} · ${userEmail.ifBlank { "No email" }}"
                ) { openDialog = "personal" }

                Spacer(Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Default.History, title = "Medical History",
                    subtitle = "${bookings.size} booking${if (bookings.size != 1) "s" else ""} · ${bookings.count { it.feePaid > 0 }} paid"
                ) { openDialog = "medical" }

                Spacer(Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Notifications, title = "Notifications",
                    subtitle = "Email alerts enabled"
                ) { openDialog = "notif" }

                Spacer(Modifier.height(10.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Shield, title = "Privacy & Security",
                    subtitle = "Password · Data · Account"
                ) { openDialog = "privacy" }
            }

            Spacer(Modifier.height(8.dp))

            // Logout
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFF991B1B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
fun PersonalInfoDialog(name: String, phone: String, email: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = Navy, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
                Spacer(Modifier.height(20.dp))
                InfoRow(Icons.Default.Badge,     "Full Name",     name.ifBlank { "Not available" })
                InfoRow(Icons.Default.Phone,     "Phone",         if (phone.isBlank()) "Not set" else "+977 $phone")
                InfoRow(Icons.Default.Email,     "Email",         email.ifBlank { "Not set" })
                InfoRow(Icons.Default.VerifiedUser, "Account",   "Verified ✅")
                InfoRow(Icons.Default.AppRegistration, "Member since", "2026")
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Close", color = Color.White) }
            }
        }
    }
}

@Composable
fun MedicalHistoryDialog(bookingCount: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Navy, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Medical History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
                Spacer(Modifier.height(20.dp))
                InfoRow(Icons.Default.EventNote,    "Total Bookings",    "$bookingCount")
                InfoRow(Icons.Default.LocalHospital,"Last Visit",        if (bookingCount > 0) "Recent" else "No visits yet")
                InfoRow(Icons.Default.Bloodtype,    "Blood Group",       "Update in booking form")
                InfoRow(Icons.Default.MedicalServices, "Conditions",     "Update per booking")
                InfoRow(Icons.Default.Vaccines,     "Allergies",         "None recorded")
                Spacer(Modifier.height(8.dp))
                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Your full medical history is recorded per booking. View 'My Bookings' for details.",
                        modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Navy
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Close", color = Color.White) }
            }
        }
    }
}

@Composable
fun NotificationsDialog(onDismiss: () -> Unit) {
    var emailAlerts   by remember { mutableStateOf(true) }
    var bookingAlerts by remember { mutableStateOf(true) }
    var promoAlerts   by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = Navy, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
                Spacer(Modifier.height(20.dp))
                SwitchRow("Email Alerts", "OTP & payment confirmations", emailAlerts) { emailAlerts = it }
                Spacer(Modifier.height(12.dp))
                SwitchRow("Booking Updates", "Status changes & reminders", bookingAlerts) { bookingAlerts = it }
                Spacer(Modifier.height(12.dp))
                SwitchRow("Promotions", "Tips & SwiftWard news", promoAlerts) { promoAlerts = it }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Save Preferences", color = Color.White) }
            }
        }
    }
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, null, tint = Navy, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Privacy & Security", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
                Spacer(Modifier.height(20.dp))
                InfoRow(Icons.Default.Lock,          "Password",          "Last changed: Account creation")
                InfoRow(Icons.Default.PhonelinkLock, "Two-Factor Auth",   "Via OTP (enabled)")
                InfoRow(Icons.Default.Storage,       "Data Storage",      "Secured · MongoDB Atlas")
                InfoRow(Icons.Default.Payment,       "Payment Security",  "Khalti encrypted gateway")
                InfoRow(Icons.Default.GppGood,       "Data Privacy",      "Your data is never sold")
                Spacer(Modifier.height(8.dp))
                Surface(color = Color(0xFFF0FFF4), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "SwiftWard uses AES-256 encryption for all sensitive data and JWT authentication for secure sessions.",
                        modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Color(0xFF166534)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Close", color = Color.White) }
            }
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Navy, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Navy, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Navy, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
    HorizontalDivider(color = Color(0xFFF3F4F6))
}

@Composable
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Navy)
        )
    }
}