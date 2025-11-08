package com.argumentor.app.ui.screens.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StanceNeutral
import com.argumentor.app.ui.theme.StancePro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDebate: (String) -> Unit,
    onNavigateToAddClaim: (String) -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel()
) {
    val topic by viewModel.topic.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(topicId) {
        viewModel.loadTopic(topicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.title ?: "Sujet") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(topicId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier")
                    }
                    IconButton(onClick = { onNavigateToDebate(topicId) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Mode Débat")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { onNavigateToAddClaim(topicId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une affirmation")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Topic summary card
            topic?.let { currentTopic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentTopic.summary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (currentTopic.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                currentTopic.tags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Affirmations (${claims.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Questions (${questions.size})") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> ClaimsTab(claims = claims)
                1 -> QuestionsTab(questions = questions)
            }
        }
    }
}

@Composable
private fun ClaimsTab(claims: List<Claim>) {
    if (claims.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Aucune affirmation", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(claims) { claim ->
                ClaimCard(claim = claim)
            }
        }
    }
}

@Composable
private fun ClaimCard(claim: Claim) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (claim.stance) {
                Claim.Stance.PRO -> StancePro.copy(alpha = 0.1f)
                Claim.Stance.CON -> StanceCon.copy(alpha = 0.1f)
                Claim.Stance.NEUTRAL -> StanceNeutral.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (claim.stance) {
                        Claim.Stance.PRO -> "Pour"
                        Claim.Stance.CON -> "Contre"
                        Claim.Stance.NEUTRAL -> "Neutre"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (claim.stance) {
                        Claim.Stance.PRO -> StancePro
                        Claim.Stance.CON -> StanceCon
                        Claim.Stance.NEUTRAL -> StanceNeutral
                    }
                )
                Text(
                    text = when (claim.strength) {
                        Claim.Strength.LOW -> "Faible"
                        Claim.Strength.MEDIUM -> "Moyen"
                        Claim.Strength.HIGH -> "Fort"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = claim.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun QuestionsTab(questions: List<com.argumentor.app.data.model.Question>) {
    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Aucune question", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = when (question.kind) {
                                com.argumentor.app.data.model.Question.QuestionKind.SOCRATIC -> "Socratique"
                                com.argumentor.app.data.model.Question.QuestionKind.CLARIFYING -> "Clarification"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = question.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
