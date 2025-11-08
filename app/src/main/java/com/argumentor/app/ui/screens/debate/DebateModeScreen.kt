package com.argumentor.app.ui.screens.debate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.ui.theme.StanceCon
import com.argumentor.app.ui.theme.StancePro

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

    LaunchedEffect(topicId) {
        viewModel.loadTopic(topicId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mode Débat - ${topic?.title ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetProgress() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recommencer")
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
                Text("Aucune carte disponible")
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
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentCardIndex + 1) / debateCards.size.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Carte ${currentCardIndex + 1} / ${debateCards.size}",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Flip card
                FlipCard(
                    card = currentCard,
                    isFlipped = isCardFlipped,
                    onFlip = { viewModel.flipCard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousCard() },
                        enabled = currentCardIndex > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Précédent")
                    }

                    Button(
                        onClick = { viewModel.flipCard() }
                    ) {
                        Text(if (isCardFlipped) "Voir Claim" else "Voir Réponse")
                    }

                    Button(
                        onClick = { viewModel.nextCard() },
                        enabled = currentCardIndex < debateCards.size - 1
                    ) {
                        Text("Suivant")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
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
            .clickable { onFlip() }
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
    }
}

@Composable
private fun ClaimBack(card: DebateCard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rebuttals
        if (card.rebuttals.isNotEmpty()) {
            Text("Contre-arguments:", style = MaterialTheme.typography.titleMedium)
            card.rebuttals.forEach { rebuttal ->
                Card(colors = CardDefaults.cardColors(containerColor = StanceCon.copy(alpha = 0.1f))) {
                    Text(
                        text = rebuttal.text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Question
        if (card.questions.isNotEmpty()) {
            Text("Question:", style = MaterialTheme.typography.titleMedium)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(
                    text = card.questions.first().text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
