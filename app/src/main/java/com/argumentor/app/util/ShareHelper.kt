package com.argumentor.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper class for sharing content from ArguMentor.
 */
object ShareHelper {

    /**
     * Share a file (PDF, Markdown, JSON) via system share sheet
     */
    fun shareFile(context: Context, file: File, mimeType: String = "*/*"): Boolean {
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
                putExtra(Intent.EXTRA_TEXT, "Partagé depuis ArguMentor")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Partager via...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Share text content directly (for simple sharing)
     */
    fun shareText(context: Context, text: String, subject: String = "ArguMentor"): Boolean {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Partager via...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Share directly to email with pre-filled content
     */
    fun shareViaEmail(
        context: Context,
        subject: String,
        body: String,
        attachment: File? = null
    ): Boolean {
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

            val chooserIntent = Intent.createChooser(emailIntent, "Envoyer par email...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            true
        } catch (e: Exception) {
            e.printStackTrace()
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
