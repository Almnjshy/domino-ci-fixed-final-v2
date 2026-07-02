package com.agon.app.domain.repository

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.model.GameResult
import com.agon.app.domain.model.GameState
import com.agon.app.domain.model.MatchScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    val gameState: StateFlow<GameState>
    val gameResults: Flow<GameResult>

    suspend fun newGame(mode: GameMode): Result<GameState>
    suspend fun newRound(mode: GameMode): Result<GameState>
    suspend fun playTile(tile: DominoTile, side: BoardSide): Result<GameState>
    suspend fun drawOrPass(): Result<GameState>

    /**
     * NEW: Force pass without checking legal moves.
     * Used in network mode when remote player sends PassTurn.
     */
    suspend fun passTurn(): Result<GameState>

    fun getLegalSides(tile: DominoTile): Set<BoardSide>
    fun hasLegalMoves(): Boolean
    fun isGameOver(): Boolean
    fun getWinner(): Int?
    suspend fun resetGame(): Result<GameState>
    fun getScores(): Map<Int, Int>
    fun getMatchScore(): MatchScore
}
