package com.borderrun.app.domain.usecase

import app.cash.turbine.test
import com.borderrun.app.domain.model.HomeStats
import com.borderrun.app.domain.repository.StatsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHomeStatsUseCaseTest {

    private val statsRepository: StatsRepository = mockk()
    private val useCase = GetHomeStatsUseCase(statsRepository)

    // ── Correct value mapping ─────────────────────────────────────────────────

    @Test
    fun `emits HomeStats with values from repository`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(5)
        every { statsRepository.getTotalAnswered() } returns flowOf(100)
        every { statsRepository.getOverallAccuracy() } returns flowOf(0.75f)
        every { statsRepository.getStreakHighScore() } returns flowOf(10)

        useCase().test {
            val stats = awaitItem()
            assertEquals(5, stats.streak)
            assertEquals(100, stats.totalAnswered)
            assertEquals(0.75f, stats.accuracy, 0.001f)
            assertEquals(10, stats.streakHighScore)
            awaitComplete()
        }
    }

    // ── Null handling ─────────────────────────────────────────────────────────

    @Test
    fun `maps null accuracy to zero`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(3)
        every { statsRepository.getTotalAnswered() } returns flowOf(50)
        every { statsRepository.getOverallAccuracy() } returns flowOf(null)
        every { statsRepository.getStreakHighScore() } returns flowOf(7)

        useCase().test {
            val stats = awaitItem()
            assertEquals(0f, stats.accuracy, 0.001f)
            awaitComplete()
        }
    }

    @Test
    fun `maps null streak high score to zero`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(2)
        every { statsRepository.getTotalAnswered() } returns flowOf(20)
        every { statsRepository.getOverallAccuracy() } returns flowOf(0.5f)
        every { statsRepository.getStreakHighScore() } returns flowOf(null)

        useCase().test {
            val stats = awaitItem()
            assertEquals(0, stats.streakHighScore)
            awaitComplete()
        }
    }

    // ── Empty data ────────────────────────────────────────────────────────────

    @Test
    fun `handles completely empty data gracefully`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(0)
        every { statsRepository.getTotalAnswered() } returns flowOf(0)
        every { statsRepository.getOverallAccuracy() } returns flowOf(null)
        every { statsRepository.getStreakHighScore() } returns flowOf(null)

        useCase().test {
            val stats = awaitItem()
            assertEquals(
                HomeStats(streak = 0, totalAnswered = 0, accuracy = 0f, streakHighScore = 0),
                stats,
            )
            awaitComplete()
        }
    }

    // ── Streak calculation ────────────────────────────────────────────────────

    @Test
    fun `streak is passed through from repository`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(14)
        every { statsRepository.getTotalAnswered() } returns flowOf(300)
        every { statsRepository.getOverallAccuracy() } returns flowOf(0.9f)
        every { statsRepository.getStreakHighScore() } returns flowOf(21)

        useCase().test {
            val stats = awaitItem()
            assertEquals(14, stats.streak)
            awaitComplete()
        }
    }

    // ── Accuracy passthrough ──────────────────────────────────────────────────

    @Test
    fun `accuracy is passed through unmodified when non-null`() = runTest {
        every { statsRepository.getStreak(any()) } returns flowOf(1)
        every { statsRepository.getTotalAnswered() } returns flowOf(42)
        every { statsRepository.getOverallAccuracy() } returns flowOf(0.632f)
        every { statsRepository.getStreakHighScore() } returns flowOf(5)

        useCase().test {
            val stats = awaitItem()
            assertEquals(0.632f, stats.accuracy, 0.001f)
            awaitComplete()
        }
    }
}
