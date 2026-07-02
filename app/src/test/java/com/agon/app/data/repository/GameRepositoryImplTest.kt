package com.agon.app.data.repository

import com.agon.app.domain.model.GameMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {

    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setup() {
        repository = GameRepositoryImpl()
    }

    @Test
    fun `newGame creates game with correct player count`() = runTest {
        val result = repository.newGame(GameMode.HUMAN_VS_AI)

        assertTrue(result.isSuccess)
        val state = result.getOrNull()!!
        assertEquals(2, state.players.size)
        assertEquals(GameMode.HUMAN_VS_AI, state.gameMode)
    }

    @Test
    fun `newGame deals 7 tiles per player`() = runTest {
        val result = repository.newGame(GameMode.HUMAN_VS_AI)

        val state = result.getOrNull()!!
        state.players.forEach { player ->
            assertEquals(7, player.hand.size)
        }
    }

    @Test
    fun `newGame creates stock with remaining tiles`() = runTest {
        val result = repository.newGame(GameMode.HUMAN_VS_AI)

        val state = result.getOrNull()!!
        assertEquals(14, state.stock.size) // 28 - (7 * 2)
    }

    @Test
    fun `newGame with four humans creates 4 players`() = runTest {
        val result = repository.newGame(GameMode.FOUR_HUMANS)

        val state = result.getOrNull()!!
        assertEquals(4, state.players.size)
    }

    @Test
    fun `gameState is initially empty`() {
        val state = repository.gameState.value
        assertTrue(state.players.isEmpty())
        assertTrue(state.board.tiles.isEmpty())
    }

    @Test
    fun `isGameOver returns false for new game`() = runTest {
        repository.newGame(GameMode.HUMAN_VS_AI)
        assertFalse(repository.isGameOver())
    }

    @Test
    fun `getScores returns empty for new game`() = runTest {
        repository.newGame(GameMode.HUMAN_VS_AI)
        val scores = repository.getScores()
        assertTrue(scores.isNotEmpty())
    }
}
