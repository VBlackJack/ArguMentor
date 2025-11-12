package com.argumentor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.argumentor.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Reusable navigation drawer content for the app.
 * Shows menu items for navigation across the app.
 */
@Composable
fun AppNavigationDrawerContent(
    currentRoute: String,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFallacyCatalog: (() -> Unit)? = null
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                label = { Text(stringResource(R.string.nav_home)) },
                selected = currentRoute == "home",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToHome()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nav_new_topic)) },
                label = { Text(stringResource(R.string.nav_new_topic)) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToCreate()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_statistics)) },
                label = { Text(stringResource(R.string.nav_statistics)) },
                selected = currentRoute == "statistics",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToStatistics()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.FileUpload, contentDescription = stringResource(R.string.nav_import_export)) },
                label = { Text(stringResource(R.string.nav_import_export)) },
                selected = currentRoute == "import_export",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToImportExport()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                label = { Text(stringResource(R.string.nav_settings)) },
                selected = currentRoute == "settings",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                }
            )

            // Fallacy catalog (optional)
            onNavigateToFallacyCatalog?.let { callback ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.nav_fallacy_catalog)) },
                    label = { Text(stringResource(R.string.nav_fallacy_catalog)) },
                    selected = currentRoute == "fallacy/catalog",
                    onClick = {
                        scope.launch { drawerState.close() }
                        callback()
                    }
                )
            }
        }
    }
}
