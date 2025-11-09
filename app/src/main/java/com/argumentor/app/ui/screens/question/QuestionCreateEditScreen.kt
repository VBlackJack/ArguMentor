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
                maxLines = 8
            )

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
