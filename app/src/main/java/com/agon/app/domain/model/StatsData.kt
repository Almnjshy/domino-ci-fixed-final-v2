package com.agon.app.domain.model

import java.util.Date

/**
 * Player statistics data
 */
data class StatsData(
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalPoints: Int = 0,
    val longestWinStreak: Int = 0,
    val currentWinStreak: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val history: List<MatchRecord> = emptyList(),
    val lastUpdated: Date = Date()
) {
    val winRate: Float
        get() = if (matchesPlayed > 0) (wins.toFloat() / matchesPlayed) * 100 else 0f

    val averagePoints: Float
        get() = if (matchesPlayed > 0) totalPoints.toFloat() / matchesPlayed else 0f

    fun recordMatch(result: GameResult, isWinner: Boolean): StatsData {
        val newStreak = if (isWinner) currentWinStreak + 1 else 0
        return copy(
            matchesPlayed = matchesPlayed + 1,
            wins = wins + if (isWinner) 1 else 0,
            losses = losses + if (!isWinner) 1 else 0,
            totalPoints = totalPoints + (result.scores.values.firstOrNull() ?: 0),
            longestWinStreak = maxOf(longestWinStreak, newStreak),
            currentWinStreak = newStreak,
            totalPlayTimeSeconds = totalPlayTimeSeconds + result.durationSeconds,
            history = (history + MatchRecord.fromResult(result)).takeLast(50),
            lastUpdated = Date()
        )
    }

    fun clear(): StatsData = StatsData()
}

data class MatchRecord(
    val id: String,
    val date: Date,
    val gameMode: GameMode,
    val winnerName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val durationSeconds: Long,
    val isWin: Boolean
) {
    companion object {
        fun fromResult(result: GameResult): MatchRecord = MatchRecord(
            id = System.currentTimeMillis().toString(),
            date = result.playedAt,
            gameMode = result.gameMode,
            winnerName = result.winnerName,
            playerScore = result.scores[0] ?: 0,
            opponentScore = result.scores[1] ?: 0,
            durationSeconds = result.durationSeconds,
            isWin = result.winnerId == 0
        )
    }
}
