package com.argumentor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.argumentor.app.ui.screens.claim.ClaimCreateEditScreen
import com.argumentor.app.ui.screens.debate.DebateModeScreen
import com.argumentor.app.ui.screens.evidence.EvidenceCreateEditScreen
import com.argumentor.app.ui.screens.home.HomeScreen
import com.argumentor.app.ui.screens.importexport.ImportExportScreen
import com.argumentor.app.ui.screens.onboarding.OnboardingScreen
import com.argumentor.app.ui.screens.permissions.PermissionsScreen
import com.argumentor.app.ui.screens.question.QuestionCreateEditScreen
import com.argumentor.app.ui.screens.settings.SettingsScreen
import com.argumentor.app.ui.screens.source.SourceCreateEditScreen
import com.argumentor.app.ui.screens.statistics.StatisticsScreen
import com.argumentor.app.ui.screens.topic.TopicCreateEditScreen
import com.argumentor.app.ui.screens.topic.TopicDetailScreen
import com.argumentor.app.ui.screens.fallacy.FallacyCatalogScreen
import com.argumentor.app.ui.screens.fallacy.FallacyDetailScreen

/**
 * Main navigation component for ArguMentor app.
 */
@Composable
fun ArguMentorNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Permissions.route  // Start directly at permissions
    ) {
        // Permissions Screen
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
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
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFallacyCatalog = {
                    navController.navigate(Screen.FallacyCatalog.route)
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
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.TopicEdit.createRoute(id))
                },
                onNavigateToDebate = { id ->
                    navController.navigate(Screen.DebateMode.createRoute(id))
                },
                onNavigateToAddClaim = { tId, cId ->
                    val route = if (cId != null) {
                        "claim/create?topicId=$tId&claimId=$cId"
                    } else {
                        "claim/create?topicId=$tId"
                    }
                    navController.navigate(route)
                },
                onNavigateToAddQuestion = { tId, qId ->
                    val route = if (qId != null) {
                        "question/create?targetId=$tId&questionId=$qId"
                    } else {
                        "question/create?targetId=$tId"
                    }
                    navController.navigate(route)
                },
                onNavigateToAddSource = { sourceId ->
                    if (sourceId != null) {
                        navController.navigate(Screen.SourceEdit.createRoute(sourceId))
                    } else {
                        navController.navigate(Screen.SourceCreate.route)
                    }
                },
                onNavigateToAddEvidence = { claimId ->
                    navController.navigate(Screen.EvidenceCreate.createRoute(claimId))
                },
                onNavigateToEditEvidence = { evidenceId, claimId ->
                    navController.navigate(Screen.EvidenceEdit.createRoute(evidenceId, claimId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.TopicCreate.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToImportExport = {
                    navController.navigate(Screen.ImportExport.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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

        // Question create/edit
        composable(
            route = "question/create?targetId={targetId}&questionId={questionId}",
            arguments = listOf(
                navArgument("targetId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("questionId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val targetId = backStackEntry.arguments?.getString("targetId")
            val questionId = backStackEntry.arguments?.getString("questionId")
            QuestionCreateEditScreen(
                questionId = questionId,
                targetId = targetId,
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

        // Statistics
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Source create
        composable(Screen.SourceCreate.route) {
            SourceCreateEditScreen(
                sourceId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Source edit
        composable(
            route = Screen.SourceEdit.route,
            arguments = listOf(navArgument("sourceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: return@composable
            SourceCreateEditScreen(
                sourceId = sourceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Evidence create
        composable(
            route = Screen.EvidenceCreate.route,
            arguments = listOf(
                navArgument("claimId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val claimId = backStackEntry.arguments?.getString("claimId") ?: return@composable
            EvidenceCreateEditScreen(
                evidenceId = null,
                claimId = claimId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateSource = {
                    navController.navigate(Screen.SourceCreate.route)
                }
            )
        }

        // Evidence edit
        composable(
            route = Screen.EvidenceEdit.route,
            arguments = listOf(
                navArgument("evidenceId") { type = NavType.StringType },
                navArgument("claimId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val evidenceId = backStackEntry.arguments?.getString("evidenceId") ?: return@composable
            val claimId = backStackEntry.arguments?.getString("claimId") ?: return@composable
            EvidenceCreateEditScreen(
                evidenceId = evidenceId,
                claimId = claimId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateSource = {
                    navController.navigate(Screen.SourceCreate.route)
                }
            )
        }

        // Fallacy catalog
        composable(Screen.FallacyCatalog.route) {
            FallacyCatalogScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { fallacyId ->
                    navController.navigate(Screen.FallacyDetail.createRoute(fallacyId))
                }
            )
        }

        // Fallacy detail
        composable(
            route = Screen.FallacyDetail.route,
            arguments = listOf(navArgument("fallacyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fallacyId = backStackEntry.arguments?.getString("fallacyId") ?: return@composable
            FallacyDetailScreen(
                fallacyId = fallacyId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
