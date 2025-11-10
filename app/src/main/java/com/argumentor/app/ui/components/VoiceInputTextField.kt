package com.argumentor.app.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.argumentor.app.R
import java.util.Locale

/**
 * OutlinedTextField with voice input capability.
 * Adds a microphone icon button that launches speech recognition.
 */
@Composable
fun VoiceInputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    locale: Locale = Locale("fr", "FR"), // Default to French
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                // Append to existing text if not empty, otherwise replace
                val newText = if (value.isNotEmpty()) {
                    "$value $spokenText"
                } else {
                    spokenText
                }
                onValueChange(newText)
            }
        }
    }

    val promptText = when (locale.language) {
        "fr" -> "Parlez maintenant..."
        "en" -> "Speak now..."
        else -> "Speak now..."
    }
    
    // Format: "fr-FR" or "en-US"
    val languageCode = "${locale.language}-${locale.country}"

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = {
            IconButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
                    }
                    voiceLauncher.launch(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.accessibility_voice_input)
                )
            }
        }
    )
}
