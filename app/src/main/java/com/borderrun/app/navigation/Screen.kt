package com.borderrun.app.navigation

/** Route string for the Home screen. */
private const val ROUTE_HOME = "home"

/** Route string for the Statistics screen. */
private const val ROUTE_STATS = "stats"

/** Route string for the Settings screen. */
private const val ROUTE_SETTINGS = "settings"

/** Route string for the Permission Rationale screen. */
private const val ROUTE_PERMISSION_RATIONALE = "permission-rationale"

/** Route string for the Explorer screen. */
private const val ROUTE_EXPLORER = "explorer"

/** Route string for the Daily Mystery screen. */
private const val ROUTE_MYSTERY = "mystery"

/** Route template for the Weakness Trainer screen (requires region argument). */
private const val ROUTE_WEAKNESS_TRAINER_TEMPLATE = "weakness/{region}"

/** Route template for the Quiz screen (requires region and difficulty arguments). */
private const val ROUTE_QUIZ_TEMPLATE = "quiz/{region}/{difficulty}"

/** Route template for the Quiz Result screen (requires sessionId argument). */
private const val ROUTE_QUIZ_RESULT_TEMPLATE = "quiz-result/{sessionId}"

/**
 * Sealed class enumerating every destination in the Border Run navigation graph.
 *
 * Each object or class exposes a [route] string used by [NavHost] and a
 * [createRoute] factory (where arguments are needed) to build type-safe
 * navigation calls.
 *
 * Navigation graph: home → quiz/{region}/{difficulty} → quiz-result/{sessionId}
 *                        → stats
 *                        → settings → permission-rationale
 *                        → explorer
 */
sealed class Screen(val route: String) {

    /** Landing / Home screen — streak, daily challenge card, region grid. */
    data object Home : Screen(ROUTE_HOME)

    /**
     * Quiz screen — question cards and answer options.
     *
     * @property route The NavHost route template (contains `{region}` and `{difficulty}`).
     */
    data object Quiz : Screen(ROUTE_QUIZ_TEMPLATE) {
        /** Argument key for the region parameter in the route. */
        const val ARG_REGION = "region"

        /** Argument key for the difficulty parameter in the route. */
        const val ARG_DIFFICULTY = "difficulty"

        /**
         * Creates the concrete navigation route for a quiz with the given parameters.
         *
         * @param region The region to quiz on, e.g. `"Asia"`. Use `"mixed"` for daily/streak.
         * @param difficulty One of `"easy"`, `"medium"`, `"hard"`.
         * @return Route string with arguments substituted.
         */
        fun createRoute(region: String, difficulty: String): String =
            "quiz/$region/$difficulty"
    }

    /**
     * Quiz Result screen — score ring, per-question review.
     *
     * @property route The NavHost route template (contains `{sessionId}`).
     */
    data object QuizResult : Screen(ROUTE_QUIZ_RESULT_TEMPLATE) {
        /** Argument key for the session ID parameter in the route. */
        const val ARG_SESSION_ID = "sessionId"

        /**
         * Creates the concrete navigation route for a specific quiz result.
         *
         * @param sessionId The [QuizSessionEntity] primary key of the completed session.
         * @return Route string with argument substituted.
         */
        fun createRoute(sessionId: Int): String =
            "quiz-result/$sessionId"
    }

    /** Statistics screen — accuracy by region, activity calendar, streak. */
    data object Stats : Screen(ROUTE_STATS)

    /** Settings screen — preferences, privacy dashboard, clear data. */
    data object Settings : Screen(ROUTE_SETTINGS)

    /**
     * Permission Rationale screen — explains Local Discovery before the
     * system location prompt is shown.
     */
    data object PermissionRationale : Screen(ROUTE_PERMISSION_RATIONALE)

    /** Explorer screen — browse and search all countries. */
    data object Explorer : Screen(ROUTE_EXPLORER)

    /** Daily Mystery Country screen — guess today's mystery country from clues. */
    data object Mystery : Screen(ROUTE_MYSTERY)

    /**
     * Smart Weakness Trainer screen — focused quiz on the user's weakest region.
     *
     * @property route The NavHost route template (contains `{region}`).
     */
    data object WeaknessTrainer : Screen(ROUTE_WEAKNESS_TRAINER_TEMPLATE) {
        /** Argument key for the region name in the route. */
        const val ARG_REGION = "region"

        /**
         * Creates the concrete navigation route for a weakness training session.
         *
         * @param region The weak region to drill, e.g. `"Asia"`.
         * @return Route string with argument substituted.
         */
        fun createRoute(region: String): String = "weakness/$region"
    }
}
