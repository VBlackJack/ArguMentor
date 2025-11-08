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
import java.util.Locale

/**
 * Helper class for Speech-to-Text functionality using Android SpeechRecognizer.
 */
class SpeechToTextHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("La reconnaissance vocale n'est pas disponible")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Erreur audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Erreur client"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions insuffisantes"
                        SpeechRecognizer.ERROR_NETWORK -> "Erreur réseau"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout réseau"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Aucune correspondance"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconnaissance occupée"
                        SpeechRecognizer.ERROR_SERVER -> "Erreur serveur"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout de parole"
                        else -> "Erreur inconnue"
                    }
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    onResult(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.toString())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

/**
 * Composable function to create a speech-to-text launcher using Intent.
 * This is a fallback method that uses the system's default speech recognition UI.
 */
@Composable
fun rememberSpeechToTextLauncher(
    onResult: (String) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val context = LocalContext.current

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
 * Create a speech recognition intent.
 */
fun createSpeechIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.toString())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
    }
}
