package com.argumentor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.argumentor.app.ui.MainViewModel
import com.argumentor.app.ui.screens.claim.ClaimCreateEditScreen
import com.argumentor.app.ui.screens.debate.DebateModeScreen
import com.argumentor.app.ui.screens.ethics.EthicsWarningScreen
import com.argumentor.app.ui.screens.home.HomeScreen
import com.argumentor.app.ui.screens.importexport.ImportExportScreen
import com.argumentor.app.ui.screens.permissions.PermissionsScreen
import com.argumentor.app.ui.screens.settings.SettingsScreen
import com.argumentor.app.ui.screens.topic.TopicCreateEditScreen
import com.argumentor.app.ui.screens.topic.TopicDetailScreen

/**
 * Main navigation component for ArguMentor app.
 */
@Composable
fun ArguMentorNavigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val ethicsWarningShown by mainViewModel.ethicsWarningShown.collectAsState()

    val startDestination = if (ethicsWarningShown) {
        Screen.Home.route
    } else {
        Screen.EthicsWarning.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Ethics Warning (first launch)
        composable(Screen.EthicsWarning.route) {
            EthicsWarningScreen(
                onAccept = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.EthicsWarning.route) { inclusive = true }
                    }
                }
            )
        }

        // Permissions Screen
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
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
            TopicDetailScreen(
                topicId = topicId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.TopicEdit.createRoute(id))
                },
                onNavigateToDebate = { id ->
                    navController.navigate(Screen.DebateMode.createRoute(id))
                },
                onNavigateToAddClaim = { id ->
                    navController.navigate("claim/create?topicId=$id")
                }
            )
        }

        // Topic create
        composable(Screen.TopicCreate.route) {
            TopicCreateEditScreen(
                topicId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Topic edit
        composable(
            route = Screen.TopicEdit.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
            TopicCreateEditScreen(
                topicId = topicId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Claim create/edit
        composable(
            route = "claim/create?topicId={topicId}&claimId={claimId}",
            arguments = listOf(
                navArgument("topicId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("claimId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId")
            val claimId = backStackEntry.arguments?.getString("claimId")
            ClaimCreateEditScreen(
                claimId = claimId,
                topicId = topicId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Debate mode
        composable(
            route = Screen.DebateMode.route,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: return@composable
            DebateModeScreen(
                topicId = topicId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Import/Export
        composable(Screen.ImportExport.route) {
            ImportExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
