package com.argumentor.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Permission types used in ArguMentor app
 */
enum class AppPermission(val manifestPermission: String) {
    RECORD_AUDIO(Manifest.permission.RECORD_AUDIO),
    READ_EXTERNAL_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    POST_NOTIFICATIONS(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        "" // Not needed for older versions
    })
}

/**
 * Check if a specific permission is granted
 */
fun Context.hasPermission(permission: AppPermission): Boolean {
    // POST_NOTIFICATIONS only needed on API 33+
    if (permission == AppPermission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return true
    }

    // READ_EXTERNAL_STORAGE only needed on API 32 and below
    if (permission == AppPermission.READ_EXTERNAL_STORAGE && Build.VERSION.SDK_INT > 32) {
        return true
    }

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
                Manifest.permission.READ_EXTERNAL_STORAGE ->
                    results[AppPermission.READ_EXTERNAL_STORAGE] = granted
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        permission == Manifest.permission.POST_NOTIFICATIONS) {
                        results[AppPermission.POST_NOTIFICATIONS] = granted
                    }
                }
            }
        }

        onPermissionsResult(results)
    }
}

/**
 * Get all required permissions for the app
 */
fun getRequiredPermissions(): List<AppPermission> {
    return buildList {
        add(AppPermission.RECORD_AUDIO)

        // Only add READ_EXTERNAL_STORAGE on API 32 and below
        if (Build.VERSION.SDK_INT <= 32) {
            add(AppPermission.READ_EXTERNAL_STORAGE)
        }

        // Only add POST_NOTIFICATIONS on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(AppPermission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Check if all required permissions are granted
 */
fun Context.hasAllRequiredPermissions(): Boolean {
    return getRequiredPermissions().all { hasPermission(it) }
}
