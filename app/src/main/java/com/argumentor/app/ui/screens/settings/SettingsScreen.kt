package com.argumentor.app.ui.screens.settings

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val defaultPosture by viewModel.defaultPosture.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance section
            SettingsSection(title = "Apparence") {
                SettingsSwitchItem(
                    title = "Thème sombre",
                    description = "Utiliser le thème sombre",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme() }
                )

                Divider()

                SettingsItem(title = "Taille de police") {
                    Column {
                        SettingsViewModel.FontSize.values().forEach { size ->
                            FilterChip(
                                selected = fontSize == size,
                                onClick = { viewModel.setFontSize(size) },
                                label = {
                                    Text(
                                        when (size) {
                                            SettingsViewModel.FontSize.SMALL -> "Petite"
                                            SettingsViewModel.FontSize.MEDIUM -> "Moyenne"
                                            SettingsViewModel.FontSize.LARGE -> "Grande"
                                            SettingsViewModel.FontSize.EXTRA_LARGE -> "Très grande"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Divider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // Content section
            SettingsSection(title = "Contenu") {
                SettingsItem(title = "Posture par défaut") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "neutral_critique" to "Neutre & Critique",
                            "sceptique" to "Sceptique",
                            "comparatif_academique" to "Comparatif Académique"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = defaultPosture == value,
                                onClick = { viewModel.setDefaultPosture(value) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            Divider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // About section
            SettingsSection(title = "À propos") {
                SettingsItem(title = "Auteur de l'application") {
                    Column {
                        Text("Julien Bombled", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Note : L'auteur développe uniquement l'application. " +
                            "Il n'est pas responsable du contenu stocké par les utilisateurs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                SettingsItem(title = "Version") {
                    Text("1.0.0", style = MaterialTheme.typography.bodyMedium)
                }

                Divider()

                SettingsItem(title = "Licence") {
                    Text("Apache License 2.0", style = MaterialTheme.typography.bodyMedium)
                }

                Divider()

                SettingsItem(title = "Description") {
                    Text(
                        "ArguMentor est un outil d'organisation de débats et d'arguments. " +
                        "Chaque utilisateur l'adapte à ses propres thématiques.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
