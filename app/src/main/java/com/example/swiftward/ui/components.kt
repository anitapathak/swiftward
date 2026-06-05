package com.example.swiftward.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.swiftward.ui.screens.TextSecondary
import com.example.swiftward.ui.theme.NavyPrimary
import com.example.swiftward.ui.theme.RedEmergency
import com.swiftward.data.model.*
import com.swiftward.data.model.WardType.*
import com.swiftward.ui.theme.*
import  androidx.compose.foundation.lazy.items

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwiftWardTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.White)
                subtitle?.let { Text(it, fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f)) }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
    )
}

// ── Bed type pill chip ────────────────────────────────────────────────────────

@Composable
fun BedTypePill(ward: Ward) {
    val (bg, fg) = when (ward.type) {
        GENERAL -> Color(0xFFE1F5EE) to Color(0xFF085041)
        ICU -> Color(0xFFFAECE7) to Color(0xFF712B13)
        HDU -> Color(0xFFFAEEDA) to Color(0xFF633806)
        PEDIATRIC -> Color(0xFFF4C0D1) to Color(0xFF72243E)
        MATERNITY -> Color(0xFFEEEDFE) to Color(0xFF3C3489)
        EMERGENCY -> Color(0xFFFCEBEB) to Color(0xFF791F1F)
        BURN -> TODO()
        ORTHOPEDIC -> TODO()
        CARDIAC -> TODO()
        NEUROLOGY -> TODO()
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            "${ward.type.displayName} ${ward.freeBeds}",
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Hospital card ─────────────────────────────────────────────────────────────

@Composable
fun HospitalCard(
    hospital: Hospital,
    rank: Int,
    distanceText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hospital.isFull)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (hospital.isFull) Color(0xFFF7C1C1) else Color(0xFFE6F1FB)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hospital.isFull) Color(0xFF791F1F) else Color(0xFF0C447C)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(hospital.name, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(hospital.address, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(top = 1.dp))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    hospital.wards.take(3).forEach { BedTypePill(it) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (hospital.isFull) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF7C1C1)) {
                        Text("FULL", fontSize = 9.sp, fontWeight = FontWeight.Medium,
                            color = Color(0xFF791F1F), modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(distanceText, fontSize = 10.sp, color = TextSecondary)
                } else {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE1F5EE)) {
                        Text(distanceText, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            color = Color(0xFF085041), modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${hospital.totalFreeBeds}", fontSize = 16.sp,
                        fontWeight = FontWeight.Medium, color = NavyPrimary)
                    Text("beds free", fontSize = 9.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ── Loading spinner ───────────────────────────────────────────────────────────

@Composable
fun LoadingIndicator(message: String = "Loading…") {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = NavyPrimary)
        Spacer(Modifier.height(12.dp))
        Text(message, color = TextSecondary, fontSize = 13.sp)
    }
}

// ── Primary & Emergency buttons ───────────────────────────────────────────────

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
        shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
}

@Composable
fun EmergencyButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
        shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White) }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}
@Composable
fun SearchBarPlaceholder() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Search hospital or specialty...", fontSize = 15.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedBorderColor = Color(0xFF1E3A8A), // Navy
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedContainerColor = Color(0xFFF3F4F6)
        )
    )
}

@Composable
fun FilterChipsRow() {
    val filters = listOf("All", "ICU", "Pediatric", "General")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        // Change this line in your components.kt
        items(filters) { label ->
            FilterChip(
                selected = label == "All",
                onClick = { /* Update filter in ViewModel */ },
                label = { Text(label) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1E3A8A),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun WardSmallPill(label: String) {
    // Logic to determine color based on type (e.g., ICU = Red, General = Green)
    val bgColor = when {
        label.contains("General") -> Color(0xFFDCFCE7)
        label.contains("ICU") -> Color(0xFFFEE2E2)
        else -> Color(0xFFFEF3C7)
    }
    val textColor = when {
        label.contains("General") -> Color(0xFF166534)
        label.contains("ICU") -> Color(0xFF991B1B)
        else -> Color(0xFF92400E)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
