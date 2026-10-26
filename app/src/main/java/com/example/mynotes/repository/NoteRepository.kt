package com.example.mynotes.repository

import com.example.mynotes.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class NoteRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

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
            onError("Utilisateur non connecté")
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
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erreur lors de l'enregistrement de la note")
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
            onError("Utilisateur non connecté")
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
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erreur lors de la modification")
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
            .addOnFailureListener {
                onError(it.message ?: "Erreur lors du changement d'épingle")
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
            .addOnFailureListener {
                onError(it.message ?: "Erreur lors du changement de couleur")
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
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Erreur lors de la suppression")
            }
    }

    fun listenToNotes(
        onNotesChanged: (List<Note>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val collection = notesCollection()
        if (collection == null) {
            onError("Utilisateur non connecté")
            return null
        }

        return collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Erreur lors du chargement des notes")
                    return@addSnapshotListener
                }

                val notes = snapshot?.documents?.mapNotNull { document ->
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val category = document.getString("category") ?: "Général"
                    val colorIndex = document.getLong("colorIndex")?.toInt() ?: 0
                    val isPinned = document.getBoolean("isPinned") ?: false

                    val createdMillis = document.getTimestamp("createdAt")?.toDate()?.time
                        ?: (document.getLong("createdAtMillis") ?: System.currentTimeMillis())

                    val updatedMillis = document.getTimestamp("updatedAt")?.toDate()?.time
                        ?: (document.getLong("updatedAtMillis") ?: createdMillis)

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
                } ?: emptyList()

                // Tri : d'abord épinglées, puis par date de modification descendante
                val sortedNotes = notes.sortedWith(
                    compareByDescending<Note> { it.isPinned }
                        .thenByDescending { it.updatedAt }
                )

                onNotesChanged(sortedNotes)
            }
    }
}