package com.agon.app.domain.usecase.ai

import com.agon.app.domain.engine.ai.DominoAIEngine
import com.agon.app.domain.model.AiDifficulty
import com.agon.app.domain.model.GameState
import com.agon.app.domain.model.Player
import kotlinx.coroutines.delay
import javax.inject.Inject

class AIPlayUseCase @Inject constructor(
    private val aiEngine: DominoAIEngine
) {
    /** Returns best move or null (draw/pass). Pure — reads state only. */
    suspend operator fun invoke(
        state: GameState,
        player: Player,
        difficulty: AiDifficulty
    ): DominoAIEngine.AIMove? {
        // Human-like thinking delay
        val delayMs = when (difficulty) {
            AiDifficulty.EASY -> (400L..800L).random()
            AiDifficulty.MEDIUM -> (600L..1200L).random()
            AiDifficulty.HARD -> (900L..1800L).random()
        }
        delay(delayMs)
        return aiEngine.calculateBestMove(state, player, difficulty)
    }

    fun shouldDrawOrPass(state: GameState, player: Player): Boolean =
        aiEngine.shouldDrawOrPass(state, player)

    private fun LongRange.random(): Long = (first..last).let { first + (Math.random() * (last - first)).toLong() }
}
