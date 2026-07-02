package com.agon.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.domain.model.*
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════
// THEME — Matching React Design Exactly
// ═══════════════════════════════════════════════════════════════════════════
private val DeepGreen = Color(0xFF0F1E14) // Background
private val CardBg = Color(0xFF192D1F) // Board / Cards
private val CardBorder = Color(0xFF253525) // Card borders
private val Gold = Color(0xFFC9A84C) // Primary accent
private val LightGold = Color(0xFFF0CC6E) // Light gold
private val TextPrimary = Color(0xFFF0E8D0) // Main text
private val TextMuted = Color(0xFFA09078) // Secondary text
private val TextDim = Color(0xFF506050) // Dim text
private val TextDark = Color(0xFF304030) // Very dim
private val Ivory = Color(0xFFFFF8F0) // Domino bg
private val IvoryDark = Color(0xFFF0E8D8) // Shadow side
private val DominoDot = Color(0xFF1C1C1E) // Black dots
private val DominoBorder = Color(0xFF504840) // Tile border
private val SuccessGreen = Color(0xFF4CAF50) // Success
private val ErrorRed = Color(0xFFE57373) // Error
private val WaitingBlue = Color(0xFF4A9EFF) // Waiting

// Player colors matching React
private val PlayerColors = listOf(
    Color(0xFF4A9EFF), // Blue
    Color(0xFF52C87A), // Green
    Color(0xFFFF9F4A), // Orange
    Color(0xFFE05252) // Red
)

// ═══════════════════════════════════════════════════════════════════════════
// MAIN GAME SCREEN
// ═══════════════════════════════════════════════════════════════════════════
@Composable
fun GameScreen(
    gameState: GameState,
    isAiThinking: Boolean,
    showResult: Boolean,
    error: String?,
    // NEW: Network params
    isNetworkGame: Boolean = false,
    isMyTurn: Boolean = true,
    networkStatus: String = "",
    pingMs: Long = 0,
    onTileClick: (DominoTile, BoardSide?) -> Unit,
    onDrawOrPass: () -> Unit,
    legalSides: (DominoTile) -> Set<BoardSide>,
    onNewGame: () -> Unit,
    onBackToMenu: () -> Unit,
    onDismissResult: () -> Unit,
    onClearError: () -> Unit
) {
    var selectedTile by remember { mutableStateOf<DominoTile?>(null) }
    var showHand by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            GameHeader(
                round = gameState.matchScore.currentRound,
                stockCount = gameState.stockCount,
                onBack = onBackToMenu,
                isNetworkGame = isNetworkGame,
                pingMs = pingMs
            )

            // NEW: Network Status Bar
            if (isNetworkGame) {
                NetworkStatusBar(
                    isMyTurn = isMyTurn,
                    networkStatus = networkStatus
                )
            }

            // ── Scores ──
            ScoreBar(
                players = gameState.players,
                matchScore = gameState.matchScore,
                currentPlayerIndex = gameState.currentPlayerIndex
            )

            // ── Status ──
            StatusText(
                message = gameState.message,
                isAiThinking = isAiThinking,
                currentPlayerName = gameState.currentPlayer?.name ?: "",
                isNetworkGame = isNetworkGame,
                isMyTurn = isMyTurn
            )

            // ── Other Players Hands (Hidden) ──
            OtherPlayersHands(
                players = gameState.players,
                currentPlayerIndex = gameState.currentPlayerIndex,
                showHand = showHand
            )

            // ── Board ──
            GameBoard(
                boardState = gameState.board,
                stockCount = gameState.stockCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // ── Side Selector ──
            AnimatedVisibility(visible = selectedTile != null && isMyTurn) {
                selectedTile?.let { tile ->
                    SideSelector(
                        tile = tile,
                        onLeft = { onTileClick(tile, BoardSide.LEFT); selectedTile = null },
                        onRight = { onTileClick(tile, BoardSide.RIGHT); selectedTile = null },
                        onCancel = { selectedTile = null }
                    )
                }
            }

            // ── Error ──
            if (error != null) {
                ErrorBanner(error = error, onDismiss = onClearError)
            }

            // ── Player Hand ──
            val currentPlayer = gameState.currentPlayer
            if (currentPlayer != null && !currentPlayer.isAi && !gameState.isGameOver) {
                // In network mode, only show hand if it's our turn or we're the player
                val canInteract = !isNetworkGame || isMyTurn

                PlayerHand(
                    player = currentPlayer,
                    playerIndex = gameState.currentPlayerIndex,
                    legalSides = legalSides,
                    selectedTile = selectedTile,
                    onTileClick = { tile ->
                        if (!canInteract) return@PlayerHand
                        val sides = legalSides(tile)
                        when {
                            sides.isEmpty() -> Unit
                            sides.size == 1 -> onTileClick(tile, sides.first())
                            else -> selectedTile = if (selectedTile?.id == tile.id) null else tile
                        }
                    },
                    onDrawOrPass = onDrawOrPass,
                    canDraw = gameState.canDraw,
                    stockCount = gameState.stockCount,
                    canInteract = canInteract
                )
            } else if (isAiThinking) {
                AiThinkingBar(currentPlayerName = gameState.currentPlayer?.name ?: "AI")
            } else if (isNetworkGame && !isMyTurn && !gameState.isGameOver) {
                // Show waiting indicator when it's not our turn in network mode
                WaitingBar(networkStatus = networkStatus)
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Round Result Dialog ──
        if (showResult && gameState.isGameOver && !gameState.isMatchOver) {
            RoundResultDialog(
                gameState = gameState,
                onNextRound = onDismissResult,
                onQuit = onBackToMenu
            )
        }

        // ── Match Over Dialog ──
        if (gameState.isMatchOver) {
            MatchResultDialog(
                gameState = gameState,
                onRematch = onNewGame,
                onQuit = onBackToMenu
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// NETWORK STATUS BAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun NetworkStatusBar(
    isMyTurn: Boolean,
    networkStatus: String
) {
    val backgroundColor = when {
        isMyTurn -> Gold.copy(alpha = 0.15f)
        networkStatus.contains("انتظار") -> WaitingBlue.copy(alpha = 0.15f)
        else -> Color(0xFF2A3A2E)
    }

    val borderColor = when {
        isMyTurn -> Gold
        networkStatus.contains("انتظار") -> WaitingBlue
        else -> CardBorder
    }

    val textColor = when {
        isMyTurn -> LightGold
        networkStatus.contains("انتظار") -> WaitingBlue
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        when {
                            isMyTurn -> SuccessGreen
                            networkStatus.contains("انتظار") -> WaitingBlue
                            else -> ErrorRed
                        },
                        CircleShape
                    )
            )
            Spacer(Modifier.width(8.dp))

            val statusText = when {
                isMyTurn -> "🎯 دورك! العب الآن"
                networkStatus.isNotBlank() -> networkStatus
                else -> "⏳ في انتظار دورك..."
            }

            Text(
                text = statusText,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// WAITING BAR (Network mode, not my turn)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun WaitingBar(networkStatus: String) {
    var dotCount by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, WaitingBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = WaitingBlue,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${networkStatus.ifBlank { "في انتظار دورك" }}${".".repeat(dotCount)}",
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun GameHeader(
    round: Int,
    stockCount: Int,
    onBack: () -> Unit,
    isNetworkGame: Boolean = false,
    pingMs: Long = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, Gold.copy(alpha = 0.13f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "دومينو",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = Gold,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Text(
            text = "جولة $round",
            color = TextDim,
            fontSize = 10.sp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "مخزون:$stockCount",
            color = TextDim,
            fontSize = 10.sp
        )

        // NEW: Network ping indicator
        if (isNetworkGame) {
            Spacer(Modifier.width(8.dp))
            ConnectionQualityIndicator(pingMs = pingMs)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SCORE BAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ScoreBar(
    players: List<Player>,
    matchScore: MatchScore,
    currentPlayerIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        players.forEachIndexed { index, player ->
            val isCurrent = index == currentPlayerIndex
            val color = PlayerColors[index % PlayerColors.size]
            val pct = (matchScore.progressPercent(player.id) * 100).toInt()

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) color.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) color else Color(0xFF1E2E2E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (player.isAi) "🤖 ${player.name}" else player.name,
                            fontSize = 10.sp,
                            color = if (isCurrent) color else TextMuted,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${matchScore.playerScore(player.id)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(pct.dp)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                    }

                    Text(
                        text = "${matchScore.playerRoundsWon(player.id)} جولة",
                        fontSize = 9.sp,
                        color = TextDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STATUS TEXT
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun StatusText(
    message: String,
    isAiThinking: Boolean,
    currentPlayerName: String,
    isNetworkGame: Boolean = false,
    isMyTurn: Boolean = true
) {
    val displayText = when {
        isAiThinking -> "🤔 $currentPlayerName يفكر..."
        isNetworkGame && !isMyTurn -> "⏳ في انتظار ${currentPlayerName}..."
        else -> message
    }

    Text(
        text = displayText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        color = if (isAiThinking || (isNetworkGame && !isMyTurn)) TextMuted else LightGold,
        fontWeight = FontWeight.SemiBold,
        minLines = 1
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// OTHER PLAYERS HANDS (Hidden/Face-down)
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun OtherPlayersHands(
    players: List<Player>,
    currentPlayerIndex: Int,
    showHand: Boolean
) {
    Column {
        players.forEachIndexed { index, player ->
            if (index == currentPlayerIndex) return@forEachIndexed

            val isCurrent = index == currentPlayerIndex
            val color = PlayerColors[index % PlayerColors.size]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .alpha(if (isCurrent) 1f else 0.45f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot indicator
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            if (isCurrent) color else Color(0xFF2A3A2E),
                            CircleShape
                        )
                )

                Spacer(Modifier.width(5.dp))

                Text(
                    text = "${if (player.isAi) "🤖 " else ""}${player.name} (${player.hand.size})",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.width(85.dp)
                )

                // Hidden tiles - FIXED: consistent size
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(player.hand.size) {
                        HiddenTileSmall()
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${player.handValue}",
                    fontSize = 9.sp,
                    color = TextDark
                )
            }
        }
    }
}

// FIXED: HiddenTileSmall with consistent size
@Composable
private fun HiddenTileSmall() {
    Box(
        modifier = Modifier
            .size(width = 20.dp, height = 34.dp)  // Fixed consistent size
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2A2A3A), Color(0xFF1A1A2A)),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 34f)
                ),
                RoundedCornerShape(4.dp)
            )
            .border(0.5.dp, Color(0xFF3A3A4E), RoundedCornerShape(4.dp))
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// GAME BOARD
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun GameBoard(
    boardState: BoardState,
    stockCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (boardState.isEmpty) {
                EmptyBoardMessage()
            } else {
                BoardContent(boardState = boardState, stockCount = stockCount)
            }
        }
    }
}

@Composable
private fun EmptyBoardMessage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DominoTileView(
            tile = DominoTile(0, 0),
            isPlaceholder = true,
            modifier = Modifier.size(width = 48.dp, height = 80.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "اللوحة فارغة — ابدأ اللعب",
            color = TextDark,
            fontSize = 12.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
private fun BoardContent(boardState: BoardState, stockCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Board ends
        if (!boardState.isEmpty) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← ${boardState.leftEnd}",
                    fontSize = 11.sp,
                    color = Gold
                )
                Text(
                    text = "${boardState.tiles.size} قطعة",
                    fontSize = 9.sp,
                    color = TextDark
                )
                Text(
                    text = "${boardState.rightEnd} →",
                    fontSize = 11.sp,
                    color = Gold
                )
            }
        }

        // Tiles row - FIXED: consistent sizing
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayTiles = if (boardState.tiles.size > 9)
                boardState.tiles.takeLast(9) else boardState.tiles

            items(displayTiles) { placed ->
                DominoTileView(
                    tile = placed.tile,
                    isMini = boardState.tiles.size > 9,
                    horizontal = true,
                    modifier = if (boardState.tiles.size > 9)
                        Modifier.size(width = 44.dp, height = 26.dp)
                    else
                        Modifier.size(width = 52.dp, height = 30.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// DOMINO TILE VIEW - FIXED VERSION
// ═══════════════════════════════════════════════════════════════════════════
@Composable
fun DominoTileView(
    tile: DominoTile,
    modifier: Modifier = Modifier,
    isMini: Boolean = false,
    isPlaceholder: Boolean = false,
    isSelected: Boolean = false,
    isLegal: Boolean = false,
    horizontal: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.07f else if (isLegal) 1.03f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )
    val ty by animateFloatAsState(
        targetValue = if (isSelected) (-10f) else if (isLegal) (-3f) else 0f,
        animationSpec = tween(150),
        label = "ty"
    )

    // FIXED: Consistent aspect ratio
    // Vertical: 1:2 (width:height)
    // Horizontal: 2:1 (width:height)
    val tileWidth = if (horizontal) {
        if (isMini) 44.dp else 52.dp
    } else {
        if (isMini) 28.dp else 40.dp
    }
    val tileHeight = if (horizontal) {
        if (isMini) 26.dp else 30.dp
    } else {
        if (isMini) 56.dp else 80.dp
    }

    Box(
        modifier = modifier
            .offset(y = ty.dp)
            .scale(scale)
            .size(width = tileWidth, height = tileHeight)
            .background(
                when {
                    isPlaceholder -> Color.White.copy(alpha = 0.05f)
                    else -> Ivory
                },
                RoundedCornerShape(6.dp)  // Slightly larger radius for glossiness
            )
            .border(
                width = when {
                    isSelected -> 3.dp
                    isPlaceholder -> 1.dp
                    else -> 2.dp
                },
                color = when {
                    isSelected -> Gold
                    isPlaceholder -> Color.White.copy(alpha = 0.3f)
                    else -> DominoBorder
                },
                shape = RoundedCornerShape(6.dp)
            )
            .then(
                if (onClick != null && !isPlaceholder)
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { onClick() })
                    }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isPlaceholder) {
            // Glossy effect overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.0f),
                                Color.Black.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(5.dp)
                    )
            )

            if (horizontal) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DotFace(value = tile.top, isMini = isMini, modifier = Modifier.weight(1f))
                    VerticalDivider(
                        color = DominoBorder,
                        thickness = if (isMini) 1.dp else 2.dp
                    )
                    DotFace(value = tile.bottom, isMini = isMini, modifier = Modifier.weight(1f))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    DotFace(value = tile.top, isMini = isMini, modifier = Modifier.weight(1f))
                    HorizontalDivider(
                        color = DominoBorder,
                        thickness = if (isMini) 1.dp else 2.dp,
                        modifier = Modifier.padding(horizontal = if (isMini) 2.dp else 6.dp)
                    )
                    DotFace(value = tile.bottom, isMini = isMini, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// FIXED: DotFace with better spacing
@Composable
private fun DotFace(value: Int, isMini: Boolean, modifier: Modifier = Modifier) {
    val dotSize = if (isMini) 4.dp else 7.dp
    val spacing = if (isMini) 2.dp else 4.dp

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            0 -> {}
            1 -> CenterDot(dotSize)
            2 -> DiagonalDots(dotSize, spacing, tl = true, br = true)
            3 -> DiagonalDots(dotSize, spacing, tl = true, c = true, br = true)
            4 -> DiagonalDots(dotSize, spacing, tl = true, tr = true, bl = true, br = true)
            5 -> DiagonalDots(dotSize, spacing, tl = true, tr = true, c = true, bl = true, br = true)
            6 -> SixDots(dotSize, spacing)
        }
    }
}

@Composable
private fun Dot(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(DominoDot, CircleShape)
    )
}

@Composable
private fun CenterDot(size: Dp) {
    Box(Modifier.size(size * 3), contentAlignment = Alignment.Center) {
        Dot(size)
    }
}

// FIXED: DiagonalDots with better spacing
@Composable
private fun DiagonalDots(
    size: Dp,
    spacing: Dp,
    tl: Boolean = false, tr: Boolean = false,
    c: Boolean = false,
    bl: Boolean = false, br: Boolean = false
) {
    val boxSize = size * 3 + spacing * 2
    Box(Modifier.size(boxSize), contentAlignment = Alignment.Center) {
        if (tl) Dot(size, Modifier.align(Alignment.TopStart))
        if (tr) Dot(size, Modifier.align(Alignment.TopEnd))
        if (c) Dot(size, Modifier.align(Alignment.Center))
        if (bl) Dot(size, Modifier.align(Alignment.BottomStart))
        if (br) Dot(size, Modifier.align(Alignment.BottomEnd))
    }
}

// FIXED: SixDots with better spacing (3 rows × 2 columns)
@Composable
private fun SixDots(size: Dp, spacing: Dp) {
    val boxSize = size * 3 + spacing * 2
    Column(
        modifier = Modifier.size(boxSize),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Dot(size); Dot(size)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Dot(size); Dot(size)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Dot(size); Dot(size)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SIDE SELECTOR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun SideSelector(
    tile: DominoTile,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Gold)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onLeft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("← يسار", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Text(
                "[${tile.top}|${tile.bottom}]",
                color = Gold,
                fontSize = 12.sp
            )

            Button(
                onClick = onRight,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("يمين →", color = LightGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF2A3A2E)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
            ) {
                Text("✕", fontSize = 11.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// PLAYER HAND
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun PlayerHand(
    player: Player,
    playerIndex: Int,
    legalSides: (DominoTile) -> Set<BoardSide>,
    selectedTile: DominoTile?,
    onTileClick: (DominoTile) -> Unit,
    onDrawOrPass: () -> Unit,
    canDraw: Boolean,
    stockCount: Int,
    canInteract: Boolean = true // NEW: disable when not my turn in network mode
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 7.dp)
            .border(1.dp, Color(0xFF192D1F))
    ) {
        // Hand header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = PlayerColors[playerIndex % PlayerColors.size]
            Text(
                text = "${if (player.isAi) "🤖 " else ""}${player.name} (${player.hand.size} قطع)",
                fontSize = 11.sp,
                color = color
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "قيمة اليد: ${player.handValue}",
                fontSize = 11.sp,
                color = TextDim
            )
        }

        Spacer(Modifier.height(5.dp))

        // Tiles
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(player.hand) { tile ->
                val sides = legalSides(tile)
                val isLegal = sides.isNotEmpty() && canInteract // NEW: respect canInteract
                val isSelected = selectedTile?.id == tile.id

                DominoTileView(
                    tile = tile,
                    isLegal = isLegal,
                    isSelected = isSelected,
                    onClick = if (isLegal) { { onTileClick(tile) } } else null,
                    modifier = Modifier.size(width = 40.dp, height = 80.dp)
                )
            }
        }

        Spacer(Modifier.height(7.dp))

        // Draw / Pass button
        val noMoves = player.hand.none { legalSides(it).isNotEmpty() }

        Button(
            onClick = onDrawOrPass,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = noMoves && canInteract, // NEW: respect canInteract
            colors = ButtonDefaults.buttonColors(
                containerColor = if (noMoves && canInteract) Gold else Color(0xFF1E2E2E),
                disabledContainerColor = Color(0xFF1E2E2E)
            ),
            contentPadding = PaddingValues(vertical = 9.dp)
        ) {
            Text(
                text = if (stockCount > 0) "سحب قطعة من المخزون ($stockCount)" else "تخطي الدور",
                color = if (noMoves && canInteract) DeepGreen else TextDim,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// AI THINKING BAR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun AiThinkingBar(currentPlayerName: String) {
    var dotCount by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dotCount = if (dotCount >= 3) 1 else dotCount + 1
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Gold,
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "🤖 $currentPlayerName يفكر${".".repeat(dotCount)}",
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ERROR BANNER
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ErrorBanner(error: String, onDismiss: () -> Unit) {
    val shake by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(100), RepeatMode.Reverse),
        label = "shake"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .offset(x = shake.dp)
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️ $error",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = onDismiss) {
                Text("✕", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ROUND RESULT DIALOG
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun RoundResultDialog(
    gameState: GameState,
    onNextRound: () -> Unit,
    onQuit: () -> Unit
) {
    val lastRound = gameState.matchScore.roundHistory.lastOrNull()

    AlertDialog(
        onDismissRequest = {},
        containerColor = CardBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "انتهت الجولة!",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    gameState.message,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                if (lastRound != null) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Gold.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "+${lastRound.pointsEarned} نقطة",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = LightGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "النتيجة:",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                gameState.players.forEach { player ->
                    val color = PlayerColors[player.id % PlayerColors.size]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(player.name, color = TextMuted)
                        Text(
                            "${gameState.matchScore.playerScore(player.id)} نقطة",
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("▶ جولة تالية", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onQuit) {
                Text("خروج", color = TextMuted)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// MATCH RESULT DIALOG
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun MatchResultDialog(
    gameState: GameState,
    onRematch: () -> Unit,
    onQuit: () -> Unit
) {
    val winnerId = gameState.matchScore.matchWinnerId
    val winner = winnerId?.let { id -> gameState.players.find { it.id == id } }

    AlertDialog(
        onDismissRequest = {},
        containerColor = CardBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "انتهت المباراة!",
                    color = Gold,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    winner?.name ?: "تعادل!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    if (winner != null) "فاز بالمباراة!" else "لا يوجد فائز",
                    color = TextMuted,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "الترتيب النهائي:",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                gameState.matchScore.leaderboard.forEachIndexed { index, (playerId, score) ->
                    val player = gameState.players.find { it.id == playerId }
                    val color = PlayerColors[playerId % PlayerColors.size]
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "4️⃣"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (index == 0) Gold.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(medal, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                player?.name ?: "لاعب",
                                color = if (index == 0) Gold else TextPrimary
                            )
                        }
                        Text(
                            "$score نقطة",
                            color = if (index == 0) Gold else color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRematch,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("🔄 مباراة جديدة", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onQuit) {
                Text("القائمة", color = TextMuted)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// CONNECTION QUALITY INDICATOR
// ═══════════════════════════════════════════════════════════════════════════
@Composable
private fun ConnectionQualityIndicator(pingMs: Long) {
    val (color, label) = when {
        pingMs <= 0 -> Color.Gray to "..."
        pingMs < 50 -> SuccessGreen to "${pingMs}ms"
        pingMs < 150 -> Color(0xFFFFC107) to "${pingMs}ms"
        else -> ErrorRed to "${pingMs}ms"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
