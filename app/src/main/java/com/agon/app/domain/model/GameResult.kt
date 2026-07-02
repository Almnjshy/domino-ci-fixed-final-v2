package com.agon.app.domain.model

import java.util.Date

/**
 * Represents the result of a completed game round
 */
data class GameResult(
    val winnerId: Int,
    val winnerName: String,
    val scores: Map<Int, Int>,
    val durationSeconds: Long,
    val playedAt: Date = Date(),
    val gameMode: GameMode,
    val totalRounds: Int = 1
) {
    companion object {
        val EMPTY = GameResult(
            winnerId = -1,
            winnerName = "",
            scores = emptyMap(),
            durationSeconds = 0,
            gameMode = GameMode.HUMAN_VS_AI
        )
    }
}

enum class GameMode {
    HUMAN_VS_AI,
    HUMAN_VS_HUMAN,
    FOUR_HUMANS;

    val playerCount: Int
        get() = when (this) {
            HUMAN_VS_AI -> 2
            HUMAN_VS_HUMAN -> 2
            FOUR_HUMANS -> 4
        }

    val aiCount: Int
        get() = when (this) {
            HUMAN_VS_AI -> 1
            HUMAN_VS_HUMAN -> 0
            FOUR_HUMANS -> 0
        }
}
