package com.argumentor.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.argumentor.app.util.AppPermission

@Composable
fun PermissionDialog(
    permission: AppPermission,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, description) = when (permission) {
        AppPermission.RECORD_AUDIO -> Pair(
            "Permission Microphone",
            "ArguMentor a besoin d'accéder au microphone pour la fonctionnalité de reconnaissance vocale (Speech-to-Text). Cela vous permet de dicter vos arguments rapidement."
        )
        AppPermission.READ_EXTERNAL_STORAGE -> Pair(
            "Permission Stockage",
            "ArguMentor a besoin d'accéder au stockage pour importer et exporter vos données d'argumentation au format JSON."
        )
        AppPermission.POST_NOTIFICATIONS -> Pair(
            "Permission Notifications",
            "ArguMentor peut vous envoyer des notifications pour vous rappeler de réviser vos débats et arguments."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Autoriser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Refuser")
            }
        }
    )
}

@Composable
fun PermissionRationaleDialog(
    permissions: List<AppPermission>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = "Permissions requises"
    val description = buildString {
        append("ArguMentor a besoin des permissions suivantes pour fonctionner correctement :\n\n")

        permissions.forEach { permission ->
            when (permission) {
                AppPermission.RECORD_AUDIO ->
                    append("• Microphone : pour la reconnaissance vocale\n")
                AppPermission.READ_EXTERNAL_STORAGE ->
                    append("• Stockage : pour importer/exporter vos données\n")
                AppPermission.POST_NOTIFICATIONS ->
                    append("• Notifications : pour les rappels de révision\n")
            }
        }

        append("\nVous pouvez modifier ces permissions à tout moment dans les paramètres de l'application.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Continuer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Plus tard")
            }
        }
    )
}
