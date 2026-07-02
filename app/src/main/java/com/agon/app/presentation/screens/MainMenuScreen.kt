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
import androidx.compose.material.icons.filled.*
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
import com.agon.app.domain.model.GameMode

// ══ Colors ════════════════════════════════════════════════════════════════
private val FeltDark = Color(0xFF0D3B10)
private val FeltGreen = Color(0xFF1B5E20)
private val FeltLight = Color(0xFF2E7D32)
private val WoodBrown = Color(0xFF5D4037)
private val GoldAccent = Color(0xFFFFD700)
private val GoldDark = Color(0xFFB8860B)
private val Ivory = Color(0xFFFFF8E1)
private val DominoBg = Color(0xFFFFF8E1)
private val DominoBorder = Color(0xFF2E3B28)

/**
 * NEW Main Menu Screen - Modern design based on PlayScreen
 * Replaces the old simple menu with the beautiful card-based design
 */
@Composable
fun MainMenuScreen(
    selectedMode: GameMode = GameMode.HUMAN_VS_AI,
    isLoading: Boolean = false,
    error: String? = null,
    onModeSelected: (GameMode) -> Unit = {},
    onVsAi: () -> Unit,
    onVsPlayer: () -> Unit,
    onNetwork: () -> Unit,
    onTournament: () -> Unit,
    onFourPlayers: () -> Unit = {},
    onSettings: () -> Unit,
    onStats: () -> Unit,
    onClearError: () -> Unit = {}
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
        BackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ══ Top Bar with Settings & Stats Icons ════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats button (top left)
                IconButton(
                    onClick = onStats,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = "الإحصائيات",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Settings button (top right)
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ══ Title ═══════════════════════════════════════════════════════
            GameTitle()

            Spacer(Modifier.height(32.dp))

            // ══ Game Modes ══════════════════════════════════════════════════
            Text(
                "اختر وضع اللعب",
                style = MaterialTheme.typography.headlineSmall,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Mode Cards - Full width cards like the screenshot
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // AI Mode (Blue)
                GameModeCardFull(
                    title = "ضد الذكاء الاصطناعي",
                    subtitle = "تحدي الكمبيوتر في مباراة كلاسيكية",
                    iconEmoji = "🤖",
                    colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1)),
                    onClick = onVsAi
                )

                // Human vs Human (Purple)
                GameModeCardFull(
                    title = "لاعب ضد لاعب",
                    subtitle = "تحدي صديقك على نفس الجهاز",
                    iconEmoji = "👤",
                    colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C)),
                    onClick = onVsPlayer
                )

                // Network (Green with "New" badge)
                GameModeCardFull(
                    title = "اللعب عبر الشبكة",
                    subtitle = "تواصل مع الأصدقاء عبر WiFi",
                    iconEmoji = "📶",
                    colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)),
                    onClick = onNetwork,
                    isNew = true
                )

                // Tournament (Orange)
                GameModeCardFull(
                    title = "بطولة",
                    subtitle = "تنافس في بطولة متعددة الجولات",
                    iconEmoji = "🏆",
                    colors = listOf(Color(0xFFEF6C00), Color(0xFFE65100)),
                    onClick = onTournament
                )
            }

            Spacer(Modifier.weight(1f))

            // ══ Bottom Decoration ═══════════════════════════════════════════
            DominoRowDecoration()
        }

        // Error display
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error,
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = onClearError) { Text("✕") }
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldAccent)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Full Width Game Mode Card (matching the screenshot design)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GameModeCardFull(
    title: String,
    subtitle: String,
    iconEmoji: String,
    colors: List<Color>,
    onClick: () -> Unit,
    isNew: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(16.dp))
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left arrow (chevron)
                Text(
                    "❮",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (isNew) {
                            Spacer(Modifier.width(8.dp))
                            NewBadge()
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }

                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun NewBadge() {
    Box(
        modifier = Modifier
            .background(GoldAccent, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            "جديد!",
            color = FeltDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Game Title (reused from PlayScreen)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GameTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Domino icon row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DominoIconTile(top = 6, bottom = 6, size = 44)
            DominoIconTile(top = 0, bottom = 0, size = 44)
            DominoIconTile(top = 6, bottom = 6, size = 44)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "DOMINO",
            style = MaterialTheme.typography.displayLarge,
            color = GoldAccent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 44.sp,
            letterSpacing = 4.sp
        )

        Text(
            "دومينو",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(3.dp)
                .background(GoldAccent, RoundedCornerShape(2.dp))
                .padding(vertical = 6.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Domino Icon Tile (reused from PlayScreen)
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
    Box(modifier = Modifier.fillMaxSize()) {
        DominoDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(80.dp)
                .alpha(0.1f)
        )
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
