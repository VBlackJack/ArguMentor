package com.argumentor.app.ui.screens.claim

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

    LaunchedEffect(claimId, topicId) {
        viewModel.loadClaim(claimId, topicId)
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.accessibility_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveClaim(onNavigateBack) },
                        enabled = !isSaving && text.isNotBlank()
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
                locale = currentLocale
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
                        }
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
