package com.agon.app.data.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.agon.app.domain.model.GameAction
import com.agon.app.domain.model.GameState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket

/**
 * HOST side: registers NSD service + runs TCP server.
 * Other players on the same WiFi/Hotspot discover and connect.
 *
 * Protocol (newline-delimited JSON over TCP):
 *   { "type": "ACTION", "data": { ... GameAction ... } }
 *   { "type": "STATE",  "data": { ... GameState ... } }
 *   { "type": "PING" }
 *   { "type": "PONG" }
 *   { "type": "PLAYER_JOINED", "name": "..." }
 */
class NsdGameServer(
    private val context: Context,
    private val roomName: String,
    private val maxPlayers: Int = 4
) {
    companion object {
        const val SERVICE_TYPE = "_domino._tcp."
        const val PORT = 47832
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null

    private val _events = MutableSharedFlow<NetworkPacket>(extraBufferCapacity = 128)
    val events: SharedFlow<NetworkPacket> = _events.asSharedFlow()

    // All connected client sockets
    private val clients = mutableMapOf<String, ClientConnection>()

    // ── Start server ───────────────────────────────
    suspend fun start(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            serverSocket = ServerSocket(PORT).also { it.reuseAddress = true }
            val port = serverSocket!!.localPort
            registerNsdService(port)
            acceptClients()
            port
        }
    }

    // ── Broadcast to all clients ───────────────────
    fun broadcastState(state: GameState) {
        val json = JSONObject().apply {
            put("type", "STATE")
            put("data", GameStateSerializer.toJson(state))
        }.toString()
        clients.values.forEach { it.send(json) }
    }

    fun broadcastAction(action: GameAction, senderId: String) {
        val json = JSONObject().apply {
            put("type", "ACTION")
            put("sender", senderId)
            put("data", GameActionSerializer.toJson(action))
        }.toString()
        // Relay to all OTHER clients (not sender)
        clients.filter { it.key != senderId }.values.forEach { it.send(json) }
    }

    // NEW: Broadcast chat message to all clients
    fun broadcastChatMessage(senderId: String, senderName: String, message: String, timestamp: Long) {
        val json = JSONObject().apply {
            put("type", "CHAT")
            put("senderId", senderId)
            put("senderName", senderName)
            put("message", message)
            put("timestamp", timestamp)
        }.toString()
        // Broadcast to ALL clients including sender (so everyone sees it)
        clients.values.forEach { it.send(json) }
    }

    // NEW: Broadcast ping to all clients
    fun broadcastPing(senderId: String, timestamp: Long) {
        val json = JSONObject().apply {
            put("type", "PING")
            put("id", senderId)
            put("timestamp", timestamp)
        }.toString()
        clients.values.forEach { it.send(json) }
    }

    // ── Stop server ────────────────────────────────
    fun stop() {
        unregisterNsdService()
        clients.values.forEach { it.close() }
        clients.clear()
        serverSocket?.close()
        scope.cancel()
    }

    // ── NSD registration ───────────────────────────
    private fun registerNsdService(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = roomName
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                scope.launch { _events.emit(NetworkPacket.ServerReady(info.serviceName, port)) }
            }
            override fun onRegistrationFailed(info: NsdServiceInfo, code: Int) {
                scope.launch { _events.emit(NetworkPacket.Error("NSD registration failed: $code")) }
            }
            override fun onServiceUnregistered(info: NsdServiceInfo) {}
            override fun onUnregistrationFailed(info: NsdServiceInfo, code: Int) {}
        }

        nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).also {
            it.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener!!)
        }
    }

    private fun unregisterNsdService() {
        registrationListener?.let { nsdManager?.unregisterService(it) }
    }

    // ── Accept incoming TCP connections ────────────
    private fun acceptClients() {
        scope.launch {
            while (isActive) {
                try {
                    val socket = serverSocket?.accept() ?: break
                    val clientId = "client_${System.currentTimeMillis()}"
                    val conn = ClientConnection(clientId, socket, scope, _events)
                    clients[clientId] = conn
                    conn.startReading()
                    _events.emit(NetworkPacket.PlayerConnected(clientId, socket.inetAddress.hostAddress ?: "unknown"))
                } catch (e: Exception) {
                    if (isActive) _events.emit(NetworkPacket.Error("Accept error: ${e.message}"))
                    break
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────
// CLIENT side: discovers room via NSD, connects TCP
// ─────────────────────────────────────────────────
class NsdGameClient(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null

    private val _events = MutableSharedFlow<NetworkPacket>(extraBufferCapacity = 128)
    val events: SharedFlow<NetworkPacket> = _events.asSharedFlow()

    private val _discoveredRooms = MutableSharedFlow<DiscoveredRoom>(extraBufferCapacity = 32)
    val discoveredRooms: SharedFlow<DiscoveredRoom> = _discoveredRooms.asSharedFlow()

    // ── Discover rooms on same WiFi ────────────────
    fun startDiscovery() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) {}
            override fun onDiscoveryStopped(type: String) {}
            override fun onStartDiscoveryFailed(type: String, code: Int) {
                scope.launch { _events.emit(NetworkPacket.Error("Discovery failed: $code")) }
            }
            override fun onStopDiscoveryFailed(type: String, code: Int) {}

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType == NsdGameServer.SERVICE_TYPE) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                scope.launch { _events.emit(NetworkPacket.RoomLost(service.serviceName)) }
            }
        }
        nsdManager?.discoverServices(NsdGameServer.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener!!)
    }

    fun stopDiscovery() {
        discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
    }

    // ── Resolve found service to get IP + port ─────
    private fun resolveService(service: NsdServiceInfo) {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, code: Int) {}
            override fun onServiceResolved(info: NsdServiceInfo) {
                scope.launch {
                    _discoveredRooms.emit(
                        DiscoveredRoom(
                            name = info.serviceName,
                            host = info.host.hostAddress ?: return@launch,
                            port = info.port
                        )
                    )
                }
            }
        }
        nsdManager?.resolveService(service, resolveListener!!)
    }

    // ── Connect to a specific room ─────────────────
    suspend fun connect(host: String, port: Int, playerName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val s = Socket(host, port)
                socket = s
                writer = BufferedWriter(OutputStreamWriter(s.getOutputStream()))

                // Send join packet
                send(JSONObject().apply {
                    put("type", "PLAYER_JOINED")
                    put("name", playerName)
                }.toString())

                startReading(s)
            }
        }

    fun sendAction(action: GameAction) {
        val json = JSONObject().apply {
            put("type", "ACTION")
            put("data", GameActionSerializer.toJson(action))
        }.toString()
        send(json)
    }

    // NEW: Send chat message to server
    fun sendChatMessage(senderId: String, senderName: String, message: String, timestamp: Long) {
        val json = JSONObject().apply {
            put("type", "CHAT")
            put("senderId", senderId)
            put("senderName", senderName)
            put("message", message)
            put("timestamp", timestamp)
        }.toString()
        send(json)
    }

    // NEW: Send ping to server
    fun sendPing(senderId: String, timestamp: Long) {
        val json = JSONObject().apply {
            put("type", "PING")
            put("id", senderId)
            put("timestamp", timestamp)
        }.toString()
        send(json)
    }

    fun disconnect() {
        socket?.close()
        stopDiscovery()
        scope.cancel()
    }

    private fun send(message: String) {
        scope.launch {
            try {
                writer?.apply { write(message); newLine(); flush() }
            } catch (e: Exception) {
                _events.emit(NetworkPacket.Error("Send error: ${e.message}"))
            }
        }
    }

    private fun startReading(socket: Socket) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    parseAndEmit(line)
                }
            } catch (e: Exception) {
                _events.emit(NetworkPacket.ConnectionLost)
            }
        }
    }

    private suspend fun parseAndEmit(json: String) {
        try {
            val obj = JSONObject(json)
            when (obj.getString("type")) {
                "STATE" -> {
                    val state = GameStateSerializer.fromJson(obj.getJSONObject("data"))
                    _events.emit(NetworkPacket.StateSync(state))
                }
                "ACTION" -> {
                    val action = GameActionSerializer.fromJson(obj.getJSONObject("data"))
                    if (action != null) _events.emit(NetworkPacket.ActionReceived(action))
                }
                "PING" -> {
                    // Respond with pong including original timestamp
                    val pongJson = JSONObject().apply {
                        put("type", "PONG")
                        put("id", obj.optString("id", ""))
                        put("originalTimestamp", obj.optLong("timestamp", 0))
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    send(pongJson)
                }
                "PONG" -> {
                    _events.emit(NetworkPacket.Pong(
                        id = obj.optString("id", ""),
                        originalTimestamp = obj.optLong("originalTimestamp", 0),
                        timestamp = obj.optLong("timestamp", 0)
                    ))
                }
                "CHAT" -> {
                    _events.emit(NetworkPacket.ChatMessage(
                        senderId = obj.optString("senderId", ""),
                        senderName = obj.optString("senderName", "لاعب"),
                        message = obj.optString("message", ""),
                        timestamp = obj.optLong("timestamp", 0)
                    ))
                }
                "PLAYER_JOINED" -> _events.emit(NetworkPacket.PlayerConnected(
                    obj.optString("id", ""), obj.optString("name", "لاعب")
                ))
            }
        } catch (e: Exception) { /* malformed packet, skip */ }
    }
}

// ─────────────────────────────────────────────────
// Client connection managed by server
// ─────────────────────────────────────────────────
class ClientConnection(
    val id: String,
    private val socket: Socket,
    private val scope: CoroutineScope,
    private val events: MutableSharedFlow<NetworkPacket>
) {
    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    fun send(message: String) {
        scope.launch {
            try { writer.write(message); writer.newLine(); writer.flush() }
            catch (e: Exception) { events.emit(NetworkPacket.PlayerDisconnected(id)) }
        }
    }

    fun startReading() {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    parseAndEmit(line)
                }
            } catch (e: Exception) {
                events.emit(NetworkPacket.PlayerDisconnected(id))
            }
        }
    }

    private suspend fun parseAndEmit(json: String) {
        try {
            val obj = JSONObject(json)
            when (obj.getString("type")) {
                "ACTION" -> {
                    val action = GameActionSerializer.fromJson(obj.getJSONObject("data"))
                    if (action != null) events.emit(NetworkPacket.ActionReceived(action, senderId = id))
                }
                "PLAYER_JOINED" -> events.emit(NetworkPacket.PlayerConnected(id, obj.optString("name", "لاعب")))
                "PING" -> {
                    // Respond with pong including original timestamp
                    val pongJson = JSONObject().apply {
                        put("type", "PONG")
                        put("id", obj.optString("id", ""))
                        put("originalTimestamp", obj.optLong("timestamp", 0))
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    send(pongJson)
                }
                "PONG" -> {
                    events.emit(NetworkPacket.Pong(
                        id = obj.optString("id", ""),
                        originalTimestamp = obj.optLong("originalTimestamp", 0),
                        timestamp = obj.optLong("timestamp", 0)
                    ))
                }
                "CHAT" -> {
                    events.emit(NetworkPacket.ChatMessage(
                        senderId = obj.optString("senderId", ""),
                        senderName = obj.optString("senderName", "لاعب"),
                        message = obj.optString("message", ""),
                        timestamp = obj.optLong("timestamp", 0)
                    ))
                }
            }
        } catch (e: Exception) { /* skip */ }
    }

    fun close() { runCatching { socket.close() } }
}

// ─────────────────────────────────────────────────
// Data classes
// ─────────────────────────────────────────────────
data class DiscoveredRoom(val name: String, val host: String, val port: Int)

sealed class NetworkPacket {
    data class ServerReady(val name: String, val port: Int) : NetworkPacket()
    data class PlayerConnected(val id: String, val name: String) : NetworkPacket()
    data class PlayerDisconnected(val id: String) : NetworkPacket()
    data class StateSync(val state: GameState) : NetworkPacket()
    data class ActionReceived(val action: GameAction, val senderId: String = "") : NetworkPacket()
    data class Error(val message: String) : NetworkPacket()
    data class RoomLost(val name: String) : NetworkPacket()
    object ConnectionLost : NetworkPacket()
    object Reconnected : NetworkPacket()

    // NEW: Ping/Pong for latency measurement
    data class Ping(val id: String, val timestamp: Long) : NetworkPacket()
    data class Pong(val id: String, val originalTimestamp: Long, val timestamp: Long) : NetworkPacket()

    // NEW: Chat message
    data class ChatMessage(
        val senderId: String,
        val senderName: String,
        val message: String,
        val timestamp: Long
    ) : NetworkPacket()
}
