package com.argumentor.app.ui.screens.claim

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.ui.components.VoiceInputTextField
import com.argumentor.app.ui.components.rememberCurrentLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimCreateEditScreen(
    claimId: String?,
    topicId: String?,
    onNavigateBack: () -> Unit,
    viewModel: ClaimCreateEditViewModel = hiltViewModel()
) {
    val text by viewModel.text.collectAsState()
    val stance by viewModel.stance.collectAsState()
    val strength by viewModel.strength.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val currentLocale = rememberCurrentLocale()

    // Dialog state for unsaved changes confirmation
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Track validation state - show errors after first save attempt
    var hasAttemptedSave by remember { mutableStateOf(false) }

    LaunchedEffect(claimId, topicId) {
        viewModel.loadClaim(claimId, topicId)
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
                    Text(stringResource(R.string.action_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(
                        if (claimId == null) R.string.claim_new_title
                        else R.string.claim_edit_title
                    ))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            showUnsavedChangesDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            hasAttemptedSave = true
                            if (text.isNotBlank()) {
                                viewModel.saveClaim(onNavigateBack)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(stringResource(R.string.save))
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
            // Text field
            VoiceInputTextField(
                value = text,
                onValueChange = viewModel::onTextChange,
                label = stringResource(R.string.claim_text_label),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                locale = currentLocale,
                isError = hasAttemptedSave && text.isBlank(),
                supportingText = if (hasAttemptedSave && text.isBlank()) {
                    { Text(stringResource(R.string.claim_error_text_required)) }
                } else null
            )

            // Stance selector
            Text(
                stringResource(R.string.claim_stance),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Claim.Stance.values().forEach { stanceOption ->
                    FilterChip(
                        selected = stance == stanceOption,
                        onClick = { viewModel.onStanceChange(stanceOption) },
                        label = {
                            Text(stringResource(
                                when (stanceOption) {
                                    Claim.Stance.PRO -> R.string.stance_pro
                                    Claim.Stance.CON -> R.string.stance_con
                                    Claim.Stance.NEUTRAL -> R.string.stance_neutral
                                }
                            ))
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            // Strength selector
            Text(
                stringResource(R.string.claim_strength),
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Claim.Strength.values().forEach { strengthOption ->
                    FilterChip(
                        selected = strength == strengthOption,
                        onClick = { viewModel.onStrengthChange(strengthOption) },
                        label = {
                            Text(stringResource(
                                when (strengthOption) {
                                    Claim.Strength.LOW -> R.string.strength_low
                                    Claim.Strength.MEDIUM -> R.string.strength_medium
                                    Claim.Strength.HIGH -> R.string.strength_high
                                }
                            ))
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}
