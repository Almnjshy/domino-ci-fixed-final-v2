package com.agon.app.domain.usecase.game

import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case for checking if game is over
 */
class CheckGameOverUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Boolean {
        return gameRepository.isGameOver()
    }

    fun getWinner(): Int? {
        return gameRepository.getWinner()
    }

    fun getScores(): Map<Int, Int> {
        return gameRepository.getScores()
    }
}
