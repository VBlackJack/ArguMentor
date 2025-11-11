package com.argumentor.app.ui.screens.debate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
                                Icon(Icons.Default.List, contentDescription = "Sélectionner une carte")
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
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400), label = ""
    )

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
                stateDescription = if (isFlipped) "Affichage de la réponse" else "Affichage de l'affirmation"
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (rotation <= 90f) {
                // Front side: Claim
                ClaimFront(card = card)
            } else {
                // Back side: Rebuttals + Sources + Question
                ClaimBack(card = card, modifier = Modifier.graphicsLayer { rotationY = 180f })
            }
        }
    }
}

@Composable
private fun ClaimFront(card: DebateCard) {
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
                    com.argumentor.app.data.model.Claim.Stance.PRO -> "POUR"
                    com.argumentor.app.data.model.Claim.Stance.CON -> "CONTRE"
                    com.argumentor.app.data.model.Claim.Stance.NEUTRAL -> "NEUTRE"
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.claim.fallacyIds.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ClaimBack(card: DebateCard, modifier: Modifier = Modifier) {
    val hasContent = card.rebuttals.isNotEmpty() || card.evidences.isNotEmpty() || card.questions.isNotEmpty()

    Column(
        modifier = modifier.fillMaxSize(),
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
                                    text = stringResource(R.string.debate_fallacy, rebuttal.fallacyIds.joinToString(", ")),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                                    text = "Avec source",
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
