package com.argumentor.app.ui.screens.source

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.argumentor.app.util.createSpeechIntent
import com.argumentor.app.util.rememberSpeechToTextLauncher
import com.argumentor.app.ui.components.rememberCurrentLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceCreateEditScreen(
    sourceId: String?,
    onNavigateBack: () -> Unit,
    viewModel: SourceCreateEditViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val citation by viewModel.citation.collectAsState()
    val url by viewModel.url.collectAsState()
    val publisher by viewModel.publisher.collectAsState()
    val date by viewModel.date.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val linkedEvidences by viewModel.linkedEvidences.collectAsState()
    val linkedClaims by viewModel.linkedClaims.collectAsState()
    val currentLocale = rememberCurrentLocale()
    val context = LocalContext.current

    val isEditMode = sourceId != null

    // Speech-to-text launchers for text fields
    val titleSpeechLauncher = rememberSpeechToTextLauncher { text ->
        if (text.isNotBlank()) {
            val currentTitle = title
            val newTitle = if (currentTitle.isNotBlank()) {
                "$currentTitle $text"
            } else {
                text
            }
            viewModel.onTitleChange(newTitle)
        }
    }

    val citationSpeechLauncher = rememberSpeechToTextLauncher { text ->
        if (text.isNotBlank()) {
            val currentCitation = citation
            val newCitation = if (currentCitation.isNotBlank()) {
                "$currentCitation $text"
            } else {
                text
            }
            viewModel.onCitationChange(newCitation)
        }
    }

    val publisherSpeechLauncher = rememberSpeechToTextLauncher { text ->
        if (text.isNotBlank()) {
            val currentPublisher = publisher
            val newPublisher = if (currentPublisher.isNotBlank()) {
                "$currentPublisher $text"
            } else {
                text
            }
            viewModel.onPublisherChange(newPublisher)
        }
    }

    val notesSpeechLauncher = rememberSpeechToTextLauncher { text ->
        if (text.isNotBlank()) {
            val currentNotes = notes
            val newNotes = if (currentNotes.isNotBlank()) {
                "$currentNotes $text"
            } else {
                text
            }
            viewModel.onNotesChange(newNotes)
        }
    }

    LaunchedEffect(sourceId) {
        viewModel.loadSource(sourceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (isEditMode) R.string.source_edit_title else R.string.source_create_title)
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
                    IconButton(
                        onClick = {
                            viewModel.saveSource {
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank()
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
                // Title field (required) with voice input
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text(stringResource(R.string.source_field_title)) },
                    placeholder = { Text(stringResource(R.string.source_field_title_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { titleSpeechLauncher.launch(createSpeechIntent(context, currentLocale)) }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.content_desc_voice_input),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // Citation field with voice input
                OutlinedTextField(
                    value = citation,
                    onValueChange = { viewModel.onCitationChange(it) },
                    label = { Text(stringResource(R.string.source_field_citation)) },
                    placeholder = { Text(stringResource(R.string.source_field_citation_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { citationSpeechLauncher.launch(createSpeechIntent(context, currentLocale)) }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.content_desc_voice_input),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // URL field
                OutlinedTextField(
                    value = url,
                    onValueChange = { viewModel.onUrlChange(it) },
                    label = { Text(stringResource(R.string.source_field_url)) },
                    placeholder = { Text(stringResource(R.string.source_field_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )

                // Publisher field with voice input
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { viewModel.onPublisherChange(it) },
                    label = { Text(stringResource(R.string.source_field_publisher)) },
                    placeholder = { Text(stringResource(R.string.source_field_publisher_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { publisherSpeechLauncher.launch(createSpeechIntent(context, currentLocale)) }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.content_desc_voice_input),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // Date field
                OutlinedTextField(
                    value = date,
                    onValueChange = { viewModel.onDateChange(it) },
                    label = { Text(stringResource(R.string.source_field_date)) },
                    placeholder = { Text(stringResource(R.string.source_field_date_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                // Notes field with voice input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text(stringResource(R.string.source_field_notes)) },
                    placeholder = { Text(stringResource(R.string.source_field_notes_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    trailingIcon = {
                        IconButton(onClick = { notesSpeechLauncher.launch(createSpeechIntent(context, currentLocale)) }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.content_desc_voice_input),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // Help text
                Text(
                    text = stringResource(R.string.source_required_fields),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Linked claims section (only in edit mode)
                if (isEditMode && linkedClaims.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.source_linked_claims),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    linkedClaims.forEach { claim ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = claim.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Show how many evidences from this source link to this claim
                                val evidenceCount = linkedEvidences.count { it.claimId == claim.id }
                                Text(
                                    text = stringResource(R.string.source_evidence_count, evidenceCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button (alternative to action button)
                Button(
                    onClick = {
                        viewModel.saveSource {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.accessibility_save))
                }
            }
        }
    }
}
