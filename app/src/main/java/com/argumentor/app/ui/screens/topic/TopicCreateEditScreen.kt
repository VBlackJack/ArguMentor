package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.data.model.Topic
import com.argumentor.app.ui.components.VoiceInputTextField
import com.argumentor.app.ui.components.rememberCurrentLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicCreateEditScreen(
    topicId: String?,
    onNavigateBack: () -> Unit,
    viewModel: TopicCreateEditViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val posture by viewModel.posture.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var newTagText by remember { mutableStateOf("") }
    val currentLocale = rememberCurrentLocale()

    // Dialog state for unsaved changes confirmation
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(topicId) {
        viewModel.loadTopic(topicId)
    }

    // Handle back button press
    BackHandler(enabled = viewModel.hasUnsavedChanges()) {
        showUnsavedChangesDialog = true
    }

    // Unsaved changes confirmation dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Modifications non sauvegardées") },
            text = { Text("Vous avez des modifications non sauvegardées. Voulez-vous vraiment quitter sans enregistrer ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (topicId == null) "Nouveau sujet" else "Modifier le sujet") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showUnsavedChangesDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveTopic(onNavigateBack) },
                        enabled = !isSaving && title.isNotBlank()
                    ) {
                        Text("Enregistrer")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title field
            VoiceInputTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = "Titre",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                locale = currentLocale
            )

            // Summary field
            VoiceInputTextField(
                value = summary,
                onValueChange = viewModel::onSummaryChange,
                label = "Résumé",
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                locale = currentLocale
            )

            // Posture selector
            Text("Posture", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Topic.Posture.values().forEach { postureOption ->
                    FilterChip(
                        selected = posture == postureOption,
                        onClick = { viewModel.onPostureChange(postureOption) },
                        label = {
                            Text(
                                when (postureOption) {
                                    Topic.Posture.NEUTRAL_CRITIQUE -> "Neutre & Critique"
                                    Topic.Posture.SCEPTIQUE -> "Sceptique"
                                    Topic.Posture.COMPARATIF_ACADEMIQUE -> "Comparatif Académique"
                                }
                            )
                        }
                    )
                }
            }

            // Tags
            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text("Nouveau tag") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        viewModel.addTag(newTagText)
                        newTagText = ""
                    },
                    enabled = newTagText.isNotBlank()
                ) {
                    Text("Ajouter")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.removeTag(tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Supprimer",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}
