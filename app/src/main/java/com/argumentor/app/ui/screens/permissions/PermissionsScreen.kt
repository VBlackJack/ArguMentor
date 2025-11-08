package com.argumentor.app.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.argumentor.app.util.*

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(context.hasAllRequiredPermissions())
    }

    val requiredPermissions = remember { getRequiredPermissions() }

    val permissionLauncher = rememberMultiplePermissionsLauncher { results ->
        permissionsGranted = results.values.all { it }
        if (permissionsGranted) {
            onAllPermissionsGranted()
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            onAllPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permissions requises",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ArguMentor a besoin de quelques permissions pour fonctionner correctement.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        requiredPermissions.forEach { permission ->
            PermissionItem(
                permission = permission,
                isGranted = context.hasPermission(permission)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val permissionsToRequest = requiredPermissions
                    .filter { !context.hasPermission(it) }
                    .map { it.manifestPermission }
                    .toTypedArray()

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest)
                } else {
                    onAllPermissionsGranted()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Autoriser les permissions")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSkip) {
            Text("Passer pour le moment")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Vous pourrez modifier ces permissions à tout moment dans les paramètres de l'application.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionItem(
    permission: AppPermission,
    isGranted: Boolean
) {
    val (icon, title, description) = when (permission) {
        AppPermission.RECORD_AUDIO -> Triple(
            Icons.Default.Mic,
            "Microphone",
            "Pour la reconnaissance vocale (Speech-to-Text)"
        )
        AppPermission.READ_EXTERNAL_STORAGE -> Triple(
            Icons.Default.Storage,
            "Stockage",
            "Pour importer et exporter vos données"
        )
        AppPermission.POST_NOTIFICATIONS -> Triple(
            Icons.Default.Notifications,
            "Notifications",
            "Pour les rappels de révision"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isGranted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isGranted)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accordée",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
