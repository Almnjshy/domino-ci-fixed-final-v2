package com.agon.app.domain.model

/**
 * Tracks cumulative score across multiple rounds in a match
 * Winner is first to reach targetScore
 */
data class MatchScore(
    val scores: Map<Int, Int> = emptyMap(),     // playerId -> cumulative points
    val roundsWon: Map<Int, Int> = emptyMap(),   // playerId -> rounds won
    val targetScore: Int = 100,                  // score to win the match
    val currentRound: Int = 1,
    val matchWinnerId: Int? = null,
    val roundHistory: List<RoundResult> = emptyList()
) {
    val isMatchOver: Boolean get() = matchWinnerId != null

    val leaderboard: List<Pair<Int, Int>>
        get() = scores.entries.sortedByDescending { it.value }.map { it.toPair() }

    fun addRoundResult(roundResult: RoundResult): MatchScore {
        val newScores = scores.toMutableMap()
        val newRoundsWon = roundsWon.toMutableMap()

        // Add hand values of losers to winner's score (classic domino scoring)
        val pointsEarned = roundResult.loserHandValues.values.sum()
        val winnerId = roundResult.winnerId

        if (winnerId != null) {
            newScores[winnerId] = (newScores[winnerId] ?: 0) + pointsEarned
            newRoundsWon[winnerId] = (newRoundsWon[winnerId] ?: 0) + 1
        }

        val matchWinner = newScores.entries.firstOrNull { it.value >= targetScore }?.key

        return copy(
            scores = newScores,
            roundsWon = newRoundsWon,
            matchWinnerId = matchWinner,
            currentRound = currentRound + 1,
            roundHistory = roundHistory + roundResult
        )
    }

    fun playerScore(playerId: Int): Int = scores[playerId] ?: 0
    fun playerRoundsWon(playerId: Int): Int = roundsWon[playerId] ?: 0
    fun progressPercent(playerId: Int): Float = (playerScore(playerId).toFloat() / targetScore).coerceIn(0f, 1f)
}

data class RoundResult(
    val roundNumber: Int,
    val winnerId: Int?,              // null = tie/blocked
    val loserHandValues: Map<Int, Int>, // playerId -> remaining hand value
    val pointsEarned: Int,
    val wasBlocked: Boolean = false,
    val durationSeconds: Long = 0
)
