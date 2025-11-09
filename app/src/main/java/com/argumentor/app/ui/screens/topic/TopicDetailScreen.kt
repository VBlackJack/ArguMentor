package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val topic by viewModel.topic.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showExportMenu by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // SAF launcher for PDF export
    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os ->
                viewModel.exportTopicToPdf(topicId, os) { success, error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (success) "PDF exporté avec succès" else "Erreur: $error"
                        )
                    }
                }
            }
        }
    }

    // SAF launcher for Markdown export
    val exportMdLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os ->
                viewModel.exportTopicToMarkdown(topicId, os) { success, error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (success) "Markdown exporté avec succès" else "Erreur: $error"
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(topicId) {
        viewModel.loadTopic(topicId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Plus d'options")
                    }
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exporter en PDF") },
                            leadingIcon = {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            },
                            onClick = {
                                showExportMenu = false
                                val timestamp = System.currentTimeMillis()
                                val title = topic?.title?.take(20)?.replace(" ", "_") ?: "topic"
                                exportPdfLauncher.launch("ArguMentor_${title}_$timestamp.pdf")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exporter en Markdown") },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null)
                            },
                            onClick = {
                                showExportMenu = false
                                val timestamp = System.currentTimeMillis()
                                val title = topic?.title?.take(20)?.replace(" ", "_") ?: "topic"
                                exportMdLauncher.launch("ArguMentor_${title}_$timestamp.md")
                            }
                        )
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
            // Topic summary card (collapsible)
            topic?.let { currentTopic ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Résumé",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showSummary = !showSummary },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (showSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showSummary) "Masquer" else "Afficher"
                                )
                            }
                        }
                        if (showSummary) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentTopic.summary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (currentTopic.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    currentTopic.tags.forEach { tag ->
                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
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
                0 -> ClaimsTab(
                    claims = claims,
                    onEditClaim = { claimId ->
                        // Navigate to edit claim screen
                        // For now we'll use the same screen as create
                        onNavigateToAddClaim("${topicId}?claimId=${claimId}")
                    },
                    onDeleteClaim = { claim ->
                        viewModel.deleteClaim(claim) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Affirmation supprimée")
                            }
                        }
                    }
                )
                1 -> QuestionsTab(questions = questions)
            }
        }
    }
}

@Composable
private fun ClaimsTab(
    claims: List<Claim>,
    onEditClaim: (String) -> Unit,
    onDeleteClaim: (Claim) -> Unit
) {
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
            items(claims, key = { it.id }) { claim ->
                ClaimCard(
                    claim = claim,
                    onEdit = { onEditClaim(claim.id) },
                    onDelete = { onDeleteClaim(claim) }
                )
            }
        }
    }
}

@Composable
private fun ClaimCard(
    claim: Claim,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'affirmation ?") },
            text = { Text("Cette action est irréversible. Toutes les réfutations et preuves associées seront également supprimées.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
