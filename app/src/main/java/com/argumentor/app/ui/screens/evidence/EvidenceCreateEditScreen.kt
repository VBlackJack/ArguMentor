package com.argumentor.app.ui.screens.evidence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.util.createSpeechIntent
import com.argumentor.app.util.rememberSpeechToTextLauncher
import com.argumentor.app.ui.components.rememberCurrentLocale

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
    val currentLocale = rememberCurrentLocale()
    val context = LocalContext.current

    val isEditMode = evidenceId != null
    // UI state preservation on configuration changes (e.g., screen rotation)
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showSourceSelector by rememberSaveable { mutableStateOf(false) }

    // Speech-to-text launcher for content field
    val speechLauncher = rememberSpeechToTextLauncher { text ->
        if (text.isNotBlank()) {
            val currentContent = content
            val newContent = if (currentContent.isNotBlank()) {
                "$currentContent $text"
            } else {
                text
            }
            viewModel.onContentChange(newContent)
        }
    }

    LaunchedEffect(evidenceId, claimId) {
        viewModel.loadEvidence(evidenceId, claimId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditMode) R.string.evidence_edit_title else R.string.evidence_new_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.accessibility_delete)
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
                            contentDescription = stringResource(R.string.accessibility_save)
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
                    .imePadding()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Content field with voice input button
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { viewModel.onContentChange(it) },
                        label = { Text(stringResource(R.string.evidence_content_label)) },
                        placeholder = { Text(stringResource(R.string.evidence_content_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 4,
                        maxLines = 10,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        trailingIcon = {
                            IconButton(onClick = { speechLauncher.launch(createSpeechIntent(context, currentLocale)) }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(R.string.content_desc_voice_input),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }

                // Type selector
                Text(
                    text = stringResource(R.string.evidence_type),
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
                                    stringResource(
                                        when (evidenceType) {
                                            Evidence.EvidenceType.STUDY -> R.string.evidence_type_study
                                            Evidence.EvidenceType.STAT -> R.string.evidence_type_stat
                                            Evidence.EvidenceType.QUOTE -> R.string.evidence_type_quote
                                            Evidence.EvidenceType.EXAMPLE -> R.string.evidence_type_example
                                        }
                                    )
                                )
                            }
                        )
                    }
                }

                // Quality selector
                Text(
                    text = stringResource(R.string.evidence_quality_label),
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
                                    stringResource(
                                        when (evidenceQuality) {
                                            Evidence.Quality.LOW -> R.string.evidence_quality_low
                                            Evidence.Quality.MEDIUM -> R.string.evidence_quality_medium
                                            Evidence.Quality.HIGH -> R.string.evidence_quality_high
                                        }
                                    )
                                )
                            }
                        )
                    }
                }

                // Source selector
                Divider()

                Text(
                    text = stringResource(R.string.evidence_source_label),
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
                                selectedSource?.let { source ->
                                    Text(
                                        text = source.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    source.citation?.takeIf { it.isNotEmpty() }?.let { citation ->
                                        Text(
                                            text = citation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.onSourceSelected(null) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.evidence_source_remove)
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
                                stringResource(R.string.evidence_source_choose),
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
                                stringResource(R.string.evidence_source_new),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Help text
                Text(
                    text = stringResource(R.string.evidence_required_fields),
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
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    // Source selector dialog
    if (showSourceSelector) {
        AlertDialog(
            onDismissRequest = { showSourceSelector = false },
            title = { Text(stringResource(R.string.evidence_select_source_title)) },
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
                                source.citation?.takeIf { it.isNotEmpty() }?.let { citation ->
                                    Text(
                                        text = citation,
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.evidence_delete_dialog_title)) },
            text = { Text(stringResource(R.string.evidence_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvidence {
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
