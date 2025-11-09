package com.argumentor.app.ui

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
}
