package com.argumentor.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.argumentor.app.R
import com.argumentor.app.util.AppPermission

@Composable
fun PermissionDialog(
    permission: AppPermission,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, description) = when (permission) {
        AppPermission.RECORD_AUDIO -> Pair(
            stringResource(R.string.permission_microphone_title),
            stringResource(R.string.permission_microphone_description)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.button_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_deny))
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
    val title = stringResource(R.string.permission_required_title)
    val description = buildString {
        append(stringResource(R.string.permission_rationale_intro))

        permissions.forEach { permission ->
            when (permission) {
                AppPermission.RECORD_AUDIO ->
                    append(stringResource(R.string.permission_microphone_rationale))
            }
        }

        append(stringResource(R.string.permission_saf_note))
        append(stringResource(R.string.permission_settings_note_short))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(description) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.button_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_later))
            }
        }
    )
}
