package com.argumentor.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.argumentor.app.R
import timber.log.Timber
import java.io.File

/**
 * Helper class for sharing content from ArguMentor.
 *
 * INTERNATIONALIZATION: All user-facing strings now use string resources
 * instead of hardcoded French text, properly supporting multiple languages.
 */
object ShareHelper {

    /**
     * Share a file (PDF, Markdown, JSON) via system share sheet.
     *
     * BUGFIX: Now uses string resources instead of hardcoded "Partagé depuis ArguMentor"
     * and "Partager via..." for proper internationalization.
     */
    fun shareFile(context: Context, file: File, mimeType: String = "*/*"): Boolean {
        // Validate file exists before attempting to share
        if (!file.exists()) {
            Timber.e("Cannot share file that doesn't exist: ${file.absolutePath}")
            return false
        }

        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "ArguMentor Export: ${file.nameWithoutExtension}")
                // BUGFIX: Use string resource instead of hardcoded French text
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_from_app))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // BUGFIX: Use string resource instead of hardcoded French text
            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_via)
            )
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to share file: ${file.name}")
            false
        }
    }

    /**
     * Share text content directly (for simple sharing).
     *
     * BUGFIX: Now uses string resources for internationalization.
     */
    fun shareText(context: Context, text: String, subject: String = "ArguMentor"): Boolean {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }

            // BUGFIX: Use string resource instead of hardcoded French text
            val chooserIntent = Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_via)
            )
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to share text")
            false
        }
    }

    /**
     * Share directly to email with pre-filled content.
     *
     * BUGFIX: Now uses string resources for internationalization.
     */
    fun shareViaEmail(
        context: Context,
        subject: String,
        body: String,
        attachment: File? = null
    ): Boolean {
        // Validate attachment exists if provided
        if (attachment != null && !attachment.exists()) {
            Timber.e("Cannot share email with non-existent attachment: ${attachment.absolutePath}")
            return false
        }

        return try {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (attachment != null) "message/rfc822" else "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)

                attachment?.let { file ->
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            // BUGFIX: Use string resource instead of hardcoded French text
            val chooserIntent = Intent.createChooser(
                emailIntent,
                context.getString(R.string.share_email_via)
            )
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to share via email")
            false
        }
    }

    /**
     * Get MIME type based on file extension
     */
    fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "md" -> "text/markdown"
            "json" -> "application/json"
            "txt" -> "text/plain"
            else -> "*/*"
        }
    }
}
