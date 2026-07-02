package com.agon.app.domain.model

/**
 * Represents the current state of a network game session.
 */
data class NetworkState(
    val isConnected: Boolean = false,
    val isHost: Boolean = false,
    val roomId: String = "",
    val roomName: String = "",
    val connectedPlayers: List<NetworkPlayer> = emptyList(),
    val localPlayerId: String = "",
    val status: NetworkStatus = NetworkStatus.DISCONNECTED,
    val error: String? = null,
    val pingMs: Long = -1,
    val maxPlayers: Int = 4,
    val discoveredRooms: List<NetworkRoom> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList()
) {
    val playerCount: Int
        get() = connectedPlayers.size

    val remainingPlayers: Int
        get() = (maxPlayers - playerCount).coerceAtLeast(0)

    val canStartGame: Boolean
        get() = isHost &&
            playerCount >= 2 &&
            playerCount <= maxPlayers &&
            connectedPlayers.all { it.isReady || it.id == localPlayerId }

    val isOffline: Boolean
        get() = status == NetworkStatus.DISCONNECTED
}

/**
 * Represents a player in a network game.
 */
data class NetworkPlayer(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val pingMs: Long = 0
)

/**
 * Represents a discoverable network room.
 */
data class NetworkRoom(
    val id: String,
    val name: String,
    val hostAddress: String,
    val hostName: String,
    val currentPlayers: Int,
    val port: Int? = null,
    val maxPlayers: Int = 4,
    val isPasswordProtected: Boolean = false
)

/**
 * Enum representing the current network connection status.
 */
enum class NetworkStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    SYNCING,
    RECONNECTING,
    ERROR
}

/**
 * Sealed class for network events.
 */
sealed class NetworkEvent {
    data class PlayerJoined(val player: NetworkPlayer) : NetworkEvent()
    data class PlayerLeft(val playerId: String) : NetworkEvent()
    data class GameStateSync(val state: GameState) : NetworkEvent()
    data class PlayerAction(val action: GameAction) : NetworkEvent()
    data class ChatMessageReceived(val message: ChatMessage) : NetworkEvent()
    data class Error(val message: String) : NetworkEvent()
    object ConnectionLost : NetworkEvent()
    object Reconnected : NetworkEvent()
}
