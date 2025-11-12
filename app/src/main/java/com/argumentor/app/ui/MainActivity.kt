package com.argumentor.app.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.argumentor.app.R
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.util.TutorialManager
import com.argumentor.app.ui.navigation.ArguMentorNavigation
import com.argumentor.app.ui.theme.ArguMentorTheme
import com.argumentor.app.util.AppConstants
import com.argumentor.app.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var languagePreferences: LanguagePreferences

    @Inject
    lateinit var tutorialManager: TutorialManager

    override fun attachBaseContext(newBase: Context) {
        try {
            // ISSUE-004 FIX: Added language code validation and error handling
            // ISSUE-005 FIX: Now uses AppConstants for consistent configuration
            val prefs = newBase.getSharedPreferences(
                AppConstants.Language.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val languageCode = prefs.getString(
                AppConstants.Language.PREF_KEY_LANGUAGE_CODE,
                AppConstants.Language.DEFAULT_LANGUAGE
            )

            // Validate language code against whitelist
            val validatedLanguageCode = AppConstants.Language.getValidatedLanguageCode(languageCode)

            // Log warning if invalid language code was detected
            if (languageCode != null && languageCode != validatedLanguageCode) {
                android.util.Log.w(
                    "MainActivity",
                    "Invalid language code: $languageCode, falling back to $validatedLanguageCode"
                )
            }

            val locale = when (validatedLanguageCode) {
                "en" -> java.util.Locale("en", "US")
                "fr" -> java.util.Locale("fr", "FR")
                else -> java.util.Locale("fr", "FR") // Fallback
            }

            val context = LocaleHelper.setLocale(newBase, locale)
            super.attachBaseContext(context)
        } catch (e: Exception) {
            // Fallback to default context on error
            android.util.Log.e("MainActivity", "Failed to set locale", e)
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for language changes and update demo topic if needed
        tutorialManager.checkAndHandleLanguageChange()

        // Show toast if demo topic was replaced
        if (tutorialManager.getDemoTopicReplaced()) {
            Toast.makeText(
                this,
                getString(R.string.demo_topic_translated),
                Toast.LENGTH_LONG
            ).show()
            tutorialManager.clearDemoTopicReplacedFlag()
        }

        // Enable modern edge-to-edge (content draws behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isImmersiveMode by viewModel.isImmersiveMode.collectAsState()

            // Apply immersive mode based on user preference
            LaunchedEffect(isImmersiveMode) {
                if (isImmersiveMode) {
                    enableImmersiveMode()
                } else {
                    disableImmersiveMode()
                }
            }

            ArguMentorTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArguMentorNavigation()
                }
            }
        }
    }

    private fun enableImmersiveMode() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            // Hide system bars (status bar and navigation bar)
            hide(WindowInsetsCompat.Type.systemBars())
            // Configure behavior when system bars are swiped in
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun disableImmersiveMode() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // No need to re-enable immersive mode here as it's handled by the state flow
    }
}
