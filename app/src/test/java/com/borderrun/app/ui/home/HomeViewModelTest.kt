package com.borderrun.app.ui.home

import app.cash.turbine.test
import com.borderrun.app.domain.model.DailyContent
import com.borderrun.app.domain.model.HomeStats
import com.borderrun.app.domain.usecase.GetDailyContentUseCase
import com.borderrun.app.domain.usecase.GetHomeStatsUseCase
import com.borderrun.app.domain.usecase.GetRegionCountsUseCase
import com.borderrun.app.domain.usecase.GetWeaknessDataUseCase
import com.borderrun.app.domain.usecase.GetWeeklyActivityUseCase
import com.borderrun.app.domain.usecase.SyncCountriesUseCase
import com.borderrun.app.domain.usecase.SyncResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── Mocks ─────────────────────────────────────────────────────────────────

    private val getHomeStatsUseCase: GetHomeStatsUseCase = mockk()
    private val getDailyContentUseCase: GetDailyContentUseCase = mockk()
    private val getWeaknessDataUseCase: GetWeaknessDataUseCase = mockk()
    private val getWeeklyActivityUseCase: GetWeeklyActivityUseCase = mockk()
    private val getRegionCountsUseCase: GetRegionCountsUseCase = mockk()
    private val syncCountriesUseCase: SyncCountriesUseCase = mockk()

    // ── Convenience builder ───────────────────────────────────────────────────

    /**
     * Configures all use-case mocks with sensible defaults and returns a new
     * [HomeViewModel].
     *
     * @param regionCounts Returned by [getRegionCountsUseCase]. A non-empty map
     *   tells the ViewModel that cached data exists, flipping [HomeUiState.isLoading]
     *   to `false`.
     * @param syncResult Returned by [syncCountriesUseCase].
     */
    private fun buildViewModel(
        regionCounts: Map<String, Int> = mapOf("Asia" to 48, "Europe" to 44),
        syncResult: SyncResult = SyncResult.Skipped,
    ): HomeViewModel {
        every { getHomeStatsUseCase.invoke() } returns flowOf(HomeStats())
        every { getDailyContentUseCase.invoke(any()) } returns
            flowOf(DailyContent(challenge = null, mystery = null))
        every { getWeaknessDataUseCase.invoke() } returns flowOf(null)
        every { getWeeklyActivityUseCase.invoke(any()) } returns flowOf(emptyMap())
        every { getRegionCountsUseCase.invoke() } returns flowOf(regionCounts)
        coEvery { syncCountriesUseCase.invoke() } returns syncResult

        return HomeViewModel(
            getHomeStatsUseCase = getHomeStatsUseCase,
            getDailyContentUseCase = getDailyContentUseCase,
            getWeaknessDataUseCase = getWeaknessDataUseCase,
            getWeeklyActivityUseCase = getWeeklyActivityUseCase,
            getRegionCountsUseCase = getRegionCountsUseCase,
            syncCountriesUseCase = syncCountriesUseCase,
        )
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() {
        // stateIn always seeds with its initialValue before the upstream fires.
        val viewModel = buildViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    // ── State after data loads ────────────────────────────────────────────────

    @Test
    fun `isLoading becomes false after flows emit with cached data`() = runTest {
        val viewModel = buildViewModel(
            regionCounts = mapOf("Asia" to 48),
            syncResult = SyncResult.Skipped,
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse("Expected isLoading = false after data loaded", state.isLoading)
        }
    }

    @Test
    fun `region counts are passed through from use case`() = runTest {
        val regionCounts = mapOf("Asia" to 48, "Europe" to 44, "Africa" to 54)
        val viewModel = buildViewModel(regionCounts = regionCounts)

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(regionCounts, state.regionCounts)
        }
    }

    // ── Sync error handling ───────────────────────────────────────────────────

    @Test
    fun `syncError is null when sync succeeds`() = runTest {
        val viewModel = buildViewModel(syncResult = SyncResult.Success)

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.syncError)
        }
    }

    @Test
    fun `syncError is null when sync is skipped`() = runTest {
        val viewModel = buildViewModel(syncResult = SyncResult.Skipped)

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(state.syncError)
        }
    }

    @Test
    fun `syncError is set when sync fails and no cached data exists`() = runTest {
        val viewModel = buildViewModel(
            regionCounts = emptyMap(), // no cached data
            syncResult = SyncResult.Failed(cacheExists = false),
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(
                "Expected syncError to be non-null when sync failed with no cache",
                state.syncError != null,
            )
        }
    }

    @Test
    fun `syncError is null when sync fails but stale cache exists`() = runTest {
        val viewModel = buildViewModel(
            regionCounts = mapOf("Asia" to 48), // stale cache present
            syncResult = SyncResult.Failed(cacheExists = true),
        )

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNull(
                "Expected syncError null — stale cache covers the sync failure",
                state.syncError,
            )
        }
    }

    // ── Greeting helper ───────────────────────────────────────────────────────

    @Test
    fun `computeGreeting returns a valid time-of-day string`() {
        // computeGreeting() is internal — directly accessible from test source set.
        // We verify it returns one of the three valid strings without depending on
        // the current wall-clock hour.
        val greeting = computeGreeting()
        assertTrue(
            "Unexpected greeting: '$greeting'",
            greeting in listOf("Good Morning", "Good Afternoon", "Good Evening"),
        )
    }
}
