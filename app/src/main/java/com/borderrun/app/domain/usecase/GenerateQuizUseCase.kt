package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.QuestionGenerator
import com.borderrun.app.domain.model.QuizQuestion
import com.borderrun.app.domain.repository.CountryRepository
import com.borderrun.app.domain.repository.QuizRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Result returned by [GenerateQuizUseCase].
 */
sealed class GenerateQuizResult {
    /**
     * Questions were generated and a session row was created.
     *
     * @property sessionId Auto-generated session primary key from Room.
     * @property questions The list of questions ready for presentation.
     */
    data class Success(val sessionId: Int, val questions: List<QuizQuestion>) : GenerateQuizResult()

    /**
     * Generation failed — not enough data or an unexpected exception.
     *
     * @property message Human-readable description of the failure.
     */
    data class Error(val message: String) : GenerateQuizResult()
}

/**
 * Loads countries from Room, generates quiz questions via [QuestionGenerator],
 * and creates a placeholder session row in [QuizRepository].
 *
 * Returns [GenerateQuizResult.Error] when the cache is empty (countries not yet
 * synced) or the filtered pool is too small to form four-option questions.
 *
 * @property countryRepository Provides the cached country list.
 * @property quizRepository Creates the session row.
 * @property questionGenerator Pure Kotlin question factory.
 */
class GenerateQuizUseCase @Inject constructor(
    private val countryRepository: CountryRepository,
    private val quizRepository: QuizRepository,
    private val questionGenerator: QuestionGenerator,
) {

    /**
     * Executes the use case.
     *
     * @param region Region to quiz on. `"mixed"` or `"daily"` uses all countries.
     * @param difficulty One of `"easy"`, `"medium"`, `"hard"`.
     * @return [GenerateQuizResult.Success] or [GenerateQuizResult.Error].
     */
    suspend operator fun invoke(region: String, difficulty: String): GenerateQuizResult {
        return try {
            val countries = countryRepository.getAllCountries().first()
            if (countries.size < QuestionGenerator.MIN_COUNTRIES_FOR_QUIZ) {
                return GenerateQuizResult.Error(
                    "Not enough country data. Please check your internet connection.",
                )
            }
            val questions = questionGenerator.generate(countries, region, difficulty)
            if (questions.isEmpty()) {
                return GenerateQuizResult.Error(
                    "Could not generate questions. Try a different region.",
                )
            }
            val sessionId = quizRepository.startSession(
                region = if (region == REGION_MIXED || region == REGION_DAILY) null else region,
                difficulty = difficulty,
                gameMode = GAME_MODE_CLASSIC,
            )
            GenerateQuizResult.Success(sessionId = sessionId, questions = questions)
        } catch (e: Exception) {
            GenerateQuizResult.Error("Failed to load quiz: ${e.message}")
        }
    }

    companion object {
        private const val REGION_MIXED = "mixed"
        private const val REGION_DAILY = "daily"
        private const val GAME_MODE_CLASSIC = "classic"
    }
}
