package com.argumentor.app.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.repository.Statistics
import com.argumentor.app.data.repository.TopicStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statistics by viewModel.statistics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Card
            item {
                OverviewCard(statistics)
            }

            // Claims by Stance
            item {
                StanceDistributionCard(statistics.claimsByStance)
            }

            // Claims by Strength
            item {
                StrengthDistributionCard(statistics.claimsByStrength)
            }

            // Topics by Posture
            item {
                PostureDistributionCard(statistics.topicsByPosture)
            }

            // Averages Card
            item {
                AveragesCard(statistics)
            }

            // Most Debated Topics
            if (statistics.mostDebatedTopics.isNotEmpty()) {
                item {
                    Text(
                        text = "Topics les plus débattus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(statistics.mostDebatedTopics) { topicStats ->
                    TopicStatsCard(topicStats)
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(statistics: Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Vue d'ensemble",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Topic,
                    label = "Topics",
                    value = statistics.totalTopics.toString()
                )
                StatItem(
                    icon = Icons.Default.Chat,
                    label = "Arguments",
                    value = statistics.totalClaims.toString()
                )
                StatItem(
                    icon = Icons.Default.Reply,
                    label = "Contre-args",
                    value = statistics.totalRebuttals.toString()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Article,
                    label = "Preuves",
                    value = statistics.totalEvidence.toString()
                )
                StatItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Questions",
                    value = statistics.totalQuestions.toString()
                )
                StatItem(
                    icon = Icons.Default.Source,
                    label = "Sources",
                    value = statistics.totalSources.toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StanceDistributionCard(claimsByStance: Map<Claim.Stance, Int>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribution des arguments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val total = claimsByStance.values.sum()
            if (total > 0) {
                Claim.Stance.values().forEach { stance ->
                    val count = claimsByStance[stance] ?: 0
                    val percentage = (count.toFloat() / total * 100).toInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (icon, color) = when (stance) {
                            Claim.Stance.PRO -> Icons.Default.ThumbUp to MaterialTheme.colorScheme.primary
                            Claim.Stance.CON -> Icons.Default.ThumbDown to MaterialTheme.colorScheme.error
                            Claim.Stance.NEUTRAL -> Icons.Default.Balance to MaterialTheme.colorScheme.tertiary
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = color
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stance.name,
                            modifier = Modifier.width(80.dp)
                        )

                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp),
                            color = color
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$count ($percentage%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucun argument pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StrengthDistributionCard(claimsByStrength: Map<Claim.Strength, Int>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Force des arguments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val total = claimsByStrength.values.sum()
            if (total > 0) {
                Claim.Strength.values().forEach { strength ->
                    val count = claimsByStrength[strength] ?: 0
                    val percentage = (count.toFloat() / total * 100).toInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strength.name,
                            modifier = Modifier.width(80.dp)
                        )

                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$count ($percentage%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucun argument pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostureDistributionCard(topicsByPosture: Map<Topic.Posture, Int>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Postures des topics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val total = topicsByPosture.values.sum()
            if (total > 0) {
                Topic.Posture.values().forEach { posture ->
                    val count = topicsByPosture[posture] ?: 0
                    val percentage = (count.toFloat() / total * 100).toInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = posture.name.replace("_", " "),
                            modifier = Modifier.width(150.dp),
                            style = MaterialTheme.typography.bodySmall
                        )

                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                Text(
                    text = "Aucun topic pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AveragesCard(statistics: Statistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Moyennes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            AverageItem(
                label = "Arguments par topic",
                value = String.format("%.1f", statistics.averageClaimsPerTopic)
            )

            AverageItem(
                label = "Contre-arguments par argument",
                value = String.format("%.1f", statistics.averageRebuttalsPerClaim)
            )

            AverageItem(
                label = "Force moyenne",
                value = String.format("%.1f/3.0", statistics.averageStrength)
            )
        }
    }
}

@Composable
private fun AverageItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TopicStatsCard(topicStats: TopicStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = topicStats.topicTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallStatItem("Arguments", topicStats.claimCount)
                SmallStatItem("Contre-args", topicStats.rebuttalCount)
                SmallStatItem("Preuves", topicStats.evidenceCount)
                SmallStatItem("Questions", topicStats.questionCount)
            }
        }
    }
}

@Composable
private fun SmallStatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
