package com.argumentor.app.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object EthicsWarning : Screen("ethics_warning")
    object Permissions : Screen("permissions")
    object Onboarding : Screen("onboarding")
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
    object SourceCreate : Screen("source/create")
    object SourceEdit : Screen("source/edit/{sourceId}") {
        fun createRoute(sourceId: String) = "source/edit/$sourceId"
    }
    object EvidenceCreate : Screen("evidence/create?claimId={claimId}") {
        fun createRoute(claimId: String) = "evidence/create?claimId=$claimId"
    }
    object EvidenceEdit : Screen("evidence/edit/{evidenceId}?claimId={claimId}") {
        fun createRoute(evidenceId: String, claimId: String) = "evidence/edit/$evidenceId?claimId=$claimId"
    }
    object FallacyCatalog : Screen("fallacy/catalog")
    object FallacyDetail : Screen("fallacy/{fallacyId}") {
        fun createRoute(fallacyId: String) = "fallacy/$fallacyId"
    }
}
