package com.argumentor.app.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.util.TutorialManager
import com.argumentor.app.ui.navigation.ArguMentorNavigation
import com.argumentor.app.ui.theme.ArguMentorTheme
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
        // Read language from a simple preferences file
        // We use a separate SharedPreferences file for quick access
        val prefs = newBase.getSharedPreferences("app_language_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "fr") ?: "fr"
        
        val locale = when (languageCode) {
            "en" -> java.util.Locale("en", "US")
            "fr" -> java.util.Locale("fr", "FR")
            else -> java.util.Locale("fr", "FR")
        }
        
        val context = LocaleHelper.setLocale(newBase, locale)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for language changes and update demo topic if needed
        tutorialManager.checkAndHandleLanguageChange()

        // Enable modern edge-to-edge (content draws behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Enable immersive mode (hide system bars)
        enableImmersiveMode()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-enable immersive mode when window regains focus
            enableImmersiveMode()
        }
    }
}
