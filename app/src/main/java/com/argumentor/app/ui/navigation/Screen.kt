package com.argumentor.app.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object EthicsWarning : Screen("ethics_warning")
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object TopicDetail : Screen("topic/{topicId}") {
        fun createRoute(topicId: String) = "topic/$topicId"
    }
    object TopicEdit : Screen("topic/edit/{topicId}") {
        fun createRoute(topicId: String) = "topic/edit/$topicId"
    }
    object TopicCreate : Screen("topic/create")
    object DebateMode : Screen("debate/{topicId}") {
        fun createRoute(topicId: String) = "debate/$topicId"
    }
    object ImportExport : Screen("import_export")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}
