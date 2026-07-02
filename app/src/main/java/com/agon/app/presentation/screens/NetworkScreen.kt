package com.agon.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.domain.model.ChatMessage
import com.agon.app.domain.model.NetworkRoom
import com.agon.app.domain.model.NetworkState
import com.agon.app.domain.model.NetworkStatus
import com.agon.app.domain.model.NetworkPlayer

// ═══════════════════════════════════════════════════════════════════════════
// THEME COLORS
// ═══════════════════════════════════════════════════════════════════════════
private val DeepGreen = Color(0xFF0F1E14)
private val CardBg = Color(0xFF192D1F)
private val CardBorder = Color(0xFF253525)
private val Gold = Color(0xFFC9A84C)
private val LightGold = Color(0xFFF0CC6E)
private val TextPrimary = Color(0xFFF0E8D0)
private val TextMuted = Color(0xFFA09078)
private val TextDim = Color(0xFF506050)
private val TextDark = Color(0xFF304030)
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    networkState: NetworkState,
    discoveredRooms: List<NetworkRoom>,
    isLoading: Boolean,
    error: String?,
    showCreateDialog: Boolean,
    onCreateRoom: (String) -> Unit,
    onDiscover: () -> Unit,
    onJoinRoom: (NetworkRoom, String) -> Unit,
    onLeaveRoom: () -> Unit,
    onShowCreateDialog: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    statusMessage: String = "",
    onStartGame: () -> Unit = {},
    chatMessages: List<ChatMessage> = emptyList(),
    onSendChatMessage: (String) -> Unit = {}
) {
    var playerName by remember { mutableStateOf("") }
    var roomToJoin by remember { mutableStateOf<NetworkRoom?>(null) }
    var createRoomName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ══ Top Bar ════════════════════════════════════════════════════
            TopAppBar(
                title = {
                    Text(
                        "اللعب عبر الشبكة",
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepGreen.copy(alpha = 0.9f)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ══ How it works card ════════════════════════════════════════
                InfoCard()

                Spacer(Modifier.height(16.dp))

                // ══ Status Bar ═══════════════════════════════════════════════
                StatusBar(networkState, statusMessage)

                Spacer(Modifier.height(16.dp))

                // ══ Error Banner ═════════════════════════════════════════════
                AnimatedVisibility(visible = error != null) {
                    error?.let {
                        ErrorBanner(error = it, onDismiss = onClearError)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // ══ Loading Indicator ════════════════════════════════════════
                AnimatedVisibility(visible = isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Gold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "جاري التحميل...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // ══ Main Content ═══════════════════════════════════════════
                if (networkState.isConnected) {
                    ConnectedView(
                        networkState = networkState,
                        onLeave = onLeaveRoom,
                        onStartGame = onStartGame,
                        chatMessages = chatMessages,
                        onSendChatMessage = onSendChatMessage,
                        localPlayerName = playerName
                    )
                } else {
                    DisconnectedView(
                        playerName = playerName,
                        onPlayerNameChange = { playerName = it },
                        discoveredRooms = discoveredRooms,
                        onShowCreateDialog = onShowCreateDialog,
                        onDiscover = onDiscover,
                        onJoinRoom = { room -> roomToJoin = room },
                        isLoading = isLoading
                    )
                }
            }
        }

        // ══ Create Room Dialog ═══════════════════════════════════════════
        if (showCreateDialog) {
            CreateRoomDialog(
                roomName = createRoomName,
                onRoomNameChange = { createRoomName = it },
                onConfirm = {
                    if (createRoomName.isNotBlank()) {
                        onCreateRoom(createRoomName)
                        createRoomName = ""
                    }
                },
                onDismiss = onDismissCreateDialog
            )
        }

        // ══ Join Room Dialog ═════════════════════════════════════════════
        roomToJoin?.let { room ->
            JoinRoomDialog(
                room = room,
                playerName = playerName,
                onPlayerNameChange = { playerName = it },
                onConfirm = {
                    if (playerName.isNotBlank()) {
                        onJoinRoom(room, playerName)
                        roomToJoin = null
                    }
                },
                onDismiss = { roomToJoin = null }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// INFO CARD
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "كيف يعمل؟",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            InfoStep(icon = "📱", text = "تأكد أن جميع الأجهزة على نفس شبكة WiFi أو Hotspot")
            InfoStep(icon = "🏠", text = "شخص واحد ينشئ غرفة (المضيف)")
            InfoStep(icon = "🔍", text = "الباقون يبحثون وينضمون للغرفة")
            InfoStep(icon = "🎮", text = "المضيف يبدأ اللعبة عند اكتمال اللاعبين")
        }
    }
}

@Composable
private fun InfoStep(icon: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STATUS BAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun StatusBar(networkState: NetworkState, statusMessage: String) {
    val (icon, color, text) = when (networkState.status) {
        NetworkStatus.CONNECTED -> Triple(
            Icons.Default.CheckCircle,
            SuccessGreen,
            statusMessage.ifBlank { "متصل: ${networkState.roomName}" }
        )
        NetworkStatus.CONNECTING -> Triple(
            Icons.Default.Wifi,
            LightGold,
            statusMessage.ifBlank { "جاري الاتصال..." }
        )
        NetworkStatus.SYNCING -> Triple(
            Icons.Default.Refresh,
            LightGold,
            statusMessage.ifBlank { "جاري المزامنة..." }
        )
        NetworkStatus.ERROR -> Triple(
            Icons.Default.WifiOff,
            ErrorRed,
            statusMessage.ifBlank { "خطأ في الاتصال" }
        )
        NetworkStatus.RECONNECTING -> Triple(
            Icons.Default.Wifi,
            LightGold,
            statusMessage.ifBlank { "إعادة الاتصال..." }
        )
        else -> Triple(
            Icons.Default.WifiOff,
            TextMuted,
            statusMessage.ifBlank { "غير متصل" }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ERROR BANNER
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⚠️",
                fontSize = 18.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                error,
                color = ErrorRed,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = ErrorRed)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// DISCONNECTED VIEW
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun DisconnectedView(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    discoveredRooms: List<NetworkRoom>,
    onShowCreateDialog: () -> Unit,
    onDiscover: () -> Unit,
    onJoinRoom: (NetworkRoom) -> Unit,
    isLoading: Boolean
) {
    Column {
        OutlinedTextField(
            value = playerName,
            onValueChange = onPlayerNameChange,
            label = { Text("اسمك في اللعبة", color = TextMuted) },
            placeholder = { Text("أدخل اسمك...", color = TextMuted.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Gold
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onShowCreateDialog,
                modifier = Modifier.weight(1f),
                enabled = !isLoading && playerName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏠 إنشاء غرفة", color = DeepGreen, fontWeight = FontWeight.Bold)
            }

            // FIX: Use OutlinedButton without OutlinedButtonDefaults
            OutlinedButton(
                onClick = onDiscover,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Gold),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Gold
                )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("🔍 بحث")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (discoveredRooms.isNotEmpty()) {
            Text(
                "الغرف المتاحة (${discoveredRooms.size})",
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(discoveredRooms) { room ->
                    RoomCard(
                        room = room,
                        onJoin = { onJoinRoom(room) }
                    )
                }
            }
        } else if (!isLoading) {
            EmptyRoomsView()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// EMPTY ROOMS VIEW
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun EmptyRoomsView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🔍",
                fontSize = 48.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "لا توجد غرف متاحة",
                color = TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "أنشئ غرفة جديدة أو تأكد من اتصال الجميع بنفس الشبكة",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ROOM CARD
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun RoomCard(
    room: NetworkRoom,
    onJoin: () -> Unit
) {
    val isFull = room.currentPlayers >= room.maxPlayers

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Gold.copy(alpha = 0.3f), Gold.copy(alpha = 0.1f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🏠", fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    room.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "المضيف: ${room.hostName}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isFull) ErrorRed else SuccessGreen,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${room.currentPlayers}/${room.maxPlayers} لاعب",
                        color = if (isFull) ErrorRed else TextMuted,
                        fontSize = 13.sp
                    )
                    if (room.isPasswordProtected) {
                        Spacer(Modifier.width(8.dp))
                        Text("🔒", fontSize = 12.sp)
                    }
                }
            }

            Button(
                onClick = onJoin,
                enabled = !isFull,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFull) CardBorder else Gold,
                    disabledContainerColor = CardBorder
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (isFull) "ممتلئة" else "انضمام",
                    color = if (isFull) TextMuted else DeepGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CONNECTED VIEW (LOBBY)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ConnectedView(
    networkState: NetworkState,
    onLeave: () -> Unit,
    onStartGame: () -> Unit,
    chatMessages: List<ChatMessage> = emptyList(),
    onSendChatMessage: (String) -> Unit = {},
    localPlayerName: String = ""
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBg.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (networkState.isHost) "👑" else "🏠",
                        fontSize = 28.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            networkState.roomName,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            if (networkState.isHost) "أنت المضيف" else "لاعب",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = CardBorder)
                Spacer(Modifier.height(16.dp))

                Text(
                    "اللاعبون المتصلون (${networkState.playerCount}):",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))

                networkState.connectedPlayers.forEach { player ->
                    PlayerRow(player = player, isLocal = player.id == networkState.localPlayerId)
                    Spacer(Modifier.height(8.dp))
                }

                if (networkState.isHost && networkState.playerCount < 2) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Gold.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Gold,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "في انتظار اللاعبين... (${networkState.playerCount}/4)",
                                color = LightGold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (networkState.isHost) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onStartGame,
                        enabled = networkState.canStartGame,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold,
                            disabledContainerColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "🎮 ابدأ اللعبة",
                            color = if (networkState.canStartGame) DeepGreen else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Chat Panel
        if (chatMessages.isNotEmpty()) {
            ChatPanel(
                messages = chatMessages,
                onSendMessage = onSendChatMessage,
                localPlayerName = localPlayerName
            )
        }

        Spacer(Modifier.height(16.dp))

        // FIX: Use OutlinedButton without OutlinedButtonDefaults
        OutlinedButton(
            onClick = onLeave,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ErrorRed
            )
        ) {
            Text("❌ مغادرة الغرفة", fontWeight = FontWeight.Medium)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// PLAYER ROW
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun PlayerRow(
    player: NetworkPlayer,
    isLocal: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (player.isHost)
                        Brush.linearGradient(listOf(Gold, LightGold))
                    else
                        Brush.linearGradient(listOf(CardBorder, CardBorder)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (player.isHost) "👑" else "👤",
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    player.name + if (isLocal) " (أنت)" else "",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (player.isReady) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "جاهز",
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (player.pingMs > 0) {
                Text(
                    "${player.pingMs}ms",
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        if (player.isReady) {
            Text(
                "جاهز",
                color = SuccessGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// CHAT PANEL
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ChatPanel(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    localPlayerName: String
) {
    var messageText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("💬", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "الدردشة",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "(${messages.size})",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = true
            ) {
                items(messages.asReversed()) { message ->
                    ChatMessageBubble(
                        message = message,
                        isLocal = message.senderName == localPlayerName
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text("اكتب رسالة...", color = TextMuted.copy(alpha = 0.5f), fontSize = 12.sp)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("إرسال", color = DeepGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isLocal: Boolean
) {
    val backgroundColor = if (isLocal)
        Gold.copy(alpha = 0.15f)
    else
        Color(0xFF1E2E1E)

    val borderColor = if (isLocal)
        Gold.copy(alpha = 0.3f)
    else
        CardBorder

    val textColor = if (isLocal)
        LightGold
    else
        TextPrimary

    val nameColor = if (isLocal)
        Gold
    else
        TextMuted

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLocal) "أنت" else message.senderName,
                    color = nameColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatChatTime(message.timestamp),
                    color = TextDark,
                    fontSize = 9.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = message.message,
                color = textColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

// ═══════════════════════════════════════════════════════════════════════════
// CREATE ROOM DIALOG
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun CreateRoomDialog(
    roomName: String,
    onRoomNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                "إنشاء غرفة جديدة",
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = roomName,
                onValueChange = onRoomNameChange,
                label = { Text("اسم الغرفة", color = TextMuted) },
                placeholder = { Text("مثال: غرفة الأبطال", color = TextMuted.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = roomName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("إنشاء", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// JOIN ROOM DIALOG
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun JoinRoomDialog(
    room: NetworkRoom,
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                "الانضمام إلى ${room.name}",
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "المضيف: ${room.hostName}",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "اللاعبون: ${room.currentPlayers}/${room.maxPlayers}",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = playerName,
                    onValueChange = onPlayerNameChange,
                    label = { Text("اسمك في اللعبة", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Gold
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = playerName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("انضمام", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
