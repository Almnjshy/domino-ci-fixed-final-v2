package com.agon.app.domain.validation

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameState
import com.agon.app.domain.model.Player

/**
 * Game validation layer
 * Validates all game actions before execution
 */
class GameValidator {

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validatePlayTile(state: GameState, tile: DominoTile, side: BoardSide): Result<Unit> {
        val currentPlayer = state.currentPlayer
        return when {
            state.roundOver -> Result.failure(IllegalStateException("Game is already over"))
            state.isBlocked -> Result.failure(IllegalStateException("Game is blocked"))
            currentPlayer == null -> Result.failure(IllegalStateException("No current player"))
            currentPlayer.isAi -> Result.failure(IllegalStateException("AI player's turn"))
            !currentPlayer.hand.any { it.id == tile.id } ->
                Result.failure(IllegalArgumentException("Player doesn't have this tile"))
            side !in state.board.getLegalSides(tile) ->
                Result.failure(IllegalArgumentException("Illegal move for this tile"))
            else -> Result.success(Unit)
        }
    }

    fun validateDrawOrPass(state: GameState): Result<Unit> {
        val currentPlayer = state.currentPlayer
        return when {
            state.roundOver -> Result.failure(IllegalStateException("Game is already over"))
            state.isBlocked -> Result.failure(IllegalStateException("Game is blocked"))
            currentPlayer == null -> Result.failure(IllegalStateException("No current player"))
            currentPlayer.isAi -> Result.failure(IllegalStateException("AI player's turn"))
            else -> Result.success(Unit)
        }
    }

    fun validateNewGame(mode: com.agon.app.domain.model.GameMode): Result<Unit> {
        return when {
            mode.playerCount < 2 -> Result.failure(IllegalArgumentException("Need at least 2 players"))
            mode.playerCount > 4 -> Result.failure(IllegalArgumentException("Max 4 players"))
            else -> Result.success(Unit)
        }
    }

    fun validateGameState(state: GameState): ValidationResult {
        return when {
            state.players.isEmpty() -> ValidationResult.Invalid("No players in game")
            state.players.any { it.hand.size > 7 } -> ValidationResult.Invalid("Player has too many tiles")
            state.stock.size + state.players.sumOf { it.hand.size } + state.board.tiles.size != 28 ->
                ValidationResult.Invalid("Tile count mismatch")
            else -> ValidationResult.Valid
        }
    }

    fun validatePlayerTurn(state: GameState, playerId: Int): Result<Unit> {
        return when {
            state.currentPlayerIndex != playerId ->
                Result.failure(IllegalStateException("Not player's turn"))
            else -> Result.success(Unit)
        }
    }
}
