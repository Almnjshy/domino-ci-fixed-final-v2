package com.agon.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ══ Colors ════════════════════════════════════════════════════════════════
private val FeltDark = Color(0xFF0D3B10)
private val FeltGreen = Color(0xFF1B5E20)
private val FeltLight = Color(0xFF2E7D32)
private val WoodBrown = Color(0xFF5D4037)
private val WoodLight = Color(0xFF8D6E63)
private val GoldAccent = Color(0xFFFFD700)
private val GoldDark = Color(0xFFB8860B)
private val Ivory = Color(0xFFFFF8E1)
private val DominoBg = Color(0xFFFFF8E1)
private val DominoBorder = Color(0xFF2E3B28)

@Composable
fun PlayScreen(
    onVsAi: () -> Unit,
    onVsPlayer: () -> Unit,
    onNetwork: () -> Unit,
    onTournament: () -> Unit,
    onBack: () -> Unit,
    // NEW: Add 4-player modes
    onFourPlayers: () -> Unit = {},
    onFourAi: () -> Unit = {}  // NEW: 4 players vs AI
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FeltDark, FeltGreen, FeltLight)
                )
            )
    ) {
        // Decorative background elements
        BackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ══ Top Bar ════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // ══ Title ═══════════════════════════════════════════════════════════
            GameTitle()

            Spacer(Modifier.height(40.dp))

            // ══ Game Modes ══════════════════════════════════════════════════════
            Text(
                "اختر وضع اللعب",
                style = MaterialTheme.typography.headlineSmall,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Mode Cards - 2x2 Grid for better layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: AI Modes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameModeCard(
                        title = "ضد AI",
                        subtitle = "مباراة كلاسيكية",
                        icon = Icons.Default.Computer,
                        iconEmoji = "🤖",
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1)),
                        onClick = onVsAi,
                        modifier = Modifier.weight(1f)
                    )

                    // NEW: 4 Players vs AI
                    GameModeCard(
                        title = "4 ضد AI",
                        subtitle = "أربعة لاعبين ضد الذكاء الاصطناعي",
                        icon = Icons.Default.SmartToy,
                        iconEmoji = "🎮",
                        colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)),
                        onClick = onFourAi,
                        modifier = Modifier.weight(1f),
                        isNew = true
                    )
                }

                // Row 2: Human Modes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameModeCard(
                        title = "لاعب ضد لاعب",
                        subtitle = "على نفس الجهاز",
                        icon = Icons.Default.Person,
                        iconEmoji = "👤",
                        colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                        onClick = onVsPlayer,
                        modifier = Modifier.weight(1f)
                    )

                    // NEW: 4 Players
                    GameModeCard(
                        title = "4 لاعبين",
                        subtitle = "أربعة لاعبين على نفس الجهاز",
                        icon = Icons.Default.Group,
                        iconEmoji = "👥",
                        colors = listOf(Color(0xFFEF6C00), Color(0xFFE65100)),
                        onClick = onFourPlayers,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Network & Tournament
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GameModeCard(
                        title = "الشبكة",
                        subtitle = "عبر WiFi",
                        icon = Icons.Default.Wifi,
                        iconEmoji = "📶",
                        colors = listOf(Color(0xFF00838F), Color(0xFF006064)),
                        onClick = onNetwork,
                        modifier = Modifier.weight(1f),
                        isNew = true
                    )

                    GameModeCard(
                        title = "بطولة",
                        subtitle = "متعددة الجولات",
                        icon = Icons.Default.Group,
                        iconEmoji = "🏆",
                        colors = listOf(Color(0xFFC62828), Color(0xFFB71C1C)),
                        onClick = onTournament,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ══ Bottom Decoration ═══════════════════════════════════════════════
            DominoRowDecoration()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Game Title
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GameTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Domino icon
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DominoIconTile(top = 6, bottom = 6, size = 50)
            DominoIconTile(top = 0, bottom = 0, size = 50)
            DominoIconTile(top = 6, bottom = 6, size = 50)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "DOMINO",
            style = MaterialTheme.typography.displayLarge,
            color = GoldAccent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 48.sp,
            letterSpacing = 4.sp
        )

        Text(
            "دومينو",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(3.dp)
                .background(GoldAccent, RoundedCornerShape(2.dp))
                .padding(vertical = 8.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Game Mode Card - Updated with modifier parameter
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconEmoji: String,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNew: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(colors = colors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        iconEmoji,
                        fontSize = 24.sp
                    )
                }

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (isNew) {
                            Spacer(Modifier.width(8.dp))
                            NewBadge()
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                // Arrow
                Text(
                    "❯",
                    color = GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NewBadge() {
    Box(
        modifier = Modifier
            .background(GoldAccent, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            "جديد!",
            color = FeltDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Domino Icon Tile (for title)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun DominoIconTile(top: Int, bottom: Int, size: Int) {
    Box(
        modifier = Modifier
            .size(width = size.dp, height = (size * 1.6).dp)
            .background(DominoBg, RoundedCornerShape(8.dp))
            .border(2.dp, DominoBorder, RoundedCornerShape(8.dp))
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(4.dp)
        ) {
            DotPatternMini(dots = top, size = size / 5)
            Divider(
                color = DominoBorder,
                thickness = 1.5.dp,
                modifier = Modifier.width((size * 0.6).dp)
            )
            DotPatternMini(dots = bottom, size = size / 5)
        }
    }
}

@Composable
private fun DotPatternMini(dots: Int, size: Int) {
    val dotSize = size.dp
    when (dots) {
        0 -> Box(modifier = Modifier.size(dotSize * 3))
        6 -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiniDot(dotSize); MiniDot(dotSize)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiniDot(dotSize); MiniDot(dotSize)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiniDot(dotSize); MiniDot(dotSize)
                }
            }
        }
        else -> {
            Box(modifier = Modifier.size(dotSize * 3), contentAlignment = Alignment.Center) {
                MiniDot(dotSize * 1.5f)
            }
        }
    }
}

@Composable
private fun MiniDot(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(DominoBorder, CircleShape)
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Background Decorations
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun BackgroundDecorations() {
    // Floating domino pieces in background
    Box(modifier = Modifier.fillMaxSize()) {
        // Top right decoration
        DominoDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(80.dp)
                .alpha(0.1f)
        )

        // Bottom left decoration
        DominoDecoration(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .size(60.dp)
                .alpha(0.08f)
        )
    }
}

@Composable
private fun DominoDecoration(modifier: Modifier) {
    Box(modifier = modifier) {
        DominoIconTile(top = 3, bottom = 4, size = 40)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Bottom Domino Row
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun DominoRowDecoration() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tiles = listOf(
            Pair(0, 0), Pair(1, 2), Pair(3, 3), Pair(4, 5), Pair(6, 6)
        )
        tiles.forEachIndexed { index, (top, bottom) ->
            val offset by rememberInfiniteTransition(label = "float_$index").animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offset"
            )
            Box(modifier = Modifier.offset(y = offset.dp)) {
                DominoIconTile(top = top, bottom = bottom, size = 36)
            }
            if (index < tiles.size - 1) {
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}
