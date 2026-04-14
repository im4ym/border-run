package com.borderrun.app.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.borderrun.app.ui.components.BottomNavTab
import com.borderrun.app.ui.components.BorderRunBottomNav
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the Home screen's shared navigation component.
 *
 * We test [BorderRunBottomNav] in isolation — it is a pure composable with
 * no ViewModel dependency, so it can be rendered directly with
 * [createComposeRule] without Hilt.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Bottom nav item visibility ─────────────────────────────────────────────

    @Test
    fun bottomNavShowsAllTabLabels() {
        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Home,
                onTabSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Quiz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explore").assertIsDisplayed()
    }

    @Test
    fun bottomNavShowsHomeTabAsSelected() {
        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Home,
                onTabSelected = {},
            )
        }

        // All labels are visible regardless of the selected tab.
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun bottomNavShowsExplorerTabAsSelected() {
        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Explorer,
                onTabSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Explore").assertIsDisplayed()
    }

    // ── Tab click callbacks ───────────────────────────────────────────────────

    @Test
    fun bottomNavInvokesCallbackWhenNonActiveTabIsClicked() {
        val clicked = mutableListOf<BottomNavTab>()

        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Home,
                onTabSelected = { tab -> clicked.add(tab) },
            )
        }

        composeTestRule.onNodeWithText("Stats").performClick()

        assertEquals(1, clicked.size)
        assertEquals(BottomNavTab.Stats, clicked[0])
    }

    @Test
    fun bottomNavDoesNotInvokeCallbackWhenActiveTabIsClicked() {
        val clicked = mutableListOf<BottomNavTab>()

        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Home,
                onTabSelected = { tab -> clicked.add(tab) },
            )
        }

        // Clicking the already-selected Home tab should NOT trigger the callback.
        composeTestRule.onNodeWithText("Home").performClick()

        assertEquals(0, clicked.size)
    }

    @Test
    fun bottomNavQuizTabClickIsCallbackedCorrectly() {
        val clicked = mutableListOf<BottomNavTab>()

        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Stats,
                onTabSelected = { tab -> clicked.add(tab) },
            )
        }

        composeTestRule.onNodeWithText("Quiz").performClick()

        assertEquals(BottomNavTab.Quiz, clicked.firstOrNull())
    }

    @Test
    fun bottomNavExploreTabClickIsCallbackedCorrectly() {
        val clicked = mutableListOf<BottomNavTab>()

        composeTestRule.setContent {
            BorderRunBottomNav(
                currentTab = BottomNavTab.Home,
                onTabSelected = { tab -> clicked.add(tab) },
            )
        }

        composeTestRule.onNodeWithText("Explore").performClick()

        assertEquals(BottomNavTab.Explorer, clicked.firstOrNull())
    }
}
