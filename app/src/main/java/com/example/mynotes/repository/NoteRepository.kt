package com.example.mynotes.repository

import android.util.Log
import com.example.mynotes.model.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

class NoteRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val tag = "MyNotesRepo"

    fun getUserId(): String? = auth.currentUser?.uid

    private fun notesCollection() =
        getUserId()?.let { uid ->
            firestore
                .collection("users")
                .document(uid)
                .collection("notes")
        }

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun addNote(
        title: String,
        content: String,
        category: String = "Général",
        colorIndex: Int = 0,
        isPinned: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = notesCollection()
        if (collection == null) {
            val err = "Utilisateur non connecté"
            Log.e(tag, "addNote failed: $err")
            onError(err)
            return
        }

        val currentTime = System.currentTimeMillis()
        val note = hashMapOf(
            "title" to title,
            "content" to content,
            "category" to category,
            "colorIndex" to colorIndex,
            "isPinned" to isPinned,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "createdAtMillis" to currentTime,
            "updatedAtMillis" to currentTime
        )

        collection
            .add(note)
            .addOnSuccessListener { docRef ->
                Log.d(tag, "Note added with ID: ${docRef.id}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error adding note", e)
                onError(e.message ?: "Erreur lors de l'enregistrement de la note")
            }
    }

    fun updateNote(
        noteId: String,
        title: String,
        content: String,
        category: String,
        colorIndex: Int,
        isPinned: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = notesCollection()
        if (collection == null) {
            val err = "Utilisateur non connecté"
            Log.e(tag, "updateNote failed: $err")
            onError(err)
            return
        }

        val currentTime = System.currentTimeMillis()
        val updates = mapOf(
            "title" to title,
            "content" to content,
            "category" to category,
            "colorIndex" to colorIndex,
            "isPinned" to isPinned,
            "updatedAt" to FieldValue.serverTimestamp(),
            "updatedAtMillis" to currentTime
        )

        collection
            .document(noteId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(tag, "Note $noteId updated")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error updating note $noteId", e)
                onError(e.message ?: "Erreur lors de la modification")
            }
    }

    fun togglePin(
        noteId: String,
        isPinned: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = notesCollection()
        if (collection == null) {
            onError("Utilisateur non connecté")
            return
        }

        collection
            .document(noteId)
            .update("isPinned", !isPinned)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erreur lors du changement d'épingle")
            }
    }

    fun updateColor(
        noteId: String,
        colorIndex: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = notesCollection()
        if (collection == null) {
            onError("Utilisateur non connecté")
            return
        }

        collection
            .document(noteId)
            .update("colorIndex", colorIndex)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Erreur lors du changement de couleur")
            }
    }

    fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val collection = notesCollection()
        if (collection == null) {
            onError("Utilisateur non connecté")
            return
        }

        collection
            .document(noteId)
            .delete()
            .addOnSuccessListener {
                Log.d(tag, "Note $noteId deleted")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error deleting note $noteId", e)
                onError(e.message ?: "Erreur lors de la suppression")
            }
    }

    fun listenToNotes(
        onNotesChanged: (List<Note>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val uid = getUserId()
        if (uid == null) {
            Log.w(tag, "listenToNotes: User is not logged in yet")
            onError("Utilisateur non connecté")
            return null
        }

        val collection = firestore
            .collection("users")
            .document(uid)
            .collection("notes")

        Log.d(tag, "Starting Firestore listener on path: users/$uid/notes")

        return collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Firestore snapshot listener error: ${error.message}", error)
                    onError(error.message ?: "Erreur lors du chargement des notes")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(tag, "Firestore snapshot is null")
                    onNotesChanged(emptyList())
                    return@addSnapshotListener
                }

                Log.d(tag, "Firestore snapshot received with ${snapshot.size()} document(s)")

                val notes = snapshot.documents.mapNotNull { document ->
                    try {
                        val rawTitle = document.get("title")
                        val rawContent = document.get("content")

                        val title = when (rawTitle) {
                            is String -> rawTitle
                            null -> ""
                            else -> rawTitle.toString()
                        }

                        val content = when (rawContent) {
                            is String -> rawContent
                            null -> ""
                            else -> rawContent.toString()
                        }

                        val category = document.getString("category") ?: "Général"
                        val colorIndex = (document.get("colorIndex") as? Number)?.toInt() ?: 0
                        val isPinned = document.getBoolean("isPinned") ?: false

                        // Résolution ultra-sécurisée des dates pour compatibilité avec tous types
                        val createdMillis = parseTimestampSafely(
                            document.get("createdAt"),
                            document.get("createdAtMillis")
                        )

                        val updatedMillis = parseTimestampSafely(
                            document.get("updatedAt"),
                            document.get("updatedAtMillis")
                        ) ?: createdMillis

                        Note(
                            id = document.id,
                            title = title,
                            content = content,
                            category = category,
                            colorIndex = colorIndex,
                            isPinned = isPinned,
                            createdAt = createdMillis,
                            updatedAt = updatedMillis
                        )
                    } catch (e: Exception) {
                        Log.e(tag, "Error parsing document ${document.id}: ${e.message}", e)
                        null
                    }
                }

                // Tri : Épinglées d'abord, puis par date de mise à jour descendante
                val sortedNotes = notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenByDescending { it.updatedAt }
                )

                Log.d(tag, "Delivering ${sortedNotes.size} note(s) to ViewModel")
                onNotesChanged(sortedNotes)
            }
    }

    private fun parseTimestampSafely(primary: Any?, fallback: Any?): Long {
        return when (primary) {
            is Timestamp -> primary.toDate().time
            is Date -> primary.time
            is Number -> primary.toLong()
            is String -> primary.toLongOrNull() ?: parseFallback(fallback)
            else -> parseFallback(fallback)
        }
    }

    private fun parseFallback(fallback: Any?): Long {
        return when (fallback) {
            is Timestamp -> fallback.toDate().time
            is Date -> fallback.time
            is Number -> fallback.toLong()
            is String -> fallback.toLongOrNull() ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }
    }
}