package com.argumentor.app.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.argumentor.app.R
import java.util.Locale

/**
 * Helper class for Speech-to-Text functionality using Android SpeechRecognizer.
 *
 * This class manages a SpeechRecognizer instance and properly cleans it up to prevent memory leaks.
 * For Compose usage, prefer using rememberSpeechToTextHelper() which automatically handles lifecycle.
 *
 * @param context The application context (not Activity context to avoid leaks)
 * @param resourceProvider Provider for localized error messages
 * @param locale The locale to use for speech recognition (default: device locale)
 * @param onResult Callback when speech recognition succeeds
 * @param onError Callback when speech recognition fails
 */
class SpeechToTextHelper(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
    private val locale: Locale = Locale.getDefault(),
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    /**
     * Starts listening for speech input.
     * Returns false if speech recognition is not available or already listening.
     */
    fun startListening(): Boolean {
        if (isListening) {
            return false
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(resourceProvider.getString(R.string.error_speech_recognition_not_available))
            return false
        }

        try {
            // Clean up any existing recognizer first
            cleanup()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> resourceProvider.getString(R.string.error_speech_audio)
                            SpeechRecognizer.ERROR_CLIENT -> resourceProvider.getString(R.string.error_speech_client)
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> resourceProvider.getString(R.string.error_speech_permissions)
                            SpeechRecognizer.ERROR_NETWORK -> resourceProvider.getString(R.string.error_speech_network)
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> resourceProvider.getString(R.string.error_speech_network_timeout)
                            SpeechRecognizer.ERROR_NO_MATCH -> resourceProvider.getString(R.string.error_speech_no_match)
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> resourceProvider.getString(R.string.error_speech_recognizer_busy)
                            SpeechRecognizer.ERROR_SERVER -> resourceProvider.getString(R.string.error_speech_server)
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> resourceProvider.getString(R.string.error_speech_timeout)
                            else -> resourceProvider.getString(R.string.error_speech_unknown)
                        }
                        onError(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = createSpeechIntent(context, locale)
            speechRecognizer?.startListening(intent)
            return true
        } catch (e: Exception) {
            isListening = false
            onError(resourceProvider.getString(R.string.error_speech_unknown))
            return false
        }
    }

    /**
     * Stops listening and cleans up resources.
     */
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
        }
        cleanup()
    }

    /**
     * Internal cleanup method to properly release SpeechRecognizer resources.
     * This prevents memory leaks by ensuring the recognizer is always destroyed.
     */
    private fun cleanup() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore exceptions during cleanup
        } finally {
            speechRecognizer = null
            isListening = false
        }
    }
}

/**
 * Composable function that creates and manages a SpeechToTextHelper with automatic lifecycle handling.
 * This is the recommended way to use speech recognition in Compose, as it automatically cleans up
 * resources when the composable leaves the composition.
 *
 * @param resourceProvider Provider for localized strings
 * @param locale The locale to use for speech recognition (default: device locale)
 * @param onResult Callback when speech recognition succeeds
 * @param onError Callback when speech recognition fails
 * @return A SpeechToTextHelper instance that is automatically cleaned up
 */
@Composable
fun rememberSpeechToTextHelper(
    resourceProvider: ResourceProvider,
    locale: Locale = Locale.getDefault(),
    onResult: (String) -> Unit,
    onError: (String) -> Unit
): SpeechToTextHelper {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    val helper = remember(locale) {
        SpeechToTextHelper(
            context = context,
            resourceProvider = resourceProvider,
            locale = locale,
            onResult = onResult,
            onError = onError
        )
    }

    DisposableEffect(lifecycleOwner, helper) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                helper.stopListening()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            helper.stopListening()
        }
    }

    return helper
}

/**
 * Composable function to create a speech-to-text launcher using Intent.
 * This is a fallback method that uses the system's default speech recognition UI.
 * Unlike rememberSpeechToTextHelper, this delegates to the system UI rather than managing
 * the recognizer directly.
 *
 * @param onResult Callback when speech recognition succeeds with recognized text
 */
@Composable
fun rememberSpeechToTextLauncher(
    onResult: (String) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val text = matches?.firstOrNull() ?: ""
        if (text.isNotBlank()) {
            onResult(text)
        }
    }
}

/**
 * Create a speech recognition intent with specified locale.
 * Handles locales with or without country codes properly.
 *
 * INTERNATIONALIZATION: Now uses string resources for prompts instead of hardcoded text.
 *
 * @param context Context to access string resources
 * @param locale The locale to use for recognition (default: French)
 * @return Configured Intent for speech recognition
 */
fun createSpeechIntent(context: Context, locale: Locale = Locale.FRENCH): Intent {
    // Use resource strings for prompts instead of hardcoded text
    val promptText = when (locale.language) {
        "fr" -> context.getString(R.string.speech_prompt_fr)
        "en" -> context.getString(R.string.speech_prompt_en)
        "es" -> context.getString(R.string.speech_prompt_es)
        "de" -> context.getString(R.string.speech_prompt_de)
        "it" -> context.getString(R.string.speech_prompt_it)
        else -> context.getString(R.string.speech_prompt_default)
    }

    // Properly format language code - handle empty country
    val languageCode = if (locale.country.isNotEmpty()) {
        "${locale.language}-${locale.country}"
    } else {
        locale.language
    }

    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
    }
}
