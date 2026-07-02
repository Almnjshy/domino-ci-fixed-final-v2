package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.model.*
import com.agon.app.domain.repository.NetworkRepository
import com.agon.app.domain.usecase.ai.AIPlayUseCase
import com.agon.app.domain.usecase.game.*
import com.agon.app.domain.usecase.network.*
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.stats.RecordGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val newGameUseCase: NewGameUseCase,
    private val playTileUseCase: PlayTileUseCase,
    private val drawOrPassUseCase: DrawOrPassUseCase,
    private val getLegalMovesUseCase: GetLegalMovesUseCase,
    private val checkGameOverUseCase: CheckGameOverUseCase,
    private val aiPlayUseCase: AIPlayUseCase,
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val recordGameUseCase: RecordGameUseCase,
    // NEW: PassTurn use case for network mode
    private val passTurnUseCase: PassTurnUseCase,
    // Network dependencies
    private val networkRepository: NetworkRepository,
    private val sendGameActionUseCase: SendGameActionUseCase,
    private val syncGameStateUseCase: SyncGameStateUseCase,
    private val observeNetworkEventsUseCase: ObserveNetworkEventsUseCase
) : ViewModel() {

    data class GameUiState(
        val gameState: GameState = GameState(),
        val isLoading: Boolean = false,
        val isAiThinking: Boolean = false,
        val error: String? = null,
        val showRoundResult: Boolean = false,
        val showMatchResult: Boolean = false,
        val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
        val selectedTile: DominoTile? = null,
        val isNetworkGame: Boolean = false,
        val isHost: Boolean = false,
        val localPlayerId: Int = 0,
        val networkStatus: String = "",
        val isMyTurn: Boolean = false
    ) {
        val showResult: Boolean get() = showRoundResult || showMatchResult
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val aiTurnQueue = Channel<Unit>(Channel.CONFLATED)
    private var currentGameMode: GameMode = GameMode.HUMAN_VS_AI

    init {
        viewModelScope.launch {
            aiTurnQueue.receiveAsFlow().collect { processAiTurn() }
        }
        viewModelScope.launch {
            try {
                val settings = loadSettingsUseCase()
                _uiState.value = _uiState.value.copy(aiDifficulty = settings.aiDifficulty)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LOCAL GAME
    // ═══════════════════════════════════════════════════════════════════════
    fun newGame(mode: GameMode) {
        currentGameMode = mode
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, showRoundResult = false,
                showMatchResult = false, error = null, selectedTile = null,
                isNetworkGame = false, isHost = false, networkStatus = ""
            )
            newGameUseCase(mode)
                .onSuccess { state ->
                    _uiState.value = _uiState.value.copy(
                        gameState = state, isLoading = false, isMyTurn = true
                    )
                    queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NETWORK GAME: Initialize from NetworkState
    // ═══════════════════════════════════════════════════════════════════════
    fun startNetworkGame(networkState: com.agon.app.domain.model.NetworkState) {
        currentGameMode = GameMode.HUMAN_VS_HUMAN
        val isHost = networkState.isHost
        val localPlayerId = if (isHost) 0 else networkState.connectedPlayers.indexOfFirst {
            it.id == networkState.localPlayerId
        }.coerceAtLeast(0)

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            isNetworkGame = true,
            isHost = isHost,
            localPlayerId = localPlayerId,
            networkStatus = if (isHost) "أنت المضيف" else "متصل بالمضيف",
            showRoundResult = false,
            showMatchResult = false,
            error = null
        )

        observeNetworkEvents()

        if (isHost) {
            viewModelScope.launch {
                val playerCount = networkState.connectedPlayers.size.coerceIn(2, 4)
                val mode = when (playerCount) {
                    2 -> GameMode.HUMAN_VS_HUMAN
                    else -> GameMode.FOUR_HUMANS
                }
                newGameUseCase(mode)
                    .onSuccess { state ->
                        _uiState.value = _uiState.value.copy(
                            gameState = state, isLoading = false,
                            isMyTurn = state.currentPlayerIndex == localPlayerId
                        )
                        syncGameStateUseCase(state)
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                networkStatus = "في انتظار المضيف..."
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NETWORK: Observe events from other players
    // ═══════════════════════════════════════════════════════════════════════
    private fun observeNetworkEvents() {
        viewModelScope.launch {
            observeNetworkEventsUseCase().collect { event ->
                when (event) {
                    is NetworkEvent.GameStateSync -> {
                        if (!_uiState.value.isHost) {
                            _uiState.value = _uiState.value.copy(
                                gameState = event.state,
                                isMyTurn = event.state.currentPlayerIndex == _uiState.value.localPlayerId,
                                networkStatus = "",
                                isLoading = false
                            )
                            handleRoundEnd(event.state)
                        }
                    }
                    is NetworkEvent.PlayerAction -> {
                        if (_uiState.value.isHost) {
                            applyRemoteAction(event.action)
                        }
                    }
                    is NetworkEvent.PlayerJoined -> {
                        _uiState.value = _uiState.value.copy(
                            networkStatus = "${event.player.name} انضم للعبة"
                        )
                    }
                    is NetworkEvent.PlayerLeft -> {
                        _uiState.value = _uiState.value.copy(
                            networkStatus = "غادر اللاعب",
                            error = "غادر أحد اللاعبين اللعبة"
                        )
                    }
                    is NetworkEvent.ConnectionLost -> {
                        _uiState.value = _uiState.value.copy(
                            error = "انقطع الاتصال بالشبكة",
                            isNetworkGame = false
                        )
                    }
                    is NetworkEvent.Error -> {
                        _uiState.value = _uiState.value.copy(error = event.message)
                    }
                    else -> {}
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NETWORK: Apply action received from remote player (Host only)
    // ═══════════════════════════════════════════════════════════════════════
    private fun applyRemoteAction(action: GameAction) {
        viewModelScope.launch {
            when (action) {
                is GameAction.PlayTile -> {
                    playTileUseCase(action.tile, action.side)
                        .onSuccess { newState ->
                            _uiState.value = _uiState.value.copy(
                                gameState = newState,
                                isMyTurn = newState.currentPlayerIndex == _uiState.value.localPlayerId
                            )
                            syncGameStateUseCase(newState)
                            handleRoundEnd(newState)
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(error = "خطأ في حركة بعيدة: ${e.message}")
                        }
                }
                is GameAction.DrawTile -> {
                    drawOrPassUseCase()
                        .onSuccess { newState ->
                            _uiState.value = _uiState.value.copy(
                                gameState = newState,
                                isMyTurn = newState.currentPlayerIndex == _uiState.value.localPlayerId
                            )
                            syncGameStateUseCase(newState)
                            handleRoundEnd(newState)
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(error = "خطأ في سحب بعيد: ${e.message}")
                        }
                }
                // FIX #1: Use passTurnUseCase instead of drawOrPassUseCase for PassTurn
                is GameAction.PassTurn -> {
                    passTurnUseCase()
                        .onSuccess { newState ->
                            _uiState.value = _uiState.value.copy(
                                gameState = newState,
                                isMyTurn = newState.currentPlayerIndex == _uiState.value.localPlayerId
                            )
                            syncGameStateUseCase(newState)
                            handleRoundEnd(newState)
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(error = "خطأ في تخطي بعيد: ${e.message}")
                        }
                }
                else -> {}
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PLAY TILE (Local + Network)
    // ═══════════════════════════════════════════════════════════════════════
    fun playTile(tile: DominoTile, side: BoardSide?) {
        val state = _uiState.value.gameState
        if (state.isGameOver) return
        val currentPlayer = state.currentPlayer ?: return

        // In network mode, only play if it's our turn
        if (_uiState.value.isNetworkGame) {
            if (state.currentPlayerIndex != _uiState.value.localPlayerId) return
        } else {
            if (currentPlayer.isAi) return
        }

        viewModelScope.launch {
            val selectedSide = side ?: getLegalMovesUseCase(tile).firstOrNull() ?: return@launch

            // In network mode, send action first
            if (_uiState.value.isNetworkGame) {
                val action = GameAction.PlayTile(
                    playerId = _uiState.value.localPlayerId,
                    tile = tile,
                    side = selectedSide
                )
                sendGameActionUseCase(action)
            }

            playTileUseCase(tile, selectedSide)
                .onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(
                        gameState = newState,
                        error = null,
                        selectedTile = null,
                        isMyTurn = if (_uiState.value.isNetworkGame) {
                            newState.currentPlayerIndex == _uiState.value.localPlayerId
                        } else true
                    )
                    handleRoundEnd(newState)
                    if (!newState.isGameOver && !_uiState.value.isNetworkGame) queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DRAW OR PASS (Local + Network)
    // ═══════════════════════════════════════════════════════════════════════
    fun drawOrPass() {
        val state = _uiState.value.gameState
        if (state.isGameOver) return
        val currentPlayer = state.currentPlayer ?: return

        // In network mode, only if it's our turn
        if (_uiState.value.isNetworkGame) {
            if (state.currentPlayerIndex != _uiState.value.localPlayerId) return
        } else {
            if (currentPlayer.isAi) return
        }

        viewModelScope.launch {
            // In network mode, send action first
            if (_uiState.value.isNetworkGame) {
                val action = GameAction.DrawTile(
                    playerId = _uiState.value.localPlayerId,
                    tile = state.stock.firstOrNull()
                )
                sendGameActionUseCase(action)
            }

            drawOrPassUseCase()
                .onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(
                        gameState = newState,
                        error = null,
                        isMyTurn = if (_uiState.value.isNetworkGame) {
                            newState.currentPlayerIndex == _uiState.value.localPlayerId
                        } else true
                    )
                    handleRoundEnd(newState)
                    if (!newState.isGameOver && !_uiState.value.isNetworkGame) queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NEW ROUND
    // ═══════════════════════════════════════════════════════════════════════
    fun newRound() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, showRoundResult = false,
                error = null, selectedTile = null
            )
            newGameUseCase.newRound(currentGameMode)
                .onSuccess { state ->
                    _uiState.value = _uiState.value.copy(
                        gameState = state, isLoading = false,
                        isMyTurn = if (_uiState.value.isNetworkGame) {
                            state.currentPlayerIndex == _uiState.value.localPlayerId
                        } else true
                    )
                    if (_uiState.value.isNetworkGame && _uiState.value.isHost) {
                        syncGameStateUseCase(state)
                    }
                    queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    fun selectTile(tile: DominoTile) {
        val sides = getLegalMovesUseCase(tile)
        if (sides.isEmpty()) return
        if (sides.size == 1) {
            playTile(tile, sides.first())
        } else {
            _uiState.value = _uiState.value.copy(selectedTile = tile)
        }
    }

    fun dismissRoundResult() {
        _uiState.value = _uiState.value.copy(showRoundResult = false)
    }

    fun dismissMatchResult() {
        _uiState.value = _uiState.value.copy(showMatchResult = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSelectedTile() {
        _uiState.value = _uiState.value.copy(selectedTile = null)
    }

    fun getLegalSides(tile: DominoTile): Set<BoardSide> = getLegalMovesUseCase(tile)

    // ═══════════════════════════════════════════════════════════════════════
    // AI
    // ═══════════════════════════════════════════════════════════════════════
    private fun queueAiTurnIfNeeded() {
        val state = _uiState.value.gameState
        val player = state.currentPlayer ?: return
        if (player.isAi && !state.isGameOver) aiTurnQueue.trySend(Unit)
    }

    /**
     * FIX #2: Improved AI turn processing with better error handling
     * and fallback logic.
     */
    private suspend fun processAiTurn() {
        val state = _uiState.value.gameState
        val player = state.currentPlayer ?: return
        if (!player.isAi || state.isGameOver) return

        _uiState.value = _uiState.value.copy(isAiThinking = true)
        val difficulty = _uiState.value.aiDifficulty

        // Check if AI should draw or pass
        if (aiPlayUseCase.shouldDrawOrPass(state, player)) {
            drawOrPassUseCase()
                .onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                    handleRoundEnd(newState)
                    if (!newState.isGameOver) queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isAiThinking = false, error = e.message)
                }
            return
        }

        // AI has a legal move - calculate best move
        val aiMove = aiPlayUseCase(state, player, difficulty)
        if (aiMove != null) {
            playTileUseCase(aiMove.tile, aiMove.side)
                .onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                    handleRoundEnd(newState)
                    if (!newState.isGameOver) queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    // If play fails, try draw/pass as fallback
                    _uiState.value = _uiState.value.copy(
                        isAiThinking = false,
                        error = "AI خطأ: ${e.message}"
                    )
                    drawOrPassUseCase()
                        .onSuccess { newState ->
                            _uiState.value = _uiState.value.copy(gameState = newState, error = null)
                            handleRoundEnd(newState)
                            if (!newState.isGameOver) queueAiTurnIfNeeded()
                        }
                }
        } else {
            // No move found but shouldDrawOrPass said false - safety fallback
            drawOrPassUseCase()
                .onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(gameState = newState, isAiThinking = false)
                    handleRoundEnd(newState)
                    if (!newState.isGameOver) queueAiTurnIfNeeded()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isAiThinking = false, error = e.message)
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ROUND / MATCH END
    // ═══════════════════════════════════════════════════════════════════════
    private fun handleRoundEnd(state: GameState) {
        if (!state.isGameOver) return

        if (state.isMatchOver) {
            _uiState.value = _uiState.value.copy(showMatchResult = true)
            saveMatchResult(state)
        } else {
            _uiState.value = _uiState.value.copy(showRoundResult = true)
        }
    }

    /**
     * FIX #3: Calculate total match duration, not just last round duration
     */
    private fun saveMatchResult(state: GameState) {
        viewModelScope.launch {
            val matchWinnerId = state.matchScore.matchWinnerId ?: return@launch
            val result = GameResult(
                winnerId = matchWinnerId,
                winnerName = state.players.getOrNull(matchWinnerId)?.displayName() ?: "فائز",
                scores = state.matchScore.scores,
                durationSeconds = state.matchScore.roundHistory.sumOf { it.durationSeconds },
                gameMode = state.gameMode
            )
            recordGameUseCase(result, 0)
        }
    }
}
