package com.argumentor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onNavigateToSettings: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "ArguMentor",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Accueil") },
                selected = currentRoute == "home",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToHome()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                label = { Text("Nouveau sujet") },
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
                icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                label = { Text("Statistiques") },
                selected = currentRoute == "statistics",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToStatistics()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                label = { Text("Import/Export") },
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
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Paramètres") },
                selected = currentRoute == "settings",
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                }
            )
        }
    }
}
