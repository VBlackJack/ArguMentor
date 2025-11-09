package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.ui.components.AppNavigationDrawerContent
import com.argumentor.app.ui.components.EngagingEmptyState
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StanceNeutral
import com.argumentor.app.ui.theme.StancePro

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDebate: (String) -> Unit,
    onNavigateToAddClaim: (String, String?) -> Unit,
    onNavigateToAddQuestion: (String, String?) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                currentRoute = "topic_detail",
                drawerState = drawerState,
                scope = coroutineScope,
                onNavigateToHome = onNavigateToHome,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToImportExport = onNavigateToImportExport,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topic?.title ?: "Sujet",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading() }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.accessibility_menu)
                            )
                        }
                    },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(topicId) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.accessibility_edit_topic)
                        )
                    }
                    IconButton(onClick = { onNavigateToDebate(topicId) }) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.accessibility_debate_mode)
                        )
                    }
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.accessibility_more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Exporter en PDF") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = stringResource(R.string.accessibility_export)
                                )
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
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = stringResource(R.string.accessibility_export)
                                )
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
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.accessibility_delete_topic),
                                    tint = MaterialTheme.colorScheme.error
                                )
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
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f)) with
                        (fadeOut() + scaleOut(targetScale = 0.8f))
                },
                label = "FAB Animation"
            ) { tab ->
                when (tab) {
                    0 -> FloatingActionButton(onClick = { onNavigateToAddClaim(topicId, null) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.accessibility_create_claim)
                        )
                    }
                    else -> FloatingActionButton(onClick = { onNavigateToAddQuestion(topicId, null) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.accessibility_create_question)
                        )
                    }
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
                                modifier = Modifier
                                    .size(28.dp)
                                    .semantics {
                                        stateDescription = if (showSummary) "Résumé développé" else "Résumé réduit"
                                    }
                            ) {
                                Icon(
                                    if (showSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showSummary) "Masquer le résumé" else "Afficher le résumé",
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
                    },
                    onAddClaim = { onNavigateToAddClaim(topicId, null) }
                )
                1 -> QuestionsTab(
                    questions = questions,
                    topicId = topicId,
                    claims = claims,
                    onEditQuestion = { questionId ->
                        onNavigateToAddQuestion(topicId, questionId)
                    },
                    onDeleteQuestion = { question ->
                        viewModel.deleteQuestion(question) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Question supprimée")
                            }
                        }
                    },
                    onAddQuestion = {
                        onNavigateToAddQuestion(topicId, null)
                    }
                )
            }
        }
    }
    }
}

@Composable
private fun ClaimsTab(
    claims: List<Claim>,
    onEditClaim: (String) -> Unit,
    onDeleteClaim: (Claim) -> Unit,
    onAddClaim: () -> Unit
) {
    if (claims.isEmpty()) {
        EngagingEmptyState(
            icon = Icons.Default.Comment,
            title = "Aucune affirmation pour l'instant",
            description = "Les affirmations sont les arguments principaux de votre débat. Ajoutez votre première affirmation pour structurer votre réflexion.",
            actionText = "Ajouter une affirmation",
            onAction = onAddClaim
        )
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

    val stanceText = when (claim.stance) {
        Claim.Stance.PRO -> "Pour"
        Claim.Stance.CON -> "Contre"
        Claim.Stance.NEUTRAL -> "Neutre"
    }
    val strengthText = when (claim.strength) {
        Claim.Strength.LOW -> "Faible"
        Claim.Strength.MEDIUM -> "Moyen"
        Claim.Strength.HIGH -> "Fort"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Affirmation $stanceText, force $strengthText: ${claim.text}"
            },
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
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.accessibility_edit_claim),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.accessibility_delete_claim),
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
private fun QuestionsTab(
    questions: List<com.argumentor.app.data.model.Question>,
    topicId: String,
    claims: List<Claim>,
    onEditQuestion: (String) -> Unit,
    onDeleteQuestion: (com.argumentor.app.data.model.Question) -> Unit,
    onAddQuestion: () -> Unit
) {
    if (questions.isEmpty()) {
        EngagingEmptyState(
            icon = Icons.Default.QuestionAnswer,
            title = "Aucune question pour l'instant",
            description = "Les questions permettent d'approfondir votre réflexion, d'explorer les nuances du sujet et de challenger vos affirmations.",
            actionText = "Ajouter une question",
            onAction = onAddQuestion
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                var showDeleteDialog by remember { mutableStateOf(false) }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Supprimer la question ?") },
                        text = { Text("Cette action est irréversible.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    onDeleteQuestion(question)
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

                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = {
                            Text(text = question.text, color = MaterialTheme.colorScheme.onSurface)
                        },
                        overlineContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (question.kind) {
                                        com.argumentor.app.data.model.Question.QuestionKind.SOCRATIC -> "Socratique"
                                        com.argumentor.app.data.model.Question.QuestionKind.CLARIFYING -> "Clarification"
                                        com.argumentor.app.data.model.Question.QuestionKind.CHALLENGE -> "Contestation"
                                        com.argumentor.app.data.model.Question.QuestionKind.EVIDENCE -> "Preuve"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Show if linked to a specific claim
                                if (question.targetId != topicId) {
                                    val targetClaim = claims.find { claim -> claim.id == question.targetId }
                                    if (targetClaim != null) {
                                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "→ Affirmation",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        },
                        supportingContent = {
                            // Show the related claim text if applicable
                            if (question.targetId != topicId) {
                                val targetClaim = claims.find { claim -> claim.id == question.targetId }
                                if (targetClaim != null) {
                                    Text(
                                        text = "\"${targetClaim.text.take(100)}${if (targetClaim.text.length > 100) "..." else ""}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onEditQuestion(question.id) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.accessibility_edit_question),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.accessibility_delete_question),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
