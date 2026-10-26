package com.example.mynotes.uii

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mynotes.model.Note
import com.example.mynotes.model.NoteConstants
import com.example.mynotes.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onLogout: () -> Unit,
    viewModel: NotesViewModel = viewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val rawNotes by viewModel.notes.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Écoute des messages du ViewModel pour les Snackbars
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MyNotes",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${rawNotes.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = viewModel.getCurrentUserEmail(),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Basculer Vue Grille / Liste
                    IconButton(onClick = { viewModel.toggleGridView() }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                            contentDescription = if (isGridView) "Passer en vue liste" else "Passer en vue grille",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Déconnexion
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Déconnexion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une note"
                    )
                },
                text = {
                    Text(
                        text = "Nouvelle note",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // =========================
            // BARRE DE RECHERCHE
            // =========================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Rechercher dans vos notes...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Recherche",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Effacer la recherche"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // =========================
            // FILTRES PAR CATÉGORIES
            // =========================
            val filterOptions = remember {
                listOf("Tous", "Épinglés") + NoteConstants.CATEGORIES
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { option ->
                    val isSelected = option == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(option) },
                        label = {
                            Text(
                                text = option,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (option == "Épinglés") {
                            {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // =========================
            // CORPS : CHARGEMENT / VIDE / NOTES
            // =========================
            if (isLoading && rawNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            } else if (filteredNotes.isEmpty()) {
                // État vide élégant
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotBlank() || selectedCategory != "Tous") Icons.Outlined.SearchOff else Icons.AutoMirrored.Outlined.NoteAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "Tous") {
                                "Aucun résultat trouvé"
                            } else {
                                "Votre carnet de notes est vide"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "Tous") {
                                "Essayez de modifier votre mot-clé ou filtre de catégorie"
                            } else {
                                "Commencez dès maintenant en créant votre première note !"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (searchQuery.isBlank() && selectedCategory == "Tous") {
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Créer une note", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setSelectedCategory("Tous")
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Réinitialiser les filtres")
                            }
                        }
                    }
                }
            } else {
                // Partition des notes : Épinglées vs Autres
                val pinnedNotes = filteredNotes.filter { it.isPinned }
                val unpinnedNotes = filteredNotes.filter { !it.isPinned }

                if (isGridView) {
                    // Vue Grille à 2 colonnes
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredNotes,
                            key = { it.id }
                        ) { note ->
                            NoteCard(
                                note = note,
                                onDelete = { noteToDelete = note },
                                onEdit = { editingNote = note },
                                onTogglePin = { viewModel.togglePin(note) },
                                onCopy = {
                                    val copyText = "${note.title}\n\n${note.content}".trim()
                                    clipboardManager.setText(AnnotatedString(copyText))
                                    viewModel.clearMessage()
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                            )
                        }
                    }
                } else {
                    // Vue Liste à 1 colonne avec séparateurs élégants
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Section des notes épinglées
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ÉPINGLÉES (${pinnedNotes.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            items(
                                items = pinnedNotes,
                                key = { it.id }
                            ) { note ->
                                NoteCard(
                                    note = note,
                                    onDelete = { noteToDelete = note },
                                    onEdit = { editingNote = note },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onCopy = {
                                        val copyText = "${note.title}\n\n${note.content}".trim()
                                        clipboardManager.setText(AnnotatedString(copyText))
                                    }
                                )
                            }
                        }

                        // Section des autres notes
                        if (unpinnedNotes.isNotEmpty()) {
                            if (pinnedNotes.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AUTRES (${unpinnedNotes.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                    )
                                }
                            }

                            items(
                                items = unpinnedNotes,
                                key = { it.id }
                            ) { note ->
                                NoteCard(
                                    note = note,
                                    onDelete = { noteToDelete = note },
                                    onEdit = { editingNote = note },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onCopy = {
                                        val copyText = "${note.title}\n\n${note.content}".trim()
                                        clipboardManager.setText(AnnotatedString(copyText))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================
    // DIALOGUE CRÉATION NOTE
    // =========================
    if (showCreateDialog) {
        EditNoteDialog(
            note = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newTitle, newContent, newCategory, newColorIndex, newIsPinned ->
                viewModel.addNote(
                    title = newTitle,
                    content = newContent,
                    category = newCategory,
                    colorIndex = newColorIndex,
                    isPinned = newIsPinned
                ) {
                    showCreateDialog = false
                }
            }
        )
    }

    // =========================
    // DIALOGUE MODIFICATION NOTE
    // =========================
    editingNote?.let { note ->
        EditNoteDialog(
            note = note,
            onDismiss = { editingNote = null },
            onSave = { updatedTitle, updatedContent, updatedCategory, updatedColorIndex, updatedIsPinned ->
                viewModel.updateNote(
                    noteId = note.id,
                    title = updatedTitle,
                    content = updatedContent,
                    category = updatedCategory,
                    colorIndex = updatedColorIndex,
                    isPinned = updatedIsPinned
                ) {
                    editingNote = null
                }
            }
        )
    }

    // =========================
    // DIALOGUE CONFIRMATION SUPPRESSION
    // =========================
    noteToDelete?.let { note ->
        ConfirmationDialog(
            title = "Supprimer la note ?",
            message = "Voulez-vous vraiment supprimer définitivement la note « ${if (note.title.isNotBlank()) note.title else "sans titre"} » ? Cette action est irréversible.",
            confirmText = "Supprimer",
            cancelText = "Annuler",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteNote(note.id)
                noteToDelete = null
            },
            onDismiss = {
                noteToDelete = null
            }
        )
    }

    // =========================
    // DIALOGUE CONFIRMATION DÉCONNEXION
    // =========================
    if (showLogoutConfirm) {
        ConfirmationDialog(
            title = "Déconnexion",
            message = "Êtes-vous sûr de vouloir vous déconnecter de MyNotes ?",
            confirmText = "Se déconnecter",
            cancelText = "Annuler",
            isDestructive = false,
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = {
                showLogoutConfirm = false
            }
        )
    }
}