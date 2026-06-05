package com.swiftward.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────────



val Navy         = Color(0xFF1B3A6B)   // top bar, rank badges, selected tab
val NavyDark     = Color(0xFF0F2447)
val RedAccent    = Color(0xFFD85A30)   // ICU pills, emergency button
val GreenAccent  = Color(0xFF1D9E75)   // General pills, distance badges
val AmberHDU     = Color(0xFFBA7517)   // HDU pills
val PinkPeds     = Color(0xFFD4537E)   // Pediatric pills
val TextPrimary  = Color(0xFF111827)
val TextSecond   = Color(0xFF6B7280)
val TextTertiary = Color(0xFF9CA3AF)
val SurfaceBg    = Color(0xFFF5F5F5)

private val LightColors = lightColorScheme(
    primary           = Navy,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFEFF6FF),
    secondary         = RedAccent,
    onSecondary       = Color.White,
    background        = SurfaceBg,
    surface           = Color.White,
    onBackground      = TextPrimary,
    onSurface         = TextPrimary,
    outline           = Color(0xFFE5E7EB)
)

@Composable
fun SwiftWardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography(),
        content     = content
    )
}
