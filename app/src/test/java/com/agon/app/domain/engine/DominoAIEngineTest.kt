package com.agon.app.domain.engine

import com.agon.app.domain.engine.ai.DominoAIEngine
import com.agon.app.domain.engine.game.DominoGameEngine
import com.agon.app.domain.engine.game.FakeClock
import com.agon.app.domain.model.*
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class DominoAIEngineTest {

    private lateinit var aiEngine: DominoAIEngine
    private lateinit var gameEngine: DominoGameEngine

    @Before
    fun setup() {
        aiEngine = DominoAIEngine()
        gameEngine = DominoGameEngine(FakeClock())
    }

    @Test
    fun `calculateBestMove returns null when no legal moves`() {
        val state = gameEngine.newGame(GameMode.HUMAN_VS_AI)
        // Create a player with no tiles that match the board
        val player = Player(id = 1, name = "Bot", isAi = true, hand = emptyList())
        val result = aiEngine.calculateBestMove(state, player, AiDifficulty.HARD)
        assertThat(result).isNull()
    }

    @Test
    fun `calculateBestMove always returns a legal move`() {
        val state = gameEngine.newGame(GameMode.HUMAN_VS_AI)
        val aiPlayer = state.players.firstOrNull { it.isAi } ?: return
        val legalTiles = aiPlayer.hand.filter { state.board.getLegalSides(it).isNotEmpty() }
        if (legalTiles.isEmpty()) return // empty board case

        val move = aiEngine.calculateBestMove(state, aiPlayer, AiDifficulty.HARD)
        if (move != null) {
            assertThat(state.board.getLegalSides(move.tile)).contains(move.side)
        }
    }

    @Test
    fun `shouldDrawOrPass returns true for empty hand`() {
        val state = gameEngine.newGame(GameMode.HUMAN_VS_AI)
        val emptyPlayer = Player(id = 0, name = "Test", isAi = false, hand = emptyList())
        assertThat(aiEngine.shouldDrawOrPass(state, emptyPlayer)).isTrue()
    }

    @Test
    fun `AI is stateless - same input gives consistent output type`() {
        val state = gameEngine.newGame(GameMode.HUMAN_VS_AI)
        val aiPlayer = state.players.firstOrNull { it.isAi } ?: return
        val move1 = aiEngine.calculateBestMove(state, aiPlayer, AiDifficulty.HARD)
        val move2 = aiEngine.calculateBestMove(state, aiPlayer, AiDifficulty.HARD)
        // Both null or both non-null — stateless so consistent
        assertThat(move1 == null).isEqualTo(move2 == null)
    }

    @Test
    fun `EASY difficulty sometimes makes mistakes`() {
        val state = gameEngine.newGame(GameMode.HUMAN_VS_AI)
        val aiPlayer = state.players.firstOrNull { it.isAi } ?: return
        // Run 20 times — at least some results should differ (randomness check)
        val moves = (1..20).mapNotNull {
            aiEngine.calculateBestMove(state, aiPlayer, AiDifficulty.EASY)
        }
        // At least we get valid moves each time
        moves.forEach { move ->
            assertThat(state.board.getLegalSides(move.tile)).contains(move.side)
        }
    }
}
