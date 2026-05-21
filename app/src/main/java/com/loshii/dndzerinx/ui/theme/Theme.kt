package com.loshii.dndzerinx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val FantasyColors = darkColorScheme(
    primary = Color(0xFF9F7AEA),
    secondary = Color(0xFF67E8F9),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E2937),
    surfaceVariant = Color(0xFF334155),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
)

private val FantasyTypography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
)

@Composable
fun DnDRoleGateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FantasyColors,
        typography = FantasyTypography,
        content = content
    )
}
