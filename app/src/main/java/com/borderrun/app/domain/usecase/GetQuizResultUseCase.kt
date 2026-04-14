package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.model.QuizResult
import com.borderrun.app.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Loads the aggregate [QuizResult] for a completed session from Room.
 *
 * Delegates entirely to [QuizRepository.getResult]; exists as a use case to
 * keep [com.borderrun.app.ui.result.QuizResultViewModel] decoupled from the
 * data layer.
 *
 * @property quizRepository Reads session and answer rows from Room.
 */
class GetQuizResultUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    /**
     * Returns the [QuizResult] for [sessionId], or `null` if not found.
     *
     * @param sessionId Session primary key passed via navigation argument.
     */
    suspend operator fun invoke(sessionId: Int): QuizResult? =
        quizRepository.getResult(sessionId)
}
