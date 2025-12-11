package com.argumentor.app.ui.screens.debate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.R
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StancePro
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebateModeScreen(
    topicId: String,
    onNavigateBack: () -> Unit,
    onNavigateToFallacyDetail: (String) -> Unit = {},
    viewModel: DebateModeViewModel = hiltViewModel()
) {
    val topic by viewModel.topic.collectAsState()
    val debateCards by viewModel.debateCards.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val isCardFlipped by viewModel.isCardFlipped.collectAsState()
    val cardsReviewed by viewModel.cardsReviewed.collectAsState()
    val sessionScore by viewModel.sessionScore.collectAsState()
    val streak by viewModel.streak.collectAsState()
    var showCardMenu by remember { mutableStateOf(false) }

    LaunchedEffect(topicId) {
        viewModel.loadTopic(topicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${stringResource(R.string.debate_mode)} - ${topic?.title ?: ""}",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.accessibility_back))
                    }
                },
                actions = {
                    // Card selector menu
                    if (debateCards.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showCardMenu = true }) {
                                Icon(Icons.Default.List, contentDescription = stringResource(R.string.debate_select_card))
                            }
                            DropdownMenu(
                                expanded = showCardMenu,
                                onDismissRequest = { showCardMenu = false }
                            ) {
                                debateCards.forEachIndexed { index, card ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "${index + 1}.",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (index == currentCardIndex)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = card.claim.text,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (index == currentCardIndex)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.goToCard(index)
                                            showCardMenu = false
                                        },
                                        leadingIcon = if (index == currentCardIndex) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = { viewModel.resetProgress() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.accessibility_debate_mode))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (debateCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.debate_no_cards))
            }
        } else {
            val currentCard = debateCards[currentCardIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Progress indicator with live region
                LinearProgressIndicator(
                    progress = (currentCardIndex + 1) / debateCards.size.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.debate_card_progress, currentCardIndex + 1, debateCards.size),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )

                    // Gamification stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        // Score
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$sessionScore",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Streak
                        if (streak >= 3) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    "$streak",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Cards reviewed
                        Text(
                            "${cardsReviewed.size}/${debateCards.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Flip card with swipe gestures
                FlipCard(
                    card = currentCard,
                    isFlipped = isCardFlipped,
                    onFlip = { viewModel.flipCard() },
                    onSwipeLeft = {
                        if (currentCardIndex < debateCards.size - 1) {
                            viewModel.nextCard()
                        }
                    },
                    onSwipeRight = {
                        if (currentCardIndex > 0) {
                            viewModel.previousCard()
                        }
                    },
                    onFallacyClick = onNavigateToFallacyDetail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons - Column layout for better mobile UX
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flip button (most important action)
                    Button(
                        onClick = { viewModel.flipCard() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isCardFlipped)
                                stringResource(R.string.debate_view_claim)
                            else
                                stringResource(R.string.debate_view_answer)
                        )
                    }

                    // Previous/Next navigation row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousCard() },
                            enabled = currentCardIndex > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.debate_previous))
                        }

                        Button(
                            onClick = { viewModel.nextCard() },
                            enabled = currentCardIndex < debateCards.size - 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.debate_next))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(
    card: DebateCard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onFallacyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400), label = ""
    )

    // Capture state description using stringResource
    val stateDescriptionText = if (isFlipped)
        stringResource(R.string.debate_showing_answer)
    else
        stringResource(R.string.debate_showing_claim)

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        // Detect swipe threshold (100dp minimum)
                        if (abs(totalDrag) > 100f) {
                            if (totalDrag > 0) {
                                // Swipe right (previous card)
                                onSwipeRight()
                            } else {
                                // Swipe left (next card)
                                onSwipeLeft()
                            }
                        }
                    },
                    onDragCancel = { totalDrag = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                )
            }
            .clickable { onFlip() }
            .semantics {
                stateDescription = stateDescriptionText
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (rotation <= 90f) {
                // Front side: Claim
                ClaimFront(card = card, onFallacyClick = onFallacyClick)
            } else {
                // Back side: Rebuttals + Sources + Question
                ClaimBack(
                    card = card,
                    onFallacyClick = onFallacyClick,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
private fun ClaimFront(
    card: DebateCard,
    onFallacyClick: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // Stance badge
        Surface(
            color = when (card.claim.stance) {
                com.argumentor.app.data.model.Claim.Stance.PRO -> StancePro
                com.argumentor.app.data.model.Claim.Stance.CON -> StanceCon
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = when (card.claim.stance) {
                    com.argumentor.app.data.model.Claim.Stance.PRO -> stringResource(R.string.debate_stance_pro)
                    com.argumentor.app.data.model.Claim.Stance.CON -> stringResource(R.string.debate_stance_con)
                    com.argumentor.app.data.model.Claim.Stance.NEUTRAL -> stringResource(R.string.debate_stance_neutral)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = card.claim.text,
            style = MaterialTheme.typography.headlineSmall
        )

        // Display fallacies identified in the claim itself (if any)
        if (card.claim.fallacyIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.claim_fallacies),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Display each fallacy with its name and description
                    card.claim.fallacyIds.forEach { fallacyId ->
                        val fallacy = card.fallacies[fallacyId]
                        val localizedFallacy = com.argumentor.app.data.constants.FallacyCatalog.getFallacyById(context, fallacyId)

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onFallacyClick(fallacyId) },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = localizedFallacy?.name ?: fallacy?.name ?: fallacyId,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.debate_view_details),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (localizedFallacy != null || fallacy != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (localizedFallacy?.description ?: fallacy?.description ?: "")
                                            .take(80) + if ((localizedFallacy?.description ?: fallacy?.description ?: "").length > 80) "..." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        maxLines = 2
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

@Composable
private fun ClaimBack(
    card: DebateCard,
    onFallacyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasContent = card.rebuttals.isNotEmpty() || card.evidences.isNotEmpty() || card.questions.isNotEmpty()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = if (hasContent) Arrangement.spacedBy(16.dp) else Arrangement.Center
    ) {
        if (!hasContent) {
            // Empty state for CON claims with no supporting content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.debate_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.debate_empty_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Rebuttals
            if (card.rebuttals.isNotEmpty()) {
                Text(stringResource(R.string.debate_rebuttals), style = MaterialTheme.typography.titleMedium)
                card.rebuttals.forEach { rebuttal ->
                    Card(colors = CardDefaults.cardColors(containerColor = StanceCon.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = rebuttal.text,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (rebuttal.fallacyIds.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.debate_fallacies_identified),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Display each fallacy with its name
                                rebuttal.fallacyIds.forEach { fallacyId ->
                                    val fallacy = card.fallacies[fallacyId]
                                    val localizedFallacy = com.argumentor.app.data.constants.FallacyCatalog.getFallacyById(context, fallacyId)

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable { onFallacyClick(fallacyId) },
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = localizedFallacy?.name ?: fallacy?.name ?: fallacyId,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = stringResource(R.string.debate_view_details),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Evidence (NEW - was missing!)
            if (card.evidences.isNotEmpty()) {
                Text(stringResource(R.string.debate_evidence), style = MaterialTheme.typography.titleMedium)
                card.evidences.forEach { evidence ->
                    Card(colors = CardDefaults.cardColors(containerColor = StancePro.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = evidence.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = evidence.type.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (evidence.sourceId != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.debate_with_source),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Question
            if (card.questions.isNotEmpty()) {
                Text(stringResource(R.string.debate_question), style = MaterialTheme.typography.titleMedium)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = card.questions.first().text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (card.questions.first().kind) {
                                com.argumentor.app.data.model.Question.QuestionKind.SOCRATIC -> stringResource(R.string.question_kind_socratic)
                                com.argumentor.app.data.model.Question.QuestionKind.CLARIFYING -> stringResource(R.string.question_kind_clarifying)
                                com.argumentor.app.data.model.Question.QuestionKind.CHALLENGE -> stringResource(R.string.question_kind_challenge)
                                com.argumentor.app.data.model.Question.QuestionKind.EVIDENCE -> stringResource(R.string.question_kind_evidence)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
