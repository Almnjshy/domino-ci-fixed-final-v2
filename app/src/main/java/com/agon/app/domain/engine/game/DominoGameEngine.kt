package com.agon.app.domain.engine.game

import com.agon.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure-function game engine — NO internal mutable state.
 * Every function takes a GameState and returns a new GameState.
 * This makes it fully testable, thread-safe, and network-ready.
 *
 * Pattern: Redux-style reducer
 * fun reduce(state: GameState, action: GameAction): Result<GameState>
 *
 * FIXES:
 * - handlePlayTile now checks Result from board.place()
 * - Fixed next player logic when round ends
 * - Better validation with clear error messages
 * - Fixed isBlocked to properly detect game lock
 */
@Singleton
class DominoGameEngine @Inject constructor(
    private val clock: GameClock
) {

    // ─────────────────────────────────────────────
    // Public API — all pure functions
    // ─────────────────────────────────────────────

    fun newGame(mode: GameMode): GameState {
        val initialMatchScore = MatchScore(
            scores = (0 until mode.playerCount).associate { it to 0 },
            roundsWon = (0 until mode.playerCount).associate { it to 0 },
            targetScore = 100
        )
        return startRound(mode, initialMatchScore, clock.now())
    }

    fun newRound(mode: GameMode, existingMatchScore: MatchScore): GameState =
        startRound(mode, existingMatchScore, clock.now())

    fun reduce(state: GameState, action: GameAction): Result<GameState> = when (action) {
        is GameAction.PlayTile -> handlePlayTile(state, action)
        is GameAction.DrawTile -> handleDraw(state)
        is GameAction.PassTurn -> handlePass(state)
        is GameAction.WinRound -> Result.success(state) // terminal — no further reduce
    }

    fun getLegalSides(state: GameState, tile: DominoTile): Set<BoardSide> =
        state.board.getLegalSides(tile)

    fun hasLegalMoves(state: GameState): Boolean =
        state.currentPlayer?.hand?.any { state.board.getLegalSides(it).isNotEmpty() } == true

    fun shouldDrawOrPass(state: GameState): Boolean = !hasLegalMoves(state)

    fun getGameResult(state: GameState): GameResult? {
        if (!state.isGameOver) return null
        val duration = (clock.now() - state.roundStartTime) / 1000
        val winner = state.winnerId
        return GameResult(
            winnerId = winner ?: -1,
            winnerName = winner?.let { state.players.getOrNull(it)?.displayName() } ?: "تعادل",
            scores = state.players.associate { it.id to it.handValue },
            durationSeconds = duration,
            gameMode = state.gameMode
        )
    }

    // ─────────────────────────────────────────────
    // Private reducers
    // ─────────────────────────────────────────────

    /**
     * FIX #1: Handle Result from board.place()
     * FIX #2: Proper next player logic
     * FIX #3: Clear validation with specific error messages
     */
    private fun handlePlayTile(state: GameState, action: GameAction.PlayTile): Result<GameState> {
        // Step 1: Validate the action
        val validation = validatePlayTile(state, action.tile, action.side)
        if (validation.isFailure) return Result.failure(validation.exceptionOrNull()!!)

        val player = state.currentPlayer
            ?: return Result.failure(IllegalStateException("لا يوجد لاعب حالي"))

        // Step 2: Place tile on board (returns Result<BoardState>)
        val boardResult = state.board.place(action.tile, action.side)
        if (boardResult.isFailure) {
            return Result.failure(boardResult.exceptionOrNull()!!)
        }
        val newBoard = boardResult.getOrThrow()

        // Step 3: Remove tile from player's hand
        val updatedPlayer = player.withoutTile(action.tile)
        val updatedPlayers = state.players.map {
            if (it.id == player.id) updatedPlayer else it
        }

        // Step 4: Build new state
        var newState = state.copy(
            players = updatedPlayers,
            board = newBoard,
            lastAction = action
        )

        // Step 5: Check if round ended (player emptied hand)
        newState = checkRoundEnd(newState, updatedPlayer)

        // Step 6: Move to next player ONLY if round hasn't ended
        if (!newState.roundOver && !newState.isBlocked) {
            val nextIdx = (state.currentPlayerIndex + 1) % state.players.size
            newState = newState.copy(
                currentPlayerIndex = nextIdx,
                turnCount = newState.turnCount + 1,
                message = "دور ${updatedPlayers.getOrNull(nextIdx)?.displayName()}"
            )
        }

        return Result.success(newState)
    }

    /**
     * Handle drawing a tile from stock.
     * If stock is empty, automatically pass.
     */
    private fun handleDraw(state: GameState): Result<GameState> {
        val player = state.currentPlayer
            ?: return Result.failure(IllegalStateException("لا يوجد لاعب حالي"))

        // If stock is empty, pass instead
        if (state.stock.isEmpty()) {
            return handlePass(state)
        }

        val drawnTile = state.stock.first()
        val newStock = state.stock.drop(1)
        val updatedPlayer = player.withTile(drawnTile)
        val updatedPlayers = state.players.map {
            if (it.id == player.id) updatedPlayer else it
        }

        // Check if drawn tile can be played immediately
        val canPlayDrawn = state.board.getLegalSides(drawnTile).isNotEmpty()

        // If can play, stay on same player; otherwise move to next
        val nextIdx = if (!canPlayDrawn) {
            (state.currentPlayerIndex + 1) % state.players.size
        } else {
            state.currentPlayerIndex
        }

        return Result.success(
            state.copy(
                players = updatedPlayers,
                stock = newStock,
                currentPlayerIndex = nextIdx,
                turnCount = if (!canPlayDrawn) state.turnCount + 1 else state.turnCount,
                lastAction = GameAction.DrawTile(player.id, drawnTile),
                message = if (canPlayDrawn)
                    "${player.displayName()} سحب قطعة ويمكن لعبها"
                else
                    "${player.displayName()} سحب قطعة ومرر"
            )
        )
    }

    /**
     * Handle passing turn (no moves available and stock empty)
     */
    private fun handlePass(state: GameState): Result<GameState> {
        val player = state.currentPlayer
            ?: return Result.failure(IllegalStateException("لا يوجد لاعب حالي"))

        val nextIdx = (state.currentPlayerIndex + 1) % state.players.size

        return Result.success(
            state.copy(
                currentPlayerIndex = nextIdx,
                turnCount = state.turnCount + 1,
                lastAction = GameAction.PassTurn(player.id),
                message = "${player.displayName()} تخطى دوره"
            )
        )
    }

    // FIX #4: Better validation with clear error messages
    private fun validatePlayTile(
        state: GameState,
        tile: DominoTile,
        side: BoardSide
    ): Result<Unit> {
        val currentPlayer = state.currentPlayer
        return when {
            state.roundOver -> Result.failure(IllegalStateException("الجولة انتهت"))
            state.isBlocked -> Result.failure(IllegalStateException("اللعبة موقوفة"))
            currentPlayer == null -> Result.failure(IllegalStateException("لا يوجد لاعب حالي"))
            currentPlayer.hand.none { it.id == tile.id } ->
                Result.failure(IllegalArgumentException("اللاعب لا يملك هذه القطعة"))
            side !in state.board.getLegalSides(tile) ->
                Result.failure(IllegalArgumentException(
                    "حركة غير قانونية: ${tile} في جانب $side. " +
                    "الأطراف المتاحة: [${state.board.leftEnd}|${state.board.rightEnd}]"
                ))
            else -> Result.success(Unit)
        }
    }

    /**
     * Check if the round has ended (player emptied hand or game is blocked)
     */
    private fun checkRoundEnd(state: GameState, lastPlayer: Player): GameState {
        // Case 1: player emptied hand → wins round
        if (lastPlayer.hand.isEmpty()) {
            val loserValues = state.players
                .filter { it.id != lastPlayer.id }
                .associate { it.id to it.handValue }
            val pointsEarned = loserValues.values.sum()

            val roundResult = RoundResult(
                roundNumber = state.matchScore.currentRound,
                winnerId = lastPlayer.id,
                loserHandValues = loserValues,
                pointsEarned = pointsEarned,
                durationSeconds = (clock.now() - state.roundStartTime) / 1000
            )

            val updated = state.applyRoundResult(roundResult)
            return updated.copy(
                roundOver = true,
                winnerId = lastPlayer.id,
                message = buildString {
                    append("${lastPlayer.displayName()} فاز بالجولة! (+$pointsEarned نقطة)")
                    if (updated.isMatchOver) append("\n🏆 ${lastPlayer.displayName()} فاز بالمباراة!")
                },
                lastAction = GameAction.WinRound(
                    lastPlayer.id,
                    state.players.associate { it.id to it.handValue }
                )
            )
        }

        // Case 2: game blocked (no one can play and stock empty)
        if (isBlocked(state)) {
            val minValue = state.players.minOf { it.handValue }
            val winners = state.players.filter { it.handValue == minValue }

            // FIX #5: Handle tie properly
            val winnerId = if (winners.size == 1) winners.first().id else null
            val loserValues = state.players.associate { it.id to it.handValue }
            val pointsEarned = if (winnerId != null)
                loserValues.filter { it.key != winnerId }.values.sum() else 0

            val roundResult = RoundResult(
                roundNumber = state.matchScore.currentRound,
                winnerId = winnerId,
                loserHandValues = loserValues,
                pointsEarned = pointsEarned,
                wasBlocked = true,
                durationSeconds = (clock.now() - state.roundStartTime) / 1000
            )

            val updated = state.applyRoundResult(roundResult)
            return updated.copy(
                roundOver = true,
                isBlocked = true,
                winnerId = winnerId,
                message = if (winnerId != null)
                    "اللعبة موقوفة! ${state.players.getOrNull(winnerId)?.displayName()} (+$pointsEarned نقطة)"
                else
                    "اللعبة موقوفة! تعادل - جميع اللاعبين بنفس القيمة",
                lastAction = GameAction.WinRound(winnerId ?: -1, loserValues)
            )
        }

        return state
    }

    /**
     * FIX #6: More accurate blocked detection
     * Game is blocked when:
     * 1. Stock is empty
     * 2. Current player cannot play
     * 3. After checking all players in sequence, none can play
     */
    private fun isBlocked(state: GameState): Boolean {
        if (state.stock.isNotEmpty()) return false

        // Check if current player can play
        val currentCanPlay = state.currentPlayer?.hand?.any {
            state.board.getLegalSides(it).isNotEmpty()
        } ?: false
        if (currentCanPlay) return false

        // Check all other players
        return state.players.all { player ->
            player.hand.all { state.board.getLegalSides(it).isEmpty() }
        }
    }

    private fun startRound(mode: GameMode, matchScore: MatchScore, now: Long): GameState {
        val playerCount = mode.playerCount
        val tilesPerPlayer = 28 / playerCount

        // Always shuffle
        val deck = DominoTile.createDeck().shuffled()

        val players = List(playerCount) { index ->
            val isAi = index >= (playerCount - mode.aiCount)
            Player(
                id = index,
                name = if (isAi) "Bot ${index + 1}" else "اللاعب ${index + 1}",
                isAi = isAi,
                hand = deck.drop(index * tilesPerPlayer).take(tilesPerPlayer)
            )
        }
        val stock = deck.drop(playerCount * tilesPerPlayer)
        val startingPlayer = findStartingPlayer(players)

        return GameState(
            players = players,
            board = BoardState(),
            stock = stock,
            currentPlayerIndex = startingPlayer,
            gameMode = mode,
            matchScore = matchScore,
            roundStartTime = now,
            message = "${players[startingPlayer].displayName()} يبدأ الجولة ${matchScore.currentRound}"
        )
    }

    /**
     * Find starting player: highest double, or highest total if no doubles
     */
    private fun findStartingPlayer(players: List<Player>): Int {
        var bestIndex = 0
        var bestValue = -1

        players.forEachIndexed { i, player ->
            val bestDouble = player.hand.filter { it.isDouble }.maxByOrNull { it.total }?.total
            val value = bestDouble ?: player.hand.maxOfOrNull { it.total } ?: 0
            if (value > bestValue) {
                bestValue = value
                bestIndex = i
            }
        }
        return bestIndex
    }
}
