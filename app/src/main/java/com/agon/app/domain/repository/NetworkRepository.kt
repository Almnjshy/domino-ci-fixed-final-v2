package com.agon.app.domain.repository

import com.agon.app.domain.model.NetworkEvent
import com.agon.app.domain.model.NetworkRoom
import com.agon.app.domain.model.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for network/multiplayer operations
 */
interface NetworkRepository {

    /** Current network state */
    val networkState: StateFlow<NetworkState>

    /** Stream of network events */
    val events: Flow<NetworkEvent>

    /**
     * Create a new game room
     */
    suspend fun createRoom(roomName: String, maxPlayers: Int = 4): Result<NetworkState>

    /**
     * Discover available rooms
     */
    suspend fun discoverRooms(): Result<List<NetworkRoom>>

    /**
     * Join an existing room
     */
    suspend fun joinRoom(room: NetworkRoom, playerName: String): Result<NetworkState>

    /**
     * Leave current room
     */
    suspend fun leaveRoom(): Result<Unit>

    /**
     * Send game action to other players
     */
    suspend fun sendGameAction(action: com.agon.app.domain.model.GameAction): Result<Unit>

    /**
     * Sync game state with other players
     */
    suspend fun syncGameState(state: com.agon.app.domain.model.GameState): Result<Unit>

    /**
     * Start the game (host only)
     */
    suspend fun startGame(): Result<Unit>

    /**
     * Disconnect from network
     */
    suspend fun disconnect(): Result<Unit>

    /**
     * Reconnect to last room
     */
    suspend fun reconnect(): Result<NetworkState>

    /**
     * Set player ready status
     */
    suspend fun setReady(isReady: Boolean): Result<Unit>

    /**
     * Measure network ping/latency
     * @return ping in milliseconds, or -1 if not connected
     */
    suspend fun measurePing(): Long

    /**
     * Send chat message to all players
     */
    suspend fun sendChatMessage(message: com.agon.app.domain.model.ChatMessage): Result<Unit>
}
