package com.example.mynotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.model.Note
import com.example.mynotes.repository.NoteRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel : ViewModel() {

    private val repository = NoteRepository()

    // Notes brutes venant de Firestore
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Recherche et Filtres
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tous")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Affichage : Grille vs Liste
    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    // Indicateur de chargement
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Messages / Feedback utilisateur
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // Notes filtrées réactives
    val filteredNotes: StateFlow<List<Note>> = combine(
        _notes,
        _searchQuery,
        _selectedCategory
    ) { allNotes, query, category ->
        allNotes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)

            val matchesCategory = category == "Tous" ||
                    (category == "Épinglés" && note.isPinned) ||
                    note.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var listener: ListenerRegistration? = null

    init {
        startListening()
    }

    fun getCurrentUserEmail(): String {
        return repository.getCurrentUserEmail() ?: "Utilisateur"
    }

    fun startListening() {
        listener?.remove()
        _isLoading.value = true

        listener = repository.listenToNotes(
            onNotesChanged = { newNotes ->
                _notes.value = newNotes
                _isLoading.value = false
            },
            onError = { error ->
                _message.value = error
                _isLoading.value = false
            }
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun addNote(
        title: String,
        content: String,
        category: String = "Général",
        colorIndex: Int = 0,
        isPinned: Boolean = false,
        onDone: (() -> Unit)? = null
    ) {
        if (title.isBlank() && content.isBlank()) {
            _message.value = "Veuillez saisir un titre ou un contenu"
            return
        }

        val resolvedTitle = if (title.isBlank()) "Note sans titre" else title.trim()

        repository.addNote(
            title = resolvedTitle,
            content = content.trim(),
            category = category,
            colorIndex = colorIndex,
            isPinned = isPinned,
            onSuccess = {
                _message.value = "Note ajoutée avec succès"
                onDone?.invoke()
            },
            onError = { error ->
                _message.value = error
            }
        )
    }

    fun updateNote(
        noteId: String,
        title: String,
        content: String,
        category: String = "Général",
        colorIndex: Int = 0,
        isPinned: Boolean = false,
        onDone: (() -> Unit)? = null
    ) {
        if (title.isBlank() && content.isBlank()) {
            _message.value = "Veuillez saisir un titre ou un contenu"
            return
        }

        val resolvedTitle = if (title.isBlank()) "Note sans titre" else title.trim()

        repository.updateNote(
            noteId = noteId,
            title = resolvedTitle,
            content = content.trim(),
            category = category,
            colorIndex = colorIndex,
            isPinned = isPinned,
            onSuccess = {
                _message.value = "Note mise à jour"
                onDone?.invoke()
            },
            onError = { error ->
                _message.value = error
            }
        )
    }

    fun togglePin(note: Note) {
        repository.togglePin(
            noteId = note.id,
            isPinned = note.isPinned,
            onSuccess = {
                _message.value = if (!note.isPinned) "Note épinglée" else "Note désépinglée"
            },
            onError = { error ->
                _message.value = error
            }
        )
    }

    fun changeColor(noteId: String, colorIndex: Int) {
        repository.updateColor(
            noteId = noteId,
            colorIndex = colorIndex,
            onSuccess = {
                _message.value = "Couleur modifiée"
            },
            onError = { error ->
                _message.value = error
            }
        )
    }

    fun deleteNote(noteId: String) {
        repository.deleteNote(
            noteId = noteId,
            onSuccess = {
                _message.value = "Note supprimée"
            },
            onError = { error ->
                _message.value = error
            }
        )
    }

    fun clearMessage() {
        _message.value = ""
    }

    override fun onCleared() {
        listener?.remove()
        listener = null
        super.onCleared()
    }
}