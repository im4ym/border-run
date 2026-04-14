package com.borderrun.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.borderrun.app.ui.explorer.ExplorerScreen
import com.borderrun.app.ui.home.HomeScreen
import com.borderrun.app.ui.mystery.MysteryCountryScreen
import com.borderrun.app.ui.permission.PermissionRationaleScreen
import com.borderrun.app.ui.quiz.QuizScreen
import com.borderrun.app.ui.result.QuizResultScreen
import com.borderrun.app.ui.settings.SettingsScreen
import com.borderrun.app.ui.stats.StatsScreen
import com.borderrun.app.ui.weakness.WeaknessTrainerScreen

// ── SavedStateHandle keys — Settings ↔ PermissionRationale ───────────────────

/** Key set on Settings back-stack entry after POST_NOTIFICATIONS is granted. */
private const val KEY_NOTIFICATIONS_GRANTED = "notifications_granted"

/** Key set on Settings back-stack entry after POST_NOTIFICATIONS is denied. */
private const val KEY_NOTIFICATIONS_DENIED = "notifications_denied"

// ── Shared navigation helpers ─────────────────────────────────────────────────

/** Navigates to Home, clearing everything above it in the back stack. */
private fun NavHostController.toHome() =
    navigate(Screen.Home.route) { launchSingleTop = true }

/** Navigates to Stats as a top-level tab (single top). */
private fun NavHostController.toStats() =
    navigate(Screen.Stats.route) { launchSingleTop = true }

/** Navigates to Settings as a top-level tab (single top). */
private fun NavHostController.toSettings() =
    navigate(Screen.Settings.route) { launchSingleTop = true }

/** Navigates to Explorer as a top-level tab (single top). */
private fun NavHostController.toExplorer() =
    navigate(Screen.Explorer.route) { launchSingleTop = true }

/** Navigates to a mixed medium quiz. */
private fun NavHostController.toMixedQuiz() =
    navigate(Screen.Quiz.createRoute("mixed", "medium"))

/**
 * Root navigation graph for Border Run.
 *
 * Defines all [NavHost] destinations and the transition rules between them.
 *
 * Navigation flow:
 * ```
 * Home ──► Quiz/{region}/{difficulty} ──► QuizResult/{sessionId}
 *      ├──► Stats
 *      ├──► Settings ──► PermissionRationale
 *      ├──► Explorer
 *      ├──► Mystery
 *      └──► WeaknessTrainer/{region} ──► QuizResult/{sessionId}
 * ```
 *
 * **Permission result passing (Settings ↔ PermissionRationale):**
 * [PermissionRationaleScreen] writes [KEY_NOTIFICATIONS_GRANTED] or
 * [KEY_NOTIFICATIONS_DENIED] onto the Settings [NavBackStackEntry]
 * `savedStateHandle`, then pops. [SettingsScreen] observes both keys via
 * `getStateFlow` and acts in `LaunchedEffect`.
 *
 * @param navController [NavHostController] used to drive navigation.
 *   Defaults to a freshly remembered controller.
 */
@Composable
fun BorderRunNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            HomeScreen(
                onRegionClick = { region ->
                    navController.navigate(Screen.Quiz.createRoute(region, "medium"))
                },
                onDailyChallengeClick = {
                    navController.navigate(Screen.Quiz.createRoute("daily", "medium"))
                },
                onMysteryClick = {
                    navController.navigate(Screen.Mystery.route) { launchSingleTop = true }
                },
                onWeaknessClick = { region ->
                    navController.navigate(Screen.WeaknessTrainer.createRoute(region))
                },
                onQuizClick = { navController.toMixedQuiz() },
                onStatsClick = { navController.toStats() },
                onSettingsClick = { navController.toSettings() },
                onExplorerClick = { navController.toExplorer() },
            )
        }

        // ── Quiz ──────────────────────────────────────────────────────────────
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument(Screen.Quiz.ARG_REGION) { type = NavType.StringType },
                navArgument(Screen.Quiz.ARG_DIFFICULTY) { type = NavType.StringType },
            ),
        ) {
            QuizScreen(
                onNavigateToResult = { sessionId ->
                    navController.navigate(Screen.QuizResult.createRoute(sessionId)) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Quiz Result ───────────────────────────────────────────────────────
        composable(
            route = Screen.QuizResult.route,
            arguments = listOf(
                navArgument(Screen.QuizResult.ARG_SESSION_ID) { type = NavType.IntType },
            ),
        ) {
            QuizResultScreen(
                onPlayAgain = { region, difficulty ->
                    navController.navigate(Screen.Quiz.createRoute(region, difficulty)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Stats ─────────────────────────────────────────────────────────────
        composable(route = Screen.Stats.route) {
            StatsScreen(
                onHome = { navController.toHome() },
                onQuizClick = { navController.toMixedQuiz() },
                onSettings = { navController.toSettings() },
                onExplorer = { navController.toExplorer() },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(route = Screen.Settings.route) { backStackEntry ->
            val notificationsGranted by backStackEntry.savedStateHandle
                .getStateFlow(KEY_NOTIFICATIONS_GRANTED, false)
                .collectAsState()
            val notificationsDenied by backStackEntry.savedStateHandle
                .getStateFlow(KEY_NOTIFICATIONS_DENIED, false)
                .collectAsState()

            SettingsScreen(
                onHome = { navController.toHome() },
                onQuizClick = { navController.toMixedQuiz() },
                onStats = { navController.toStats() },
                onExplorer = { navController.toExplorer() },
                onNavigateToPermissionRationale = {
                    navController.navigate(Screen.PermissionRationale.route)
                },
                notificationsJustGranted = notificationsGranted,
                notificationsJustDenied = notificationsDenied,
                onConsumeNotificationsGranted = {
                    backStackEntry.savedStateHandle[KEY_NOTIFICATIONS_GRANTED] = false
                },
                onConsumeNotificationsDenied = {
                    backStackEntry.savedStateHandle[KEY_NOTIFICATIONS_DENIED] = false
                },
            )
        }

        // ── Permission Rationale ──────────────────────────────────────────────
        composable(route = Screen.PermissionRationale.route) {
            val previousEntry = navController.previousBackStackEntry
            PermissionRationaleScreen(
                onPermissionGranted = {
                    previousEntry?.savedStateHandle?.set(KEY_NOTIFICATIONS_GRANTED, true)
                    navController.popBackStack()
                },
                onPermissionDenied = {
                    previousEntry?.savedStateHandle?.set(KEY_NOTIFICATIONS_DENIED, true)
                    navController.popBackStack()
                },
            )
        }

        // ── Explorer ──────────────────────────────────────────────────────────
        composable(route = Screen.Explorer.route) {
            ExplorerScreen(
                onHome = { navController.toHome() },
                onQuizClick = { navController.toMixedQuiz() },
                onStats = { navController.toStats() },
                onSettings = { navController.toSettings() },
            )
        }

        // ── Mystery Country ───────────────────────────────────────────────────
        composable(route = Screen.Mystery.route) {
            MysteryCountryScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Weakness Trainer ──────────────────────────────────────────────────
        composable(
            route = Screen.WeaknessTrainer.route,
            arguments = listOf(
                navArgument(Screen.WeaknessTrainer.ARG_REGION) { type = NavType.StringType },
            ),
        ) {
            WeaknessTrainerScreen(
                onNavigateToResult = { sessionId ->
                    navController.navigate(Screen.QuizResult.createRoute(sessionId)) {
                        popUpTo(Screen.WeaknessTrainer.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

/**
 * Temporary placeholder composable used for screens that have not yet been
 * implemented.
 */
@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label)
    }
}
