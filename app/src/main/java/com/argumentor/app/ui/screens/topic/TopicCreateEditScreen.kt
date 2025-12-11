package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
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

    // UI state preservation on configuration changes
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    var hasAttemptedSave by rememberSaveable { mutableStateOf(false) }

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
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.action_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (topicId == null) R.string.topic_create_title else R.string.topic_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showUnsavedChangesDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.accessibility_back))
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    TextButton(
                        onClick = {
                            hasAttemptedSave = true
                            if (title.isNotBlank()) {
                                viewModel.saveTopic(onNavigateBack)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(stringResource(R.string.accessibility_save))
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
            // Title field (max 200 characters)
            VoiceInputTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = stringResource(R.string.topic_field_title),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLength = 200,
                locale = currentLocale,
                isError = hasAttemptedSave && title.isBlank(),
                supportingText = if (hasAttemptedSave && title.isBlank()) {
                    { Text(stringResource(R.string.topic_field_title_required)) }
                } else null
            )

            // Summary field (max 2000 characters)
            VoiceInputTextField(
                value = summary,
                onValueChange = viewModel::onSummaryChange,
                label = stringResource(R.string.topic_field_summary),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                maxLength = 2000,
                locale = currentLocale
            )

            // Posture selector
            Text(stringResource(R.string.topic_posture_section), style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Topic.Posture.values().forEach { postureOption ->
                    FilterChip(
                        selected = posture == postureOption,
                        onClick = { viewModel.onPostureChange(postureOption) },
                        label = {
                            Text(
                                stringResource(when (postureOption) {
                                    Topic.Posture.NEUTRAL_CRITICAL -> R.string.topic_posture_neutral
                                    Topic.Posture.SKEPTICAL -> R.string.topic_posture_skeptical
                                    Topic.Posture.ACADEMIC_COMPARATIVE -> R.string.topic_posture_academic
                                })
                            )
                        }
                    )
                }
            }

            // Tags
            Text(stringResource(R.string.topic_tags_section), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text(stringResource(R.string.topic_new_tag)) },
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
                    Text(stringResource(R.string.action_add))
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
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.accessibility_delete),
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
