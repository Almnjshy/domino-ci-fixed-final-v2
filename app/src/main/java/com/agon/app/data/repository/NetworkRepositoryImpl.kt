package com.agon.app.data.repository

import android.content.Context
import com.agon.app.data.network.*
import com.agon.app.domain.model.*
import com.agon.app.domain.repository.NetworkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real NetworkRepository using NSD (Network Service Discovery) + TCP Sockets.
 * Works over WiFi or Mobile Hotspot — no internet required.
 */
@Singleton
class NetworkRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networkState = MutableStateFlow(NetworkState())
    override val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _events = MutableSharedFlow<NetworkEvent>(extraBufferCapacity = 128)
    override val events: Flow<NetworkEvent> = _events.asSharedFlow()

    private val _discoveredRooms = MutableStateFlow<List<NetworkRoom>>(emptyList())

    // Active server/client
    private var server: NsdGameServer? = null
    private var client: NsdGameClient? = null
    private var localPlayerId: String = ""
    private var localPlayerIndex: Int = 0

    // ── HOST: Create room ──────────────────────────
    override suspend fun createRoom(roomName: String, maxPlayers: Int): Result<NetworkState> {
        return try {
            val actualMaxPlayers = maxPlayers.coerceIn(2, 4)
            val srv = NsdGameServer(context, roomName, actualMaxPlayers)
            server = srv

            scope.launch {
                srv.events.collect { packet -> handleServerPacket(packet, maxPlayers) }
            }

            val portResult = srv.start()
            if (portResult.isFailure) return Result.failure(portResult.exceptionOrNull()!!)

            localPlayerId = "host_${System.currentTimeMillis()}"
            localPlayerIndex = 0

            val hostPlayer = NetworkPlayer(
                id = localPlayerId, name = "المضيف", isHost = true, isReady = true
            )
            val newState = NetworkState(
                isConnected = true, isHost = true,
                roomId = roomName, roomName = roomName,
                connectedPlayers = listOf(hostPlayer),
                localPlayerId = localPlayerId,
                status = NetworkStatus.CONNECTED,
                maxPlayers = actualMaxPlayers
            )
            _networkState.value = newState
            Result.success(newState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── CLIENT: Discover rooms ─────────────────────
    override suspend fun discoverRooms(): Result<List<NetworkRoom>> {
        return try {
            _networkState.value = _networkState.value.copy(status = NetworkStatus.SYNCING)
            val cl = NsdGameClient(context)

            val found = mutableListOf<NetworkRoom>()
            val job = scope.launch {
                cl.discoveredRooms.collect { room ->
                    val networkRoom = NetworkRoom(
                        id = room.name,
                        name = room.name,
                        hostAddress = room.host,
                        hostName = "مضيف",
                        currentPlayers = 1,
                        maxPlayers = 4,
                        port = room.port
                    )
                    if (found.none { it.id == networkRoom.id }) {
                        found.add(networkRoom)
                        _discoveredRooms.value = found.toList()
                    }
                }
            }

            cl.startDiscovery()
            delay(3000)
            cl.stopDiscovery()
            job.cancel()

            _networkState.value = _networkState.value.copy(status = NetworkStatus.DISCONNECTED)
            Result.success(_discoveredRooms.value)
        } catch (e: Exception) {
            _networkState.value = _networkState.value.copy(status = NetworkStatus.ERROR, error = e.message)
            Result.failure(e)
        }
    }

    // ── CLIENT: Join room ──────────────────────────
    override suspend fun joinRoom(room: NetworkRoom, playerName: String): Result<NetworkState> {
        return try {
            _networkState.value = _networkState.value.copy(status = NetworkStatus.CONNECTING)
            val cl = NsdGameClient(context)
            client = cl

            scope.launch {
                cl.events.collect { packet -> handleClientPacket(packet) }
            }

            val port = room.port ?: NsdGameServer.PORT
            val connectResult = cl.connect(room.hostAddress, port, playerName)
            if (connectResult.isFailure) return Result.failure(connectResult.exceptionOrNull()!!)

            localPlayerId = "client_${System.currentTimeMillis()}"
            val localPlayer = NetworkPlayer(
                id = localPlayerId, name = playerName, isHost = false
            )
            val newState = NetworkState(
                isConnected = true, isHost = false,
                roomId = room.id, roomName = room.name,
                connectedPlayers = listOf(localPlayer),
                localPlayerId = localPlayerId,
                status = NetworkStatus.CONNECTED
            )
            _networkState.value = newState
            _events.emit(NetworkEvent.PlayerJoined(localPlayer))
            Result.success(newState)
        } catch (e: Exception) {
            _networkState.value = _networkState.value.copy(status = NetworkStatus.ERROR, error = e.message)
            Result.failure(e)
        }
    }

    // ── Send game action over network ──────────────
    override suspend fun sendGameAction(action: GameAction): Result<Unit> {
        return try {
            if (_networkState.value.isHost) {
                server?.broadcastAction(action, localPlayerId)
            } else {
                client?.sendAction(action)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── HOST: Sync full game state to all clients ──
    override suspend fun syncGameState(state: GameState): Result<Unit> {
        return try {
            if (_networkState.value.isHost) {
                server?.broadcastState(state)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveRoom(): Result<Unit> {
        return try {
            server?.stop(); server = null
            client?.disconnect(); client = null
            _networkState.value = NetworkState()
            _events.emit(NetworkEvent.ConnectionLost)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startGame(): Result<Unit> {
        if (!_networkState.value.isHost) return Result.failure(IllegalStateException("فقط المضيف يمكنه بدء اللعبة"))
        return Result.success(Unit)
    }

    override suspend fun disconnect(): Result<Unit> = leaveRoom()

    // FIX #1: Return Result<NetworkState> instead of Result<Unit>
    override suspend fun reconnect(): Result<NetworkState> {
        _networkState.value = _networkState.value.copy(status = NetworkStatus.RECONNECTING)
        return Result.failure(UnsupportedOperationException("أعد الاتصال يدوياً"))
    }

    override suspend fun setReady(isReady: Boolean): Result<Unit> {
        val state = _networkState.value
        val updated = state.connectedPlayers.map {
            if (it.id == state.localPlayerId) it.copy(isReady = isReady) else it
        }
        _networkState.value = state.copy(connectedPlayers = updated)
        return Result.success(Unit)
    }

    // ── Event handlers ─────────────────────────────

    private suspend fun handleServerPacket(packet: NetworkPacket, maxPlayers: Int) {
        when (packet) {
            is NetworkPacket.PlayerConnected -> {
                val newPlayer = NetworkPlayer(
                    id = packet.id, name = packet.name, isHost = false
                )
                val current = _networkState.value
                val updatedPlayers = current.connectedPlayers + newPlayer
                _networkState.value = current.copy(connectedPlayers = updatedPlayers)
                _events.emit(NetworkEvent.PlayerJoined(newPlayer))
            }
            is NetworkPacket.PlayerDisconnected -> {
                val current = _networkState.value
                val updated = current.connectedPlayers.filter { it.id != packet.id }
                _networkState.value = current.copy(connectedPlayers = updated)
                _events.emit(NetworkEvent.PlayerLeft(packet.id))
            }
            is NetworkPacket.ActionReceived -> {
                _events.emit(NetworkEvent.PlayerAction(packet.action))
            }
            is NetworkPacket.Ping -> {
                server?.broadcastPing(localPlayerId, packet.timestamp)
            }
            is NetworkPacket.Pong -> {
                val rtt = System.currentTimeMillis() - packet.originalTimestamp
                updatePlayerPing(packet.id, rtt)
            }
            is NetworkPacket.ChatMessage -> {
                val chatMessage = ChatMessage(
                    senderId = packet.senderId,
                    senderName = packet.senderName,
                    message = packet.message,
                    timestamp = packet.timestamp
                )
                _events.emit(NetworkEvent.ChatMessageReceived(chatMessage))
            }
            is NetworkPacket.Error -> {
                _networkState.value = _networkState.value.copy(error = packet.message)
                _events.emit(NetworkEvent.Error(packet.message))
            }
            else -> {}
        }
    }

    private suspend fun handleClientPacket(packet: NetworkPacket) {
        when (packet) {
            is NetworkPacket.StateSync -> {
                _events.emit(NetworkEvent.GameStateSync(packet.state))
            }
            is NetworkPacket.ActionReceived -> {
                _events.emit(NetworkEvent.PlayerAction(packet.action))
            }
            is NetworkPacket.Ping -> {}
            is NetworkPacket.Pong -> {
                val rtt = System.currentTimeMillis() - packet.originalTimestamp
                updatePlayerPing(localPlayerId, rtt)
            }
            is NetworkPacket.ChatMessage -> {
                val chatMessage = ChatMessage(
                    senderId = packet.senderId,
                    senderName = packet.senderName,
                    message = packet.message,
                    timestamp = packet.timestamp
                )
                _events.emit(NetworkEvent.ChatMessageReceived(chatMessage))
            }
            is NetworkPacket.ConnectionLost -> {
                _networkState.value = _networkState.value.copy(
                    isConnected = false, status = NetworkStatus.ERROR, error = "انقطع الاتصال"
                )
                _events.emit(NetworkEvent.ConnectionLost)
            }
            is NetworkPacket.Error -> {
                _networkState.value = _networkState.value.copy(error = packet.message)
                _events.emit(NetworkEvent.Error(packet.message))
            }
            else -> {}
        }
    }

    // ── Ping Measurement ───────────────────────────
    override suspend fun measurePing(): Long {
        return try {
            if (!_networkState.value.isConnected) return -1

            val startTime = System.currentTimeMillis()

            if (_networkState.value.isHost) {
                server?.broadcastPing(localPlayerId, startTime)
            } else {
                client?.sendPing(localPlayerId, startTime)
            }

            kotlinx.coroutines.delay(100)

            val elapsed = System.currentTimeMillis() - startTime
            updatePlayerPing(localPlayerId, elapsed)

            elapsed
        } catch (e: Exception) {
            -1
        }
    }

    private fun updatePlayerPing(playerId: String, pingMs: Long) {
        val current = _networkState.value
        val updatedPlayers = current.connectedPlayers.map { player ->
            if (player.id == playerId) player.copy(pingMs = pingMs) else player
        }
        _networkState.value = current.copy(connectedPlayers = updatedPlayers)
    }

    // ── Chat Message ───────────────────────────────
    override suspend fun sendChatMessage(message: ChatMessage): Result<Unit> {
        return try {
            if (!_networkState.value.isConnected) {
                return Result.failure(IllegalStateException("غير متصل"))
            }

            if (_networkState.value.isHost) {
                server?.broadcastChatMessage(
                    message.senderId,
                    message.senderName,
                    message.message,
                    message.timestamp
                )
                _events.emit(NetworkEvent.ChatMessageReceived(message))
            } else {
                client?.sendChatMessage(
                    message.senderId,
                    message.senderName,
                    message.message,
                    message.timestamp
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
