package com.argumentor.app.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
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
import com.argumentor.app.ui.navigation.ArguMentorNavigation
import com.argumentor.app.ui.theme.ArguMentorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
