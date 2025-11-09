package com.argumentor.app.ui.screens.claim

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
import com.argumentor.app.data.model.Claim
import com.argumentor.app.ui.components.VoiceInputTextField

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

    LaunchedEffect(claimId, topicId) {
        viewModel.loadClaim(claimId, topicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (claimId == null) "Nouvelle affirmation" else "Modifier l'affirmation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveClaim(onNavigateBack) },
                        enabled = !isSaving && text.isNotBlank()
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
            // Text field
            VoiceInputTextField(
                value = text,
                onValueChange = viewModel::onTextChange,
                label = "Texte de l'affirmation",
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8
            )

            // Stance selector
            Text("Position", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Claim.Stance.values().forEach { stanceOption ->
                    FilterChip(
                        selected = stance == stanceOption,
                        onClick = { viewModel.onStanceChange(stanceOption) },
                        label = {
                            Text(
                                when (stanceOption) {
                                    Claim.Stance.PRO -> "Pour"
                                    Claim.Stance.CON -> "Contre"
                                    Claim.Stance.NEUTRAL -> "Neutre"
                                }
                            )
                        }
                    )
                }
            }

            // Strength selector
            Text("Force", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Claim.Strength.values().forEach { strengthOption ->
                    FilterChip(
                        selected = strength == strengthOption,
                        onClick = { viewModel.onStrengthChange(strengthOption) },
                        label = {
                            Text(
                                when (strengthOption) {
                                    Claim.Strength.LOW -> "Faible"
                                    Claim.Strength.MEDIUM -> "Moyen"
                                    Claim.Strength.HIGH -> "Fort"
                                }
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
