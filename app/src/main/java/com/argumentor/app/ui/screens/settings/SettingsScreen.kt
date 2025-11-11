package com.argumentor.app.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.BuildConfig
import com.argumentor.app.R
import com.argumentor.app.data.preferences.AppLanguage

/**
 * Delay before restarting the app after language change to ensure preferences are saved.
 */
private const val LANGUAGE_CHANGE_RESTART_DELAY_MS = 500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isImmersiveMode by viewModel.isImmersiveMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val defaultPosture by viewModel.defaultPosture.collectAsState()
    val language by viewModel.language.collectAsState()
    val pendingLanguage by viewModel.pendingLanguage.collectAsState()
    val tutorialEnabled by viewModel.tutorialEnabled.collectAsState()
    val context = LocalContext.current
    var showRestartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.accessibility_back))
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
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_dark_theme),
                    description = stringResource(R.string.settings_dark_theme_description),
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme() }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsSwitchItem(
                    title = stringResource(R.string.settings_immersive_mode),
                    description = stringResource(R.string.settings_immersive_mode_description),
                    checked = isImmersiveMode,
                    onCheckedChange = { viewModel.toggleImmersiveMode() }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(title = stringResource(R.string.settings_font_size)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingsViewModel.FontSize.values().forEach { size ->
                            FilterChip(
                                selected = fontSize == size,
                                onClick = { viewModel.setFontSize(size) },
                                label = {
                                    Text(
                                        when (size) {
                                            SettingsViewModel.FontSize.SMALL -> stringResource(R.string.settings_font_small)
                                            SettingsViewModel.FontSize.MEDIUM -> stringResource(R.string.settings_font_medium)
                                            SettingsViewModel.FontSize.LARGE -> stringResource(R.string.settings_font_large)
                                            SettingsViewModel.FontSize.EXTRA_LARGE -> stringResource(R.string.settings_font_extra_large)
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language section
            SettingsSection(title = stringResource(R.string.settings_language)) {
                SettingsItem(title = stringResource(R.string.settings_language_app)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppLanguage.values().forEach { lang ->
                                FilterChip(
                                    selected = (pendingLanguage ?: language) == lang,
                                    onClick = { viewModel.selectLanguage(lang) },
                                    label = { Text(lang.displayName) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.settings_language_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (pendingLanguage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showRestartDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.settings_apply_and_restart))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content section
            SettingsSection(title = stringResource(R.string.settings_content)) {
                SettingsItem(title = stringResource(R.string.settings_default_posture)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "neutral_critique" to stringResource(R.string.posture_neutral_critique),
                            "sceptique" to stringResource(R.string.posture_sceptique),
                            "comparatif_academique" to stringResource(R.string.posture_comparatif_academique)
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = defaultPosture == value,
                                onClick = { viewModel.setDefaultPosture(value) },
                                label = { Text(label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tutorial section
            SettingsSection(title = stringResource(R.string.settings_tutorial)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_tutorial_enabled),
                    description = stringResource(R.string.settings_tutorial_enabled_description),
                    checked = tutorialEnabled,
                    onCheckedChange = { viewModel.toggleTutorialEnabled() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permissions section
            SettingsSection(title = stringResource(R.string.settings_permissions)) {
                SettingsItem(title = stringResource(R.string.settings_permissions_microphone_title)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_permissions_microphone_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_permissions_open_settings))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            SettingsSection(title = stringResource(R.string.settings_about)) {
                SettingsItem(title = stringResource(R.string.settings_app_author)) {
                    Column {
                        Text(stringResource(R.string.about_author_name), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.settings_author_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(title = stringResource(R.string.settings_version)) {
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(title = stringResource(R.string.settings_license)) {
                    Text(stringResource(R.string.about_license_name), style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(title = stringResource(R.string.settings_description)) {
                    Text(
                        stringResource(R.string.settings_app_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Restart dialog
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { showRestartDialog = false },
                title = { Text(stringResource(R.string.settings_restart_required_title)) },
                text = { Text(stringResource(R.string.settings_restart_required_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.applyLanguageChange {
                                // Wait for save to complete, then restart gracefully
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    (context as? Activity)?.recreate()
                                }, LANGUAGE_CHANGE_RESTART_DELAY_MS)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.settings_restart_now))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.cancelLanguageChange()
                            showRestartDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
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
