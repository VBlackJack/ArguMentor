package com.argumentor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.argumentor.app.ui.screens.ethics.EthicsWarningScreen
import com.argumentor.app.ui.screens.home.HomeScreen

/**
 * Main navigation component for ArguMentor app.
 */
@Composable
fun ArguMentorNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.EthicsWarning.route
    ) {
        // Ethics Warning (first launch)
        composable(Screen.EthicsWarning.route) {
            EthicsWarningScreen(
                onAccept = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.EthicsWarning.route) { inclusive = true }
                    }
                }
            )
        }

        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTopic = { topicId ->
                    navController.navigate(Screen.TopicDetail.createRoute(topicId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.TopicCreate.route)
                },
                onNavigateToImportExport = {
                    navController.navigate(Screen.ImportExport.route)
                }
            )
        }

        // Topic detail
        composable(
            route = Screen.TopicDetail.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
            // TopicDetailScreen implementation would go here
        }

        // Additional screens will be added as needed
    }
}
