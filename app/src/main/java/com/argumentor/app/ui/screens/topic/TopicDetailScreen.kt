package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StanceNeutral
import com.argumentor.app.ui.theme.StancePro

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDebate: (String) -> Unit,
    onNavigateToAddClaim: (String, String?) -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val topic by viewModel.topic.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showExportMenu by remember { mutableStateOf(false) }
    var showDeleteTopicDialog by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(true) }
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

    // Delete topic confirmation dialog
    if (showDeleteTopicDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTopicDialog = false },
            title = { Text("Supprimer le sujet ?") },
            text = { Text("Cette action est irréversible. Toutes les affirmations, réfutations, preuves et questions associées seront également supprimées.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteTopicDialog = false
                        viewModel.deleteTopic {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Sujet supprimé")
                            }
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTopicDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topic?.title ?: "Sujet",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Supprimer le sujet", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                showExportMenu = false
                                showDeleteTopicDialog = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { onNavigateToAddClaim(topicId, null) }) {
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Résumé du sujet",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showSummary = !showSummary },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (showSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showSummary) "Masquer" else "Afficher",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (showSummary) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = currentTopic.summary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentTopic.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                val isDark = isSystemInDarkTheme()
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentTopic.tags.forEach { tag ->
                                        AssistChip(
                                            onClick = { },
                                            label = {
                                                Text(
                                                    tag,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (isDark) Color(0xFF2A2F36) else Color(0xFFE9EEF6),
                                                labelColor = if (isDark) Color(0xFFEEF2F6) else Color(0xFF263238)
                                            )
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
                        onNavigateToAddClaim(topicId, claimId)
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

    val isDark = isSystemInDarkTheme()

    // Backgrounds adaptés au mode sombre
    val backgroundColor = when (claim.stance) {
        Claim.Stance.PRO -> if (isDark) Color(0xFF1A2E1A) else Color(0xFFDFF7DF)
        Claim.Stance.CON -> if (isDark) Color(0xFF2E1A1A) else Color(0xFFFBE4E4)
        Claim.Stance.NEUTRAL -> if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    }

    // Badges avec contraste garanti (couleurs foncées + texte blanc)
    val stanceColor = when (claim.stance) {
        Claim.Stance.PRO -> Color(0xFF1B5E20)  // Vert foncé
        Claim.Stance.CON -> Color(0xFFB71C1C)  // Rouge foncé
        Claim.Stance.NEUTRAL -> Color(0xFF424242)  // Gris foncé
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = claim.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            overlineContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    // Stance badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(stanceColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (claim.stance) {
                                Claim.Stance.PRO -> "Pour"
                                Claim.Stance.CON -> "Contre"
                                Claim.Stance.NEUTRAL -> "Neutre"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                    // Divider
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Strength text
                    Text(
                        text = when (claim.strength) {
                            Claim.Strength.LOW -> "Faible"
                            Claim.Strength.MEDIUM -> "Moyen"
                            Claim.Strength.HIGH -> "Fort"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun QuestionsTab(questions: List<com.argumentor.app.data.model.Question>) {
    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aucune question pour l'instant",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Les questions permettent d'approfondir votre réflexion et d'explorer les nuances du sujet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
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
