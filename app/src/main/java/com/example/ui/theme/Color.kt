package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Professional Slate & Obsidian Dark Palette
val SlateBackground = Color(0xFF0B0F19)
val SlateSurface = Color(0xFF131A2A)
val SlateSurfaceVariant = Color(0xFF1B2438)
val SlateSurfaceElevated = Color(0xFF232E48)
val SlateBorderSubtle = Color(0x1FFFFFFF)
val SlateBorderCrisp = Color(0x33FFFFFF)

// Refined Primary & Secondary Accents (Professional Tech Palette)
val AccentIndigo = Color(0xFF6366F1)       // Indigo 500
val AccentIndigoLight = Color(0xFF818CF8)  // Indigo 400
val AccentSky = Color(0xFF0EA5E9)          // Sky 500
val AccentCyan = Color(0xFF06B6D4)         // Cyan 500
val AccentTeal = Color(0xFF14B8A6)         // Teal 500

// Status & Semantic Tokens
val SemanticSuccess = Color(0xFF10B981)    // Emerald 500
val SemanticWarning = Color(0xFFF59E0B)    // Amber 500
val SemanticError = Color(0xFFEF4444)      // Rose 500

// Text Colors (High Contrast & Clean Hierarchy)
val TextPrimary = Color(0xFFF8FAFC)        // Slate 50
val TextSecondary = Color(0xFF94A3B8)      // Slate 400
val TextTertiary = Color(0xFF64748B)       // Slate 500
val TextDisabled = Color(0xFF475569)       // Slate 600

// Legacy Aliases for backwards compatibility with existing references
val IndigoPrimary = AccentIndigo
val IndigoSecondary = AccentIndigoLight
val CyanAccent = AccentCyan
val DarkBackground = SlateBackground
val DarkSurface = SlateSurface
val DarkSurfaceVariant = SlateSurfaceVariant
val LightText = TextPrimary
val LightTextSecondary = TextSecondary
val SuccessGreen = SemanticSuccess
val ErrorRed = SemanticError
