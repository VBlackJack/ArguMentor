package com.argumentor.app.ui.screens.question

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
import com.argumentor.app.data.model.Question
import com.argumentor.app.ui.components.VoiceInputTextField
import com.argumentor.app.ui.components.rememberCurrentLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCreateEditScreen(
    questionId: String?,
    targetId: String?,
    onNavigateBack: () -> Unit,
    viewModel: QuestionCreateEditViewModel = hiltViewModel()
) {
    val text by viewModel.text.collectAsState()
    val kind by viewModel.kind.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val availableClaims by viewModel.availableClaims.collectAsState()
    val selectedClaim by viewModel.selectedClaim.collectAsState()
    val isTopicLevel by viewModel.isTopicLevel.collectAsState()
    val currentLocale = rememberCurrentLocale()

    LaunchedEffect(questionId, targetId) {
        viewModel.loadQuestion(questionId, targetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (questionId == null) "Nouvelle question" else "Modifier la question") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveQuestion(onNavigateBack) },
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
                label = "Texte de la question",
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                locale = currentLocale
            )

            // Target selector (topic vs claim)
            if (availableClaims.isNotEmpty()) {
                Text("Cible de la question", style = MaterialTheme.typography.titleMedium)

                // Toggle between topic level and claim level
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = isTopicLevel,
                        onClick = { viewModel.onToggleLevel() },
                        label = { Text("Sujet global") }
                    )
                    FilterChip(
                        selected = !isTopicLevel,
                        onClick = {
                            if (isTopicLevel) {
                                viewModel.onToggleLevel()
                            }
                        },
                        label = { Text("Affirmation spécifique") }
                    )
                }

                // Claim selector (shown when not topic level)
                if (!isTopicLevel) {
                    Text("Sélectionner une affirmation", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableClaims.forEach { claim ->
                            FilterChip(
                                selected = selectedClaim?.id == claim.id,
                                onClick = { viewModel.onClaimSelected(claim) },
                                label = {
                                    Text(
                                        text = claim.text,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else if (selectedClaim != null) {
                    // Show currently linked claim when in topic mode
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Affirmation liée:",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                selectedClaim!!.text,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Kind selector
            Text("Type de question", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Question.QuestionKind.values().forEach { kindOption ->
                    FilterChip(
                        selected = kind == kindOption,
                        onClick = { viewModel.onKindChange(kindOption) },
                        label = {
                            Text(
                                when (kindOption) {
                                    Question.QuestionKind.SOCRATIC -> "Socratique"
                                    Question.QuestionKind.CLARIFYING -> "Clarification"
                                    Question.QuestionKind.CHALLENGE -> "Contestation"
                                    Question.QuestionKind.EVIDENCE -> "Preuve"
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
