package com.example.swiftward.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftward.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyBookingScreen(
    hospitalId: String,
    onBack: () -> Unit,
    onBookingConfirmed: (String) -> Unit
) {
    // Form States
    var patientName by remember { mutableStateOf("anita pathak") }
    var age by remember { mutableStateOf("12") }
    var selectedGender by remember { mutableStateOf("Female") }
    var selectedCondition by remember { mutableStateOf("Cardiac arrest") }
    var estimatedTime by remember { mutableStateOf("~10 minutes") }
    var notesForStaff by remember { mutableStateOf("") }

    // Dropdown expanded configurations
    var genderExpanded by remember { mutableStateOf(false) }
    var etaExpanded by remember { mutableStateOf(false) }
    var bedTypeExpanded by remember { mutableStateOf(false) }

    // Bed options available
    val bedOptions = listOf(
        "General Ward (18 Beds Free)",
        "ICU (4 Beds Left)",
        "HDU (12 Beds Free)",
        "Pediatric (12 Beds Free)",
        "Emergency (12 Beds Free)"
    )
    var selectedBedType by remember { mutableStateOf(bedOptions[1]) }

    val orangeThematicColor = Color(0xFFD65A31)

    // Auto-update suggestion when condition chips are directly clicked
    LaunchedEffect(selectedCondition) {
        when (selectedCondition) {
            "Cardiac arrest" -> selectedBedType = "ICU (4 Beds Left)"
            "Stroke" -> selectedBedType = "HDU (12 Beds Free)"
            "Trauma", "Burns" -> selectedBedType = "Emergency (12 Beds Free)"
        }
    }

    // =======================================================================
    // FIX: Combined vertical scroll added to the root layout container.
    // This allows the entire screen view to slide up dynamically together.
    // =======================================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // --- Custom Orange Header Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(orangeThematicColor)
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hospitalId == "h1") "Bir Hospital" else "Hospital Details",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Emergency pre-booking",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hospital prepares before you arrive",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }

        // --- Form Fields ---
        // =======================================================================
        // FIX: Replaced .fillMaxSize() with .fillMaxWidth() so that vertical
        // heights wrap cleanly without conflicting with the root scrolling setup.
        // =======================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Patient Name & Age Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(0.65f)) {
                    FormLabel("PATIENT NAME")
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        placeholder = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy)
                    )
                }
                Column(modifier = Modifier.weight(0.35f)) {
                    FormLabel("AGE")
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        placeholder = { Text("45") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Field Dropdown
            FormLabel("GENDER")
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy)
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    listOf("Male", "Female", "Other").forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                selectedGender = gender
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- AI Triage Intelligent Assistant Component ---
            TriageAiAssistantComponent(
                onRecommendationReceived = { recommendedBed ->
                    selectedBedType = recommendedBed
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bed Type Field - MANUALLY SELECTABLE AND ASSISTED
            FormLabel("BED TYPE")
            ExposedDropdownMenuBox(
                expanded = bedTypeExpanded,
                onExpandedChange = { bedTypeExpanded = !bedTypeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBedType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bedTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy,
                        focusedContainerColor = Color(0xFFF1F3F4),
                        unfocusedContainerColor = Color(0xFFF1F3F4)
                    )
                )
                ExposedDropdownMenu(
                    expanded = bedTypeExpanded,
                    onDismissRequest = { bedTypeExpanded = false }
                ) {
                    bedOptions.forEach { bedOption ->
                        DropdownMenuItem(
                            text = { Text(bedOption) },
                            onClick = {
                                selectedBedType = bedOption
                                bedTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Condition Filter Chips Row
            FormLabel("CONDITION")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val conditions = listOf("Cardiac arrest", "Stroke", "Trauma", "Burns")
                conditions.forEach { condition ->
                    val isSelected = selectedCondition == condition
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCondition = condition },
                        label = { Text(condition) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFCE8E6),
                            selectedLabelColor = Color(0xFFA51D24)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color(0xFFF5B7B1),
                            borderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ETA Dropdown Field
            FormLabel("ETA TO HOSPITAL")
            ExposedDropdownMenuBox(
                expanded = etaExpanded,
                onExpandedChange = { etaExpanded = !etaExpanded }
            ) {
                OutlinedTextField(
                    value = estimatedTime,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = etaExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy)
                )
                ExposedDropdownMenu(
                    expanded = etaExpanded,
                    onDismissRequest = { etaExpanded = false }
                ) {
                    listOf("~10 minutes", "~20 minutes", "~30 minutes", "~45 minutes").forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time) },
                            onClick = {
                                estimatedTime = time
                                etaExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes for Staff Text Field
            FormLabel("NOTES FOR STAFF")
            OutlinedTextField(
                value = notesForStaff,
                onValueChange = { notesForStaff = it },
                placeholder = { Text("Patient unconscious, BP low...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Navy)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- Action Button Block ---
            Button(
                onClick = {
                    onBookingConfirmed("BK-${(1000..9999).random()}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = " Continuee to Payment ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            // Subtle buffer height at the very bottom to ensure sleek navigation bar margins
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageAiAssistantComponent(onRecommendationReceived: (String) -> Unit) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("heart pain") }
    var isListening by remember { mutableStateOf(false) }

    val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ne-NP")
        putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("ne-NP", "en-US"))
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the emergency situation...")
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val outputs = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val vocalText = outputs?.get(0) ?: ""
            textInput = vocalText

            val bedDecision = evaluateTriageScenario(vocalText)
            onRecommendationReceived(bedDecision)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFD2E3FC))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Triage Assistant (English / नेपाली)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A73E8)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Describe the symptoms or scenario via speech or text. The system automatically recommends the appropriate bed allocation context.",
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        val bedDecision = evaluateTriageScenario(it)
                        onRecommendationReceived(bedDecision)
                    },
                    placeholder = { Text("Type symptoms or context...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy,
                        focusedContainerColor = Color(0xFFF1F3F4),
                        unfocusedContainerColor = Color(0xFFF1F3F4)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        isListening = true
                        try {
                            speechLauncher.launch(speechIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
                            isListening = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (isListening) Color(0xFFD93025) else Color(0xFF1A73E8), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone Triage Action",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

fun evaluateTriageScenario(input: String): String {
    val cleanInput = input.lowercase()

    val icuKeywords = listOf(
        "icu", "heart attack", "cardiac", "unconscious", "stroke", "breathing", "coma", "accident",
        "मुटु", "अचेत", "सास फेर्न गाह्रो", "हर्ट अट्याक", "स्ट्रोक", "गम्भीर", "दुर्घटना"
    )

    val hduKeywords = listOf(
        "hdu", "oxygen", "fracture", "blood pressure", "high bp", "chest pain", "bleeding", "asthma",
        "अक्सिजन", "रगत", "फ्र्याक्चर", "प्रेशर", "छाती दुखेको", "दम"
    )

    return when {
        icuKeywords.any { cleanInput.contains(it) } -> "ICU (4 Beds Left)"
        hduKeywords.any { cleanInput.contains(it) } -> "HDU (12 Beds Free)"
        else -> "General Ward (18 Beds Free)"
    }
}