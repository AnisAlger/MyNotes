package com.example.mynotes.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.mynotes.model.NoteConstants

// Palette Principale - Indigo Moderne
val IndigoPrimary = Color(0xFF4F46E5)
val IndigoPrimaryVariant = Color(0xFF4338CA)
val IndigoSecondary = Color(0xFF06B6D4)
val IndigoTertiary = Color(0xFF8B5CF6)

// Thème Clair
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFCBD5E1)

// Thème Sombre
val DarkBackground = Color(0xFF0B0F17)
val DarkSurface = Color(0xFF161F2E)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Couleurs d'état
val SuccessGreen = Color(0xFF10B981)
val WarningOrange = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)

// Helper pour obtenir la couleur de fond d'une carte de note
fun getNoteCardBackgroundColor(colorIndex: Int, isDark: Boolean): Color {
    val index = colorIndex.coerceIn(0, NoteConstants.COLOR_OPTIONS.size - 1)
    return if (isDark) {
        NoteConstants.COLOR_OPTIONS_DARK[index]
    } else {
        NoteConstants.COLOR_OPTIONS[index]
    }
}