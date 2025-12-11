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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.argumentor.app.R
import java.util.Locale

/**
 * OutlinedTextField with voice input capability.
 * Adds a microphone icon button that launches speech recognition.
 *
 * PERFORMANCE: Uses rememberUpdatedState to stabilize callbacks and prevent unnecessary recompositions.
 * BUGFIX: Properly handles locales with or without country codes to prevent empty language codes.
 * UX: Optional character counter with maxLength support.
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
    maxLength: Int? = null,
    locale: Locale = Locale("fr", "FR"), // Default to French
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    // Stabilize the callback to prevent recompositions when the lambda changes
    val currentOnValueChange = rememberUpdatedState(onValueChange)
    val currentValue = rememberUpdatedState(value)

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                // Append to existing text if not empty, otherwise replace
                val newText = if (currentValue.value.isNotEmpty()) {
                    "${currentValue.value} $spokenText"
                } else {
                    spokenText
                }
                // Apply maxLength limit to voice input as well
                val limitedText = if (maxLength != null && newText.length > maxLength) {
                    newText.take(maxLength)
                } else {
                    newText
                }
                currentOnValueChange.value(limitedText)
            }
        }
    }

    val promptText = androidx.compose.ui.platform.LocalContext.current.getString(
        when (locale.language) {
            "fr" -> R.string.speech_prompt_french
            "en" -> R.string.speech_prompt_english
            "es" -> R.string.speech_prompt_spanish
            "de" -> R.string.speech_prompt_german
            "it" -> R.string.speech_prompt_italian
            else -> R.string.speech_prompt_default
        }
    )

    // BUGFIX: Properly format language code - handle empty country
    // Format: "fr-FR" or "en-US", or just "fr" if no country
    val languageCode = if (locale.country.isNotEmpty()) {
        "${locale.language}-${locale.country}"
    } else {
        locale.language
    }

    // Determine the supporting text: custom, character counter, or both
    val effectiveSupportingText: @Composable (() -> Unit)? = when {
        supportingText != null -> supportingText
        maxLength != null -> {
            {
                val isOverLimit = value.length > maxLength
                Text(
                    text = "${value.length}/$maxLength",
                    color = if (isOverLimit) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> null
    }

    // Enforce maxLength if set
    val effectiveOnValueChange: (String) -> Unit = if (maxLength != null) {
        { newValue -> if (newValue.length <= maxLength) onValueChange(newValue) }
    } else {
        onValueChange
    }

    OutlinedTextField(
        value = value,
        onValueChange = effectiveOnValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError || (maxLength != null && value.length > maxLength),
        supportingText = effectiveSupportingText,
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
