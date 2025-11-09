package com.argumentor.app.ui.screens.evidence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.data.model.Evidence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceCreateEditScreen(
    evidenceId: String?,
    claimId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreateSource: () -> Unit,
    viewModel: EvidenceCreateEditViewModel = hiltViewModel()
) {
    val content by viewModel.content.collectAsState()
    val type by viewModel.type.collectAsState()
    val quality by viewModel.quality.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val availableSources by viewModel.availableSources.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isEditMode = evidenceId != null
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSourceSelector by remember { mutableStateOf(false) }

    LaunchedEffect(evidenceId, claimId) {
        viewModel.loadEvidence(evidenceId, claimId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Éditer la preuve" else "Nouvelle preuve"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.saveEvidence {
                                onNavigateBack()
                            }
                        },
                        enabled = content.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Enregistrer"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Content field
                OutlinedTextField(
                    value = content,
                    onValueChange = { viewModel.onContentChange(it) },
                    label = { Text("Contenu de la preuve *") },
                    placeholder = { Text("Décrivez la preuve...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 10
                )

                // Type selector
                Text(
                    text = "Type de preuve",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Evidence.EvidenceType.values().forEach { evidenceType ->
                        FilterChip(
                            selected = type == evidenceType,
                            onClick = { viewModel.onTypeChange(evidenceType) },
                            label = {
                                Text(
                                    when (evidenceType) {
                                        Evidence.EvidenceType.STUDY -> "Étude"
                                        Evidence.EvidenceType.STAT -> "Statistique"
                                        Evidence.EvidenceType.QUOTE -> "Citation"
                                        Evidence.EvidenceType.EXAMPLE -> "Exemple"
                                    }
                                )
                            }
                        )
                    }
                }

                // Quality selector
                Text(
                    text = "Qualité de la preuve",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Evidence.Quality.values().forEach { evidenceQuality ->
                        FilterChip(
                            selected = quality == evidenceQuality,
                            onClick = { viewModel.onQualityChange(evidenceQuality) },
                            label = {
                                Text(
                                    when (evidenceQuality) {
                                        Evidence.Quality.LOW -> "Faible"
                                        Evidence.Quality.MEDIUM -> "Moyenne"
                                        Evidence.Quality.HIGH -> "Élevée"
                                    }
                                )
                            }
                        )
                    }
                }

                // Source selector
                Divider()

                Text(
                    text = "Source (optionnelle)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (selectedSource != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedSource!!.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!selectedSource!!.citation.isNullOrEmpty()) {
                                    Text(
                                        text = selectedSource!!.citation!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.onSourceSelected(null) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Retirer la source"
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSourceSelector = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Choisir",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        OutlinedButton(
                            onClick = onNavigateToCreateSource,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Nouvelle",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Help text
                Text(
                    text = "* Champs obligatoires",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        viewModel.saveEvidence {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer")
                }
            }
        }
    }

    // Source selector dialog
    if (showSourceSelector) {
        AlertDialog(
            onDismissRequest = { showSourceSelector = false },
            title = { Text("Sélectionner une source") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSources.forEach { source ->
                        OutlinedCard(
                            onClick = {
                                viewModel.onSourceSelected(source)
                                showSourceSelector = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = source.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!source.citation.isNullOrEmpty()) {
                                    Text(
                                        text = source.citation!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceSelector = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la preuve ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvidence {
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
