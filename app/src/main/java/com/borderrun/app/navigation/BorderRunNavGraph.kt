package com.borderrun.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Root navigation graph for Border Run.
 *
 * Defines all [NavHost] destinations using the routes declared in [Screen].
 * Each composable destination is a placeholder for now; the real screen
 * composables will replace them in subsequent implementation milestones.
 *
 * The graph starts at [Screen.Home]. Navigation into the quiz flow follows
 * the path: Home → Quiz → QuizResult → (back to Home or retry).
 *
 * A deep link from notification taps uses the route:
 * `borderrun://quiz/daily/medium`
 *
 * @param navController The [NavHostController] used to drive navigation.
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
            PlaceholderScreen(label = "Home")
        }

        // ── Quiz ──────────────────────────────────────────────────────────────
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument(Screen.Quiz.ARG_REGION) { type = NavType.StringType },
                navArgument(Screen.Quiz.ARG_DIFFICULTY) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val region = backStackEntry.arguments?.getString(Screen.Quiz.ARG_REGION) ?: ""
            val difficulty = backStackEntry.arguments?.getString(Screen.Quiz.ARG_DIFFICULTY) ?: ""
            PlaceholderScreen(label = "Quiz — $region / $difficulty")
        }

        // ── Quiz Result ───────────────────────────────────────────────────────
        composable(
            route = Screen.QuizResult.route,
            arguments = listOf(
                navArgument(Screen.QuizResult.ARG_SESSION_ID) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getInt(Screen.QuizResult.ARG_SESSION_ID) ?: 0
            PlaceholderScreen(label = "Quiz Result — session $sessionId")
        }

        // ── Stats ─────────────────────────────────────────────────────────────
        composable(route = Screen.Stats.route) {
            PlaceholderScreen(label = "Statistics")
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(route = Screen.Settings.route) {
            PlaceholderScreen(label = "Settings")
        }

        // ── Permission Rationale ──────────────────────────────────────────────
        composable(route = Screen.PermissionRationale.route) {
            PlaceholderScreen(label = "Permission Rationale")
        }

        // ── Explorer ──────────────────────────────────────────────────────────
        composable(route = Screen.Explorer.route) {
            PlaceholderScreen(label = "Explorer")
        }
    }
}

/**
 * Temporary placeholder composable used for screens that have not yet been
 * implemented. Displays the screen [label] centred on a blank surface.
 *
 * @param label Human-readable name of the destination, shown as placeholder text.
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
