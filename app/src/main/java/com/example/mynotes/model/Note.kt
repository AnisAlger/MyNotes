package com.example.mynotes.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "Général",
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val timeToFormat = if (updatedAt > 0) updatedAt else createdAt
        val date = Date(if (timeToFormat > 0) timeToFormat else System.currentTimeMillis())
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        return format.format(date)
    }

    val displayTitle: String
        get() = if (title.isNotBlank()) title else if (content.isNotBlank()) content.take(30) else "Note sans titre"

    val wordCount: Int
        get() {
            val text = "$title $content".trim()
            if (text.isEmpty()) return 0
            return text.split("\\s+".toRegex()).size
        }

    val charCount: Int
        get() = content.length
}

object NoteConstants {
    val CATEGORIES = listOf(
        "Général",
        "Personnel",
        "Travail",
        "Idées",
        "Études",
        "Urgent"
    )

    // Palette claire
    val COLOR_OPTIONS = listOf(
        Color(0xFFFFFFFF), // Blanc / Défaut
        Color(0xFFFFE5D9), // Pêche douce
        Color(0xFFFFF3CD), // Ambre / Miel
        Color(0xFFD8F3DC), // Menthe fraîche
        Color(0xFFD0F4DE), // Vert pastel
        Color(0xFFE0E1DD), // Sauge ardoise
        Color(0xFFDCEAFE), // Ciel azur
        Color(0xFFE8DDFF), // Lavande douce
        Color(0xFFFFD6E0)  // Rose quartz
    )

    // Palette sombre
    val COLOR_OPTIONS_DARK = listOf(
        Color(0xFF1E293B), // Neutre sombre
        Color(0xFF3E2723), // Pêche sombre
        Color(0xFF3E3614), // Ambre sombre
        Color(0xFF1B382B), // Menthe sombre
        Color(0xFF1A382A), // Vert sombre
        Color(0xFF252A30), // Sauge ardoise sombre
        Color(0xFF1A2D42), // Ciel sombre
        Color(0xFF2E2442), // Lavande sombre
        Color(0xFF42212B)  // Rose sombre
    )
}