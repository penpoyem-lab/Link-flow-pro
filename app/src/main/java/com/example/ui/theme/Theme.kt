package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = AccentIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF282868),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = AccentSky,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = AccentCyan,
    onTertiary = Color.Black,
    background = SlateBackground,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SlateBorderCrisp,
    outlineVariant = SlateBorderSubtle,
    error = SemanticError,
    onError = Color.White
)

@Composable
fun LinkFlowTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProfessionalDarkColorScheme,
        content = content
    )
}
