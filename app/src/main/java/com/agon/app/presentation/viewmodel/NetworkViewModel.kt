package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.model.*
import com.agon.app.domain.repository.NetworkRepository
import com.agon.app.domain.usecase.network.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val createRoomUseCase: CreateRoomUseCase,
    private val discoverRoomsUseCase: DiscoverRoomsUseCase,
    private val joinRoomUseCase: JoinRoomUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val syncGameStateUseCase: SyncGameStateUseCase,
    private val startNetworkGameUseCase: StartNetworkGameUseCase,
    private val observeNetworkEventsUseCase: ObserveNetworkEventsUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    data class NetworkUiState(
        val networkState: NetworkState = NetworkState(),
        val discoveredRooms: List<NetworkRoom> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val statusMessage: String = "غير متصل",
        val gameStarted: Boolean = false,
        val gameState: GameState? = null,
        val chatMessages: List<ChatMessage> = emptyList()
    )

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        observeNetworkEvents()
    }

    fun createRoom(roomName: String) {
        if (roomName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "أدخل اسم الغرفة")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showCreateDialog = false)
            createRoomUseCase(roomName)
                .onSuccess { state ->
                    _uiState.value = _uiState.value.copy(
                        networkState = state, isLoading = false,
                        statusMessage = "تم إنشاء الغرفة $roomName — في انتظار اللاعبين"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message ?: "فشل إنشاء الغرفة"
                    )
                }
        }
    }

    fun discoverRooms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, discoveredRooms = emptyList(),
                statusMessage = "جاري البحث عن غرف..."
            )
            discoverRoomsUseCase()
                .onSuccess { rooms ->
                    _uiState.value = _uiState.value.copy(
                        discoveredRooms = rooms, isLoading = false,
                        statusMessage = if (rooms.isEmpty()) "لا توجد غرف" else "وجدنا ${rooms.size} غرفة"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message ?: "فشل البحث"
                    )
                }
        }
    }

    fun joinRoom(room: NetworkRoom, playerName: String) {
        if (playerName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "أدخل اسمك أولاً")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "جاري الاتصال بـ ${room.name}..."
            )
            joinRoomUseCase(room, playerName)
                .onSuccess { state ->
                    _uiState.value = _uiState.value.copy(
                        networkState = state, isLoading = false,
                        statusMessage = "متصل بـ ${room.name}"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = e.message ?: "فشل الانضمام"
                    )
                }
        }
    }

    fun startGame() {
        val state = _uiState.value.networkState
        if (!state.isHost) {
            _uiState.value = _uiState.value.copy(error = "فقط المضيف يمكنه بدء اللعبة")
            return
        }
        if (state.playerCount < 2) {
            _uiState.value = _uiState.value.copy(error = "يحتاج لاعبين على الأقل")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            startNetworkGameUseCase(state)
                .onSuccess { gameState ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        gameStarted = true,
                        gameState = gameState,
                        statusMessage = "اللعبة بدأت!"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل بدء اللعبة"
                    )
                }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            leaveRoomUseCase()
            _uiState.value = NetworkUiState(statusMessage = "غير متصل")
        }
    }

    fun showCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = true) }
    fun dismissCreateDialog() { _uiState.value = _uiState.value.copy(showCreateDialog = false) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
    fun resetGameStarted() { _uiState.value = _uiState.value.copy(gameStarted = false, gameState = null) }

    fun sendChatMessage(message: String) {
        val playerName = _uiState.value.networkState.connectedPlayers.find {
            it.id == _uiState.value.networkState.localPlayerId
        }?.name ?: "لاعب"

        val chatMessage = ChatMessage(
            senderId = _uiState.value.networkState.localPlayerId,
            senderName = playerName,
            message = message
        )

        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + chatMessage
        )

        viewModelScope.launch {
            networkRepository.sendChatMessage(chatMessage)
        }
    }

    private fun observeNetworkEvents() {
        viewModelScope.launch {
            observeNetworkEventsUseCase().collect { event ->
                when (event) {
                    is NetworkEvent.ChatMessageReceived -> {
                        _uiState.value = _uiState.value.copy(
                            chatMessages = _uiState.value.chatMessages + event.message,
                            statusMessage = "رسالة من ${event.message.senderName}"
                        )
                    }
                    is NetworkEvent.PlayerJoined -> {
                        _uiState.value = _uiState.value.copy(
                            statusMessage = "${event.player.name} انضم للغرفة"
                        )
                    }
                    is NetworkEvent.PlayerLeft -> {
                        _uiState.value = _uiState.value.copy(
                            statusMessage = "غادر لاعب",
                            gameStarted = false
                        )
                    }
                    is NetworkEvent.ConnectionLost -> {
                        _uiState.value = _uiState.value.copy(
                            error = "انقطع الاتصال",
                            gameStarted = false
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
