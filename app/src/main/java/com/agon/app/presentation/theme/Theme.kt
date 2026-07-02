package com.agon.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Casino Colors ───────────────────────────────────
private val FeltGreen = Color(0xFF1B5E20)
private val FeltDark = Color(0xFF0D3B10)
private val FeltLight = Color(0xFF2E7D32)
private val WoodBrown = Color(0xFF5D4037)
private val WoodLight = Color(0xFF8D6E63)
private val GoldAccent = Color(0xFFFFD700)
private val GoldDark = Color(0xFFB8860B)
private val Ivory = Color(0xFFFFF8E1)
private val DominoBg = Color(0xFFFFF8E1)
private val DominoBorder = Color(0xFF2E3B28)
private val RedFelt = Color(0xFFB71C1C)

// Light Theme (Casino Table)
private val LightColors = lightColorScheme(
    primary = FeltGreen,
    onPrimary = Color.White,
    primaryContainer = FeltLight,
    onPrimaryContainer = Color.White,
    secondary = WoodBrown,
    onSecondary = Color.White,
    secondaryContainer = WoodLight,
    onSecondaryContainer = Color.White,
    tertiary = GoldAccent,
    onTertiary = FeltDark,
    tertiaryContainer = GoldDark,
    onTertiaryContainer = Color.White,
    background = FeltDark,
    onBackground = Color.White,
    surface = WoodBrown.copy(alpha = 0.9f),
    onSurface = Color.White,
    surfaceVariant = FeltGreen.copy(alpha = 0.7f),
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    error = RedFelt,
    onError = Color.White,
    errorContainer = RedFelt.copy(alpha = 0.8f),
    onErrorContainer = Color.White,
    outline = GoldAccent.copy(alpha = 0.5f),
    outlineVariant = GoldAccent.copy(alpha = 0.3f),
    inverseSurface = Ivory,
    inverseOnSurface = FeltDark,
    inversePrimary = GoldAccent,
    surfaceTint = FeltGreen,
    scrim = Color.Black.copy(alpha = 0.6f)
)

// Dark Theme (Casino Night)
private val DarkColors = darkColorScheme(
    primary = FeltLight,
    onPrimary = Color.White,
    primaryContainer = FeltGreen,
    onPrimaryContainer = Color.White,
    secondary = WoodLight,
    onSecondary = Color.White,
    secondaryContainer = WoodBrown,
    onSecondaryContainer = Color.White,
    tertiary = GoldAccent,
    onTertiary = FeltDark,
    tertiaryContainer = GoldDark,
    onTertiaryContainer = Color.White,
    background = Color(0xFF0A1F0E),
    onBackground = Color.White,
    surface = Color(0xFF1A3A1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A4A2E),
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    error = Color(0xFFEF5350),
    onError = Color.White,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color.White,
    outline = GoldAccent.copy(alpha = 0.5f),
    outlineVariant = GoldAccent.copy(alpha = 0.3f),
    inverseSurface = Ivory,
    inverseOnSurface = FeltDark,
    inversePrimary = GoldAccent,
    surfaceTint = FeltLight,
    scrim = Color.Black.copy(alpha = 0.7f)
)

// ── Typography ──────────────────────────────────────
private val CasinoTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun DominoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CasinoTypography,
        content = content
    )
}
