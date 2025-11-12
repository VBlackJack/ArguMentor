package com.argumentor.app.ui.screens.topic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Source
import com.argumentor.app.ui.components.AppNavigationDrawerContent
import com.argumentor.app.ui.components.EngagingEmptyState
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StanceNeutral
import com.argumentor.app.ui.theme.StancePro
import androidx.compose.animation.ExperimentalAnimationApi

private const val CURRENT_ROUTE = "topic_detail"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDebate: (String) -> Unit,
    onNavigateToAddClaim: (String, String?) -> Unit,
    onNavigateToAddQuestion: (String, String?) -> Unit,
    onNavigateToAddSource: (String?) -> Unit,
    onNavigateToAddEvidence: (String) -> Unit,
    onNavigateToEditEvidence: (String, String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFallacyCatalog: () -> Unit,
    viewModel: TopicDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val topic by viewModel.topic.collectAsState()
    val claims by viewModel.claims.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // UI state preservation on configuration changes
    var showExportMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteTopicDialog by rememberSaveable { mutableStateOf(false) }
    var showSummary by rememberSaveable { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // SAF launcher for PDF export
    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            // Don't use .use {} here - the exporter closes the stream
            val successMessage = context.getString(R.string.export_pdf_success)
            val errorMessageTemplate = context.getString(R.string.export_error)
            context.contentResolver.openOutputStream(it)?.let { os ->
                viewModel.exportTopicToPdf(topicId, os) { success, error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (success) {
                                successMessage
                            } else {
                                errorMessageTemplate.format(error ?: "")
                            }
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
            // Don't use .use {} here - the exporter closes the stream
            val successMessage = context.getString(R.string.export_markdown_success)
            val errorMessageTemplate = context.getString(R.string.export_error)
            context.contentResolver.openOutputStream(it)?.let { os ->
                viewModel.exportTopicToMarkdown(topicId, os) { success, error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (success) {
                                successMessage
                            } else {
                                errorMessageTemplate.format(error ?: "")
                            }
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
            title = { Text(stringResource(R.string.topic_delete_dialog_title)) },
            text = { Text(stringResource(R.string.topic_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteTopicDialog = false
                        val deletedMessage = context.getString(R.string.topic_deleted_message)
                        viewModel.deleteTopic {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(deletedMessage)
                            }
                            onNavigateBack()
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTopicDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                currentRoute = CURRENT_ROUTE,
                drawerState = drawerState,
                scope = coroutineScope,
                onNavigateToHome = onNavigateToHome,
                onNavigateToCreate = onNavigateToCreate,
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToImportExport = onNavigateToImportExport,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFallacyCatalog = onNavigateToFallacyCatalog
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topic?.title ?: stringResource(R.string.topic_default_title),
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
                            text = { Text(stringResource(R.string.export_pdf_action)) },
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
                            text = { Text(stringResource(R.string.export_markdown_action)) },
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
                            text = { Text(stringResource(R.string.topic_delete_action), color = MaterialTheme.colorScheme.error) },
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
                    0 -> ExtendedFloatingActionButton(
                        onClick = { onNavigateToAddClaim(topicId, null) },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.fab_add_claim)) }
                    )
                    1 -> ExtendedFloatingActionButton(
                        onClick = { onNavigateToAddQuestion(topicId, null) },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.fab_add_question)) }
                    )
                    2 -> ExtendedFloatingActionButton(
                        onClick = { onNavigateToAddSource(null) },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.fab_add_source)) }
                    )
                    else -> null
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
                                    text = stringResource(R.string.topic_summary_section),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            val stateDescriptionText = stringResource(
                                if (showSummary) R.string.topic_summary_expanded else R.string.topic_summary_collapsed
                            )
                            IconButton(
                                onClick = { showSummary = !showSummary },
                                modifier = Modifier
                                    .size(28.dp)
                                    .semantics {
                                        stateDescription = stateDescriptionText
                                    }
                            ) {
                                Icon(
                                    if (showSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = stringResource(
                                        if (showSummary) R.string.topic_hide_summary else R.string.topic_show_summary
                                    ),
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
                    text = { Text(stringResource(R.string.tab_claims_count, claims.size)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text(stringResource(R.string.tab_questions_count, questions.size)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = { Text(stringResource(R.string.tab_sources_count, sources.size)) }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> ClaimsTab(
                    claims = claims,
                    viewModel = viewModel,
                    onEditClaim = { claimId ->
                        onNavigateToAddClaim(topicId, claimId)
                    },
                    onDeleteClaim = { claim ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val deletedMessage = context.getString(R.string.claim_deleted_message)
                        val undoLabel = context.getString(R.string.snackbar_undo)
                        viewModel.deleteClaim(claim) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = deletedMessage,
                                    actionLabel = undoLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreClaim(claim)
                                }
                            }
                        }
                    },
                    onAddClaim = { onNavigateToAddClaim(topicId, null) },
                    onAddEvidence = onNavigateToAddEvidence,
                    onEditEvidence = onNavigateToEditEvidence,
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                , haptic = haptic
                )
                1 -> QuestionsTab(
                    questions = questions,
                    topicId = topicId,
                    claims = claims,
                    onEditQuestion = { questionId ->
                        onNavigateToAddQuestion(topicId, questionId)
                    },
                    onDeleteQuestion = { question ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val deletedMessage = context.getString(R.string.question_deleted_message)
                        val undoLabel = context.getString(R.string.snackbar_undo)
                        viewModel.deleteQuestion(question) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = deletedMessage,
                                    actionLabel = undoLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreQuestion(question)
                                }
                            }
                        }
                    },
                    onAddQuestion = {
                        onNavigateToAddQuestion(topicId, null)
                    }
                )
                2 -> SourcesTab(
                    sources = sources,
                    onAddSource = {
                        onNavigateToAddSource(null)
                    },
                    onEditSource = { sourceId ->
                        onNavigateToAddSource(sourceId)
                    },
                    onDeleteSource = { source ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val deletedMessage = context.getString(R.string.source_deleted_message)
                        val undoLabel = context.getString(R.string.snackbar_undo)
                        viewModel.deleteSource(source) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = deletedMessage,
                                    actionLabel = undoLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreSource(source)
                                }
                            }
                        }
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
    viewModel: TopicDetailViewModel,
    onEditClaim: (String) -> Unit,
    onDeleteClaim: (Claim) -> Unit,
    onAddClaim: () -> Unit,
    onAddEvidence: (String) -> Unit,
    onEditEvidence: (String, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    if (claims.isEmpty()) {
        EngagingEmptyState(
            icon = Icons.Default.Comment,
            title = stringResource(R.string.claims_empty_title),
            description = stringResource(R.string.claims_empty_description),
            actionText = stringResource(R.string.claims_empty_action),
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
                    viewModel = viewModel,
                    onEdit = { onEditClaim(claim.id) },
                    onDelete = { onDeleteClaim(claim) },
                    onAddEvidence = { onAddEvidence(claim.id) },
                    onEditEvidence = { evidenceId -> onEditEvidence(evidenceId, claim.id) },
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                , haptic = haptic
                )
            }
        }
    }
}

@Composable
private fun ClaimCard(
    claim: Claim,
    viewModel: TopicDetailViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddEvidence: () -> Unit,
    onEditEvidence: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEvidenceSection by remember { mutableStateOf(true) }
    val evidences by viewModel.getClaimEvidences(claim.id).collectAsState(initial = emptyList())

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.claim_delete_dialog_title)) },
            text = { Text(stringResource(R.string.claim_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
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

    val stanceText = stringResource(when (claim.stance) {
        Claim.Stance.PRO -> R.string.stance_pro
        Claim.Stance.CON -> R.string.stance_con
        Claim.Stance.NEUTRAL -> R.string.stance_neutral
    })
    val strengthText = stringResource(when (claim.strength) {
        Claim.Strength.LOW -> R.string.strength_low
        Claim.Strength.MEDIUM -> R.string.strength_medium
        Claim.Strength.HIGH -> R.string.strength_high
    })
    val claimCardDescription = stringResource(R.string.content_desc_claim_card, stanceText, strengthText, claim.text)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = claimCardDescription
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
                                text = stringResource(when (claim.stance) {
                                    Claim.Stance.PRO -> R.string.stance_pro
                                    Claim.Stance.CON -> R.string.stance_con
                                    Claim.Stance.NEUTRAL -> R.string.stance_neutral
                                }),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White
                            )
                        }
                        // Divider
                        Text(
                            text = stringResource(R.string.bullet_point),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Strength text
                        Text(
                            text = stringResource(when (claim.strength) {
                                Claim.Strength.LOW -> R.string.strength_low
                                Claim.Strength.MEDIUM -> R.string.strength_medium
                                Claim.Strength.HIGH -> R.string.strength_high
                            }),
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

        // Evidence section
        if (evidences.isNotEmpty() || showEvidenceSection) {
            val cs = MaterialTheme.colorScheme
            val isDark = isSystemInDarkTheme()

            // M3 surfaces & borders
            val borderColor = cs.outlineVariant.copy(alpha = if (isDark) 0.6f else 0.9f)
            val evidenceBg = if (isDark) cs.surface else cs.surfaceVariant
            val evidenceCardBg = if (isDark) cs.surfaceVariant.copy(alpha = 0.5f) else cs.surface

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp)
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(18.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = evidenceBg
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.evidence_section_title, evidences.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = cs.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = onAddEvidence,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.evidence_add_button), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Evidence items
                    evidences.forEach { evidence ->
                        var showDeleteEvidenceDialog by remember { mutableStateOf(false) }

                        if (showDeleteEvidenceDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteEvidenceDialog = false },
                                title = { Text(stringResource(R.string.evidence_delete_dialog_title)) },
                                text = { Text(stringResource(R.string.evidence_delete_dialog_message)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val deletedEvidence = evidence
                                            val deletedMessage = context.getString(R.string.evidence_deleted_message)
                                            val undoLabel = context.getString(R.string.snackbar_undo)
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.deleteEvidence(evidence) {
                                                showDeleteEvidenceDialog = false
                                                coroutineScope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = deletedMessage,
                                                        actionLabel = undoLabel,
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreEvidence(deletedEvidence)
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteEvidenceDialog = false }) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                }
                            )
                        }

                        // Quality M3 semantic colors
                        val qualityBgColor = when (evidence.quality) {
                            com.argumentor.app.data.model.Evidence.Quality.HIGH -> cs.primaryContainer
                            com.argumentor.app.data.model.Evidence.Quality.MEDIUM -> cs.tertiaryContainer
                            com.argumentor.app.data.model.Evidence.Quality.LOW -> cs.errorContainer
                        }

                        val qualityTextColor = when (evidence.quality) {
                            com.argumentor.app.data.model.Evidence.Quality.HIGH -> cs.onPrimaryContainer
                            com.argumentor.app.data.model.Evidence.Quality.MEDIUM -> cs.onTertiaryContainer
                            com.argumentor.app.data.model.Evidence.Quality.LOW -> cs.onErrorContainer
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = evidenceCardBg
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        // Type badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(cs.surfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stringResource(when (evidence.type) {
                                                    com.argumentor.app.data.model.Evidence.EvidenceType.STUDY -> R.string.evidence_type_study
                                                    com.argumentor.app.data.model.Evidence.EvidenceType.STAT -> R.string.evidence_type_stat
                                                    com.argumentor.app.data.model.Evidence.EvidenceType.QUOTE -> R.string.evidence_type_quote
                                                    com.argumentor.app.data.model.Evidence.EvidenceType.EXAMPLE -> R.string.evidence_type_example
                                                }),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = cs.onSurfaceVariant
                                            )
                                        }
                                        // Quality badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(qualityBgColor)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stringResource(when (evidence.quality) {
                                                    com.argumentor.app.data.model.Evidence.Quality.HIGH -> R.string.evidence_quality_high
                                                    com.argumentor.app.data.model.Evidence.Quality.MEDIUM -> R.string.evidence_quality_medium
                                                    com.argumentor.app.data.model.Evidence.Quality.LOW -> R.string.evidence_quality_low
                                                }),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = qualityTextColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = evidence.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                    IconButton(
                                        onClick = { onEditEvidence(evidence.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.content_desc_edit_evidence),
                                            modifier = Modifier.size(16.dp),
                                            tint = cs.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { showDeleteEvidenceDialog = true },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.content_desc_delete_evidence),
                                            modifier = Modifier.size(16.dp),
                                            tint = cs.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
            title = stringResource(R.string.questions_empty_title),
            description = stringResource(R.string.questions_empty_description),
            actionText = stringResource(R.string.questions_empty_action),
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
                        title = { Text(stringResource(R.string.question_delete_dialog_title)) },
                        text = { Text(stringResource(R.string.question_delete_dialog_message)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    onDeleteQuestion(question)
                                }
                            ) {
                                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(R.string.action_cancel))
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
                                    text = stringResource(when (question.kind) {
                                        com.argumentor.app.data.model.Question.QuestionKind.SOCRATIC -> R.string.question_kind_socratic
                                        com.argumentor.app.data.model.Question.QuestionKind.CLARIFYING -> R.string.question_kind_clarifying
                                        com.argumentor.app.data.model.Question.QuestionKind.CHALLENGE -> R.string.question_kind_challenge
                                        com.argumentor.app.data.model.Question.QuestionKind.EVIDENCE -> R.string.question_kind_evidence
                                    }),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // Show if linked to a specific claim
                                if (question.targetId != topicId) {
                                    val targetClaim = claims.find { claim -> claim.id == question.targetId }
                                    if (targetClaim != null) {
                                        Text(stringResource(R.string.bullet_point), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = stringResource(R.string.question_target_claim),
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
                                IconButton(
                                    onClick = { onEditQuestion(question.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.accessibility_edit_question),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
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

@Composable
private fun SourcesTab(
    sources: List<Source>,
    onAddSource: () -> Unit,
    onEditSource: (String) -> Unit,
    onDeleteSource: (Source) -> Unit = {}
) {
    if (sources.isEmpty()) {
        EngagingEmptyState(
            icon = Icons.Default.MenuBook,
            title = stringResource(R.string.sources_empty_title),
            description = stringResource(R.string.sources_empty_description),
            actionText = stringResource(R.string.sources_empty_action),
            onAction = onAddSource
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sources) { source ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Title
                            Text(
                                text = source.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // Citation
                            if (!source.citation.isNullOrEmpty()) {
                                Text(
                                    text = "\"${source.citation}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // URL
                            if (!source.url.isNullOrEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = source.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Actions
                        Row {
                            // Edit button
                            IconButton(
                                onClick = { onEditSource(source.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.content_desc_edit_source),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            // Delete button
                            IconButton(
                                onClick = { onDeleteSource(source) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.content_desc_delete_source),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
