package com.agon.app.domain.engine

import com.agon.app.domain.model.MatchScore
import com.agon.app.domain.model.RoundResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MatchScoreTest {

    @Test
    fun `addRoundResult updates scores correctly`() {
        val score = MatchScore(
            scores = mapOf(0 to 0, 1 to 0),
            roundsWon = mapOf(0 to 0, 1 to 0),
            targetScore = 100
        )
        val result = RoundResult(
            roundNumber = 1, winnerId = 0,
            loserHandValues = mapOf(1 to 35), pointsEarned = 35
        )
        val updated = score.addRoundResult(result)
        assertThat(updated.playerScore(0)).isEqualTo(35)
        assertThat(updated.playerScore(1)).isEqualTo(0)
        assertThat(updated.playerRoundsWon(0)).isEqualTo(1)
    }

    @Test
    fun `match is over when score reaches target`() {
        val score = MatchScore(
            scores = mapOf(0 to 95, 1 to 50),
            roundsWon = mapOf(0 to 3, 1 to 1),
            targetScore = 100
        )
        val result = RoundResult(
            roundNumber = 5, winnerId = 0,
            loserHandValues = mapOf(1 to 10), pointsEarned = 10
        )
        val updated = score.addRoundResult(result)
        assertThat(updated.isMatchOver).isTrue()
        assertThat(updated.matchWinnerId).isEqualTo(0)
    }

    @Test
    fun `progressPercent is capped at 1f`() {
        val score = MatchScore(scores = mapOf(0 to 150), targetScore = 100)
        assertThat(score.progressPercent(0)).isEqualTo(1f)
    }

    @Test
    fun `leaderboard is sorted by score descending`() {
        val score = MatchScore(scores = mapOf(0 to 30, 1 to 70, 2 to 50))
        val board = score.leaderboard
        assertThat(board[0].first).isEqualTo(1)
        assertThat(board[1].first).isEqualTo(2)
        assertThat(board[2].first).isEqualTo(0)
    }
}
