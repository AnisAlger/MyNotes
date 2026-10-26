package com.example.mynotes.model

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
        val date = Date(if (updatedAt > 0) updatedAt else createdAt)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        return format.format(date)
    }

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

    // Palette de couleurs pour les cartes de notes
    val COLOR_OPTIONS = listOf(
        0xFFF8F9FA, // Neutre / Défaut
        0xFFFFE5D9, // Pêche douce
        0xFFFFF3CD, // Ambre / Miel
        0xFFD8F3DC, // Menthe fraîche
        0xFFD0F4DE, // Vert pastel
        0xFFE0E1DD, // Sauge ardoise
        0xFFDCEAFE, // Ciel azur
        0xFFE8DDFF, // Lavande douce
        0xFFFFD6E0  // Rose quartz
    )

    // Palette sombre correspondante pour le mode sombre
    val COLOR_OPTIONS_DARK = listOf(
        0xFF242529, // Neutre sombre
        0xFF3E2723, // Pêche sombre
        0xFF3E3614, // Ambre sombre
        0xFF1B382B, // Menthe sombre
        0xFF1A382A, // Vert sombre
        0xFF252A30, // Sauge ardoise sombre
        0xFF1A2D42, // Ciel sombre
        0xFF2E2442, // Lavande sombre
        0xFF42212B  // Rose sombre
    )
}