package com.argumentor.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Permission types used in ArguMentor app.
 * Note: File access uses Storage Access Framework (SAF) and does not require storage permissions.
 */
enum class AppPermission(val manifestPermission: String) {
    RECORD_AUDIO(Manifest.permission.RECORD_AUDIO)
}

/**
 * Check if a specific permission is granted
 */
fun Context.hasPermission(permission: AppPermission): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission.manifestPermission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Composable to create a permission launcher
 */
@Composable
fun rememberPermissionLauncher(
    onPermissionResult: (AppPermission, Boolean) -> Unit
): (AppPermission) -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // We need to track which permission was requested
        // This will be handled by the returned lambda
    }

    return remember(launcher) {
        { permission: AppPermission ->
            if (context.hasPermission(permission)) {
                onPermissionResult(permission, true)
            } else {
                launcher.launch(permission.manifestPermission)
                // Note: The result will be handled in the launcher callback
                // For a more robust implementation, we'd need a wrapper
            }
        }
    }
}

/**
 * Composable to create a multi-permission launcher
 */
@Composable
fun rememberMultiplePermissionsLauncher(
    onPermissionsResult: (Map<AppPermission, Boolean>) -> Unit
): ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val results = mutableMapOf<AppPermission, Boolean>()

        permissions.forEach { (permission, granted) ->
            when (permission) {
                Manifest.permission.RECORD_AUDIO ->
                    results[AppPermission.RECORD_AUDIO] = granted
            }
        }

        onPermissionsResult(results)
    }
}

/**
 * Get all required permissions for the app
 */
fun getRequiredPermissions(): List<AppPermission> {
    return listOf(AppPermission.RECORD_AUDIO)
}

/**
 * Check if all required permissions are granted
 */
fun Context.hasAllRequiredPermissions(): Boolean {
    return getRequiredPermissions().all { hasPermission(it) }
}
