package com.argumentor.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Composable to create a permission launcher that properly tracks which permission was requested.
 *
 * This implementation fixes the race condition issue where the launcher callback wouldn't know
 * which permission was requested. We use a MutableState to track the current permission being
 * requested, ensuring the callback always knows which permission result it's handling.
 *
 * @param onPermissionResult Callback with the permission and whether it was granted
 * @return A function to launch the permission request for a given AppPermission
 */
@Composable
fun rememberPermissionLauncher(
    onPermissionResult: (AppPermission, Boolean) -> Unit
): (AppPermission) -> Unit {
    val context = LocalContext.current

    // Track which permission is currently being requested to avoid race conditions
    var currentPermission by remember { mutableStateOf<AppPermission?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Safely handle the result with the tracked permission
        currentPermission?.let { permission ->
            onPermissionResult(permission, isGranted)
            currentPermission = null  // Reset after handling
        }
    }

    return remember(launcher, context) {
        { permission: AppPermission ->
            if (context.hasPermission(permission)) {
                // Permission already granted, call callback immediately
                onPermissionResult(permission, true)
            } else {
                // Track the permission before launching to avoid race condition
                currentPermission = permission
                launcher.launch(permission.manifestPermission)
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
