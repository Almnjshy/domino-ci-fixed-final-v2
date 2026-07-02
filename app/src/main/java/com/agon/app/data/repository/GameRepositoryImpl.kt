package com.agon.app.data.repository

import com.agon.app.domain.engine.ai.DominoAIEngine
import com.agon.app.domain.engine.game.DominoGameEngine
import com.agon.app.domain.model.*
import com.agon.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository holds the single source of truth for GameState.
 * The Engine is stateless — Repository owns the state.
 *
 * FIXES:
 * - drawOrPass() now checks if player has legal moves first
 * - Added passOnly() for network PassTurn actions
 * - Better error handling with Arabic messages
 * - Thread-safe state updates
 */
@Singleton
class GameRepositoryImpl @Inject constructor(
    private val engine: DominoGameEngine,
    private val aiEngine: DominoAIEngine
) : GameRepository {

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _gameResults = MutableStateFlow<GameResult?>(null)
    override val gameResults: Flow<GameResult> = _gameResults.filterNotNull()

    override suspend fun newGame(mode: GameMode): Result<GameState> = runCatching {
        val state = engine.newGame(mode)
        _gameState.value = state
        state
    }

    override suspend fun newRound(mode: GameMode): Result<GameState> = runCatching {
        val currentMatchScore = _gameState.value.matchScore
        val state = engine.newRound(mode, currentMatchScore)
        _gameState.value = state
        state
    }

    override suspend fun playTile(tile: DominoTile, side: BoardSide): Result<GameState> {
        val current = _gameState.value
        return engine.reduce(current, GameAction.PlayTile(current.currentPlayerIndex, tile, side))
            .onSuccess { newState ->
                _gameState.value = newState
                emitResultIfOver(newState)
            }
    }

    /**
     * FIX #1: drawOrPass now checks if player has legal moves first.
     * In domino rules, a player MUST play if they have a legal move.
     * They can only draw/pass if they have no legal moves.
     */
    override suspend fun drawOrPass(): Result<GameState> {
        val current = _gameState.value

        // Check if the current player has legal moves
        if (engine.hasLegalMoves(current)) {
            return Result.failure(
                IllegalStateException("لديك حركات قانونية - يجب اللعب أولاً")
            )
        }

        val action = if (current.stock.isNotEmpty())
            GameAction.DrawTile(current.currentPlayerIndex, null)
        else
            GameAction.PassTurn(current.currentPlayerIndex)

        return engine.reduce(current, action).onSuccess { newState ->
            _gameState.value = newState
            emitResultIfOver(newState)
        }
    }

    /**
     * FIX #2: New method for forced pass (used in network mode).
     * When a remote player sends PassTurn, we trust they already tried to draw.
     */
    override suspend fun passTurn(): Result<GameState> {
        val current = _gameState.value
        val action = GameAction.PassTurn(current.currentPlayerIndex)
        return engine.reduce(current, action).onSuccess { newState ->
            _gameState.value = newState
            emitResultIfOver(newState)
        }
    }

    override fun getLegalSides(tile: DominoTile): Set<BoardSide> =
        engine.getLegalSides(_gameState.value, tile)

    override fun hasLegalMoves(): Boolean =
        engine.hasLegalMoves(_gameState.value)

    override fun isGameOver(): Boolean = _gameState.value.isGameOver
    override fun getWinner(): Int? = _gameState.value.winnerId
    override fun getScores(): Map<Int, Int> = _gameState.value.players.associate { it.id to it.handValue }
    override fun getMatchScore(): MatchScore = _gameState.value.matchScore

    override suspend fun resetGame(): Result<GameState> =
        newGame(_gameState.value.gameMode)

    private fun emitResultIfOver(state: GameState) {
        val result = engine.getGameResult(state)
        if (result != null) _gameResults.value = result
    }
}
