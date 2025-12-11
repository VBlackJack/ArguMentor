package com.argumentor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.argumentor.app.R
import com.argumentor.app.ui.common.WindowSizeClass
import com.argumentor.app.ui.common.rememberWindowSizeClass
import kotlinx.coroutines.launch

/**
 * Adaptive navigation scaffold that automatically switches between:
 * - ModalNavigationDrawer for compact screens (phones)
 * - PermanentNavigationDrawer for medium/expanded screens (tablets/desktops)
 *
 * Follows Material 3 adaptive layout guidelines
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNavigationScaffold(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    content: @Composable (
        onMenuClick: () -> Unit,
        paddingValues: PaddingValues
    ) -> Unit
) {
    val windowSizeClass = rememberWindowSizeClass()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationContent = @Composable {
        NavigationDrawerContent(
            currentRoute = currentRoute,
            onNavigateToHome = {
                scope.launch { drawerState.close() }
                onNavigateToHome()
            },
            onNavigateToCreate = {
                scope.launch { drawerState.close() }
                onNavigateToCreate()
            },
            onNavigateToStatistics = {
                scope.launch { drawerState.close() }
                onNavigateToStatistics()
            },
            onNavigateToImportExport = {
                scope.launch { drawerState.close() }
                onNavigateToImportExport()
            },
            onNavigateToSettings = {
                scope.launch { drawerState.close() }
                onNavigateToSettings()
            }
        )
    }

    when {
        // Compact: Use modal drawer (phones)
        windowSizeClass.isCompact -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        navigationContent()
                    }
                }
            ) {
                content(
                    { scope.launch { drawerState.open() } },
                    PaddingValues(0.dp)
                )
            }
        }
        // Medium/Expanded: Use permanent drawer (tablets/desktops)
        else -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(280.dp)
                    ) {
                        navigationContent()
                    }
                }
            ) {
                content(
                    { /* No-op for permanent drawer */ },
                    PaddingValues(0.dp)
                )
            }
        }
    }
}

@Composable
private fun NavigationDrawerContent(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        val homeLabel = stringResource(R.string.nav_home)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = homeLabel) },
            label = { Text(homeLabel) },
            selected = currentRoute == "home",
            onClick = onNavigateToHome
        )

        val newTopicLabel = stringResource(R.string.nav_new_topic)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Add, contentDescription = newTopicLabel) },
            label = { Text(newTopicLabel) },
            selected = false,
            onClick = onNavigateToCreate
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        val statisticsLabel = stringResource(R.string.nav_statistics)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = statisticsLabel) },
            label = { Text(statisticsLabel) },
            selected = currentRoute == "statistics",
            onClick = onNavigateToStatistics
        )

        val importExportLabel = stringResource(R.string.nav_import_export)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.FileUpload, contentDescription = importExportLabel) },
            label = { Text(importExportLabel) },
            selected = currentRoute == "import_export",
            onClick = onNavigateToImportExport
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        val settingsLabel = stringResource(R.string.nav_settings)
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = settingsLabel) },
            label = { Text(settingsLabel) },
            selected = currentRoute == "settings",
            onClick = onNavigateToSettings
        )
    }
}
