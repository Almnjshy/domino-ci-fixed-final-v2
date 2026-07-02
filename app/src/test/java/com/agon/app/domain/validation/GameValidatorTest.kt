package com.agon.app.domain.validation

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.model.GameState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameValidatorTest {

    private lateinit var validator: GameValidator
    private lateinit var gameState: GameState

    @Before
    fun setup() {
        validator = GameValidator()
        val engine = com.agon.app.domain.engine.game.DominoGameEngine()
        gameState = engine.newGame(GameMode.HUMAN_VS_AI)
    }

    @Test
    fun `validatePlayTile with valid move returns success`() {
        val player = gameState.currentPlayer!!
        val tile = player.hand.first()
        val side = BoardSide.LEFT

        val result = validator.validatePlayTile(gameState, tile, side)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validatePlayTile with invalid tile returns failure`() {
        val invalidTile = DominoTile(99, 99)

        val result = validator.validatePlayTile(gameState, invalidTile, BoardSide.LEFT)

        assertTrue(result.isFailure)
    }

    @Test
    fun `validatePlayTile when game over returns failure`() {
        val finishedState = gameState.copy(roundOver = true)
        val tile = gameState.currentPlayer!!.hand.first()

        val result = validator.validatePlayTile(finishedState, tile, BoardSide.LEFT)

        assertTrue(result.isFailure)
    }

    @Test
    fun `validateDrawOrPass with valid state returns success`() {
        val result = validator.validateDrawOrPass(gameState)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateNewGame with valid mode returns success`() {
        val result = validator.validateNewGame(GameMode.HUMAN_VS_AI)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `validateGameState with valid state returns valid`() {
        val result = validator.validateGameState(gameState)

        assertTrue(result is GameValidator.ValidationResult.Valid)
    }

    @Test
    fun `validateGameState with empty players returns invalid`() {
        val invalidState = gameState.copy(players = emptyList())

        val result = validator.validateGameState(invalidState)

        assertTrue(result is GameValidator.ValidationResult.Invalid)
    }
}
