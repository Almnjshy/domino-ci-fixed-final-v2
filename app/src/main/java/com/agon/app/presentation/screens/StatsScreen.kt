package com.agon.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agon.app.domain.model.StatsData

private val FeltDark = Color(0xFF0D3B10)
private val FeltGreen = Color(0xFF1B5E20)
private val GoldAccent = Color(0xFFFFD700)

@Composable
fun StatsScreen(
    stats: StatsData,
    achievements: List<String>,
    isLoading: Boolean,
    error: String?,
    showClearConfirmation: Boolean,
    exportedJson: String?,
    showExportDialog: Boolean,
    onClearStats: () -> Unit,
    onExport: () -> Unit,
    onShowClearConfirmation: () -> Unit,
    onDismissClearConfirmation: () -> Unit,
    onDismissExportDialog: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FeltDark, FeltGreen, Color(0xFF2E7D32))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ══ Top Bar ════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    "الإحصائيات",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
                return@Column
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ══ Stats Summary Card ═════════════════════════════════════
                item {
                    StatsSummaryCard(stats)
                }

                // ══ Win Rate Chart ═════════════════════════════════════════
                item {
                    WinRateChart(stats)
                }

                // ══ Achievements ═══════════════════════════════════════════
                if (achievements.isNotEmpty()) {
                    item {
                        Text(
                            "🏆 الإنجازات",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(achievements) { achievement ->
                        AchievementCard(achievement)
                    }
                }

                // ══ Action Buttons ═════════════════════════════════════════
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export Button - using Button with transparent background instead of OutlinedButton with defaults
                        OutlinedButton(
                            onClick = onExport,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldAccent),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = GoldAccent
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("تصدير")
                        }
                        // Clear Button
                        OutlinedButton(
                            onClick = onShowClearConfirmation,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("مسح الكل")
                        }
                    }
                }

                // ══ Match History ══════════════════════════════════════════
                if (stats.history.isNotEmpty()) {
                    item {
                        Text(
                            "📋 آخر المباريات",
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(stats.history.takeLast(10).reversed()) { record ->
                        MatchHistoryCard(record)
                    }
                }

                // Bottom padding
                item { Spacer(Modifier.height(32.dp)) }
            }
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
                        Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onClearError) { Text("إغلاق") }
                    }
                }
            }
        }

        // Clear Confirmation Dialog
        if (showClearConfirmation) {
            AlertDialog(
                onDismissRequest = onDismissClearConfirmation,
                containerColor = FeltDark,
                title = {
                    Text(
                        "مسح الإحصائيات",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "هل تريد مسح جميع الإحصائيات؟ هذا الإجراء لا يمكن التراجع عنه.",
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onClearStats,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("مسح")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissClearConfirmation) {
                        Text("إلغاء", color = Color.White)
                    }
                }
            )
        }

        // Export Dialog
        if (showExportDialog && exportedJson != null) {
            AlertDialog(
                onDismissRequest = onDismissExportDialog,
                containerColor = FeltDark,
                title = {
                    Text(
                        "تصدير البيانات",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "تم تجهيز البيانات للتصدير",
                        color = Color.White
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onDismissExportDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = FeltDark
                        )
                    ) {
                        Text("حسناً")
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Stats Summary Card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StatsSummaryCard(stats: StatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📊 ملخص الإحصائيات",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatRowEnhanced(
                    icon = "🎮",
                    label = "المباريات",
                    value = "${stats.matchesPlayed}"
                )
                StatRowEnhanced(
                    icon = "🏆",
                    label = "الانتصارات",
                    value = "${stats.wins}",
                    valueColor = Color(0xFF4CAF50)
                )
                StatRowEnhanced(
                    icon = "❌",
                    label = "الخسائر",
                    value = "${stats.losses}",
                    valueColor = Color(0xFFEF5350)
                )
                StatRowEnhanced(
                    icon = "📈",
                    label = "نسبة الفوز",
                    value = "${"%.1f".format(stats.winRate)}%",
                    valueColor = GoldAccent
                )
                StatRowEnhanced(
                    icon = "🔥",
                    label = "أطول سلسلة انتصارات",
                    value = "${stats.longestWinStreak}"
                )
                StatRowEnhanced(
                    icon = "⏱️",
                    label = "وقت اللعب",
                    value = "${stats.totalPlayTimeSeconds / 60} دقيقة"
                )
            }
        }
    }
}

@Composable
private fun StatRowEnhanced(
    icon: String,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Win Rate Chart
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun WinRateChart(stats: StatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📈 نسبة الفوز",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${"%.0f".format(stats.winRate)}%",
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${stats.wins} فوز من ${stats.matchesPlayed} مباراة",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Achievement Card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AchievementCard(achievement: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoldAccent.copy(alpha = 0.15f)
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏆", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                achievement,
                color = GoldAccent,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Match History Card - Using Any to avoid GameRecord dependency
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun MatchHistoryCard(record: Any) {
    // Use reflection to get properties safely
    val isWin = try {
        record.javaClass.getMethod("isWin").invoke(record) as? Boolean ?: false
    } catch (e: Exception) { false }

    val durationSeconds = try {
        record.javaClass.getMethod("getDurationSeconds").invoke(record) as? Int ?: 0
    } catch (e: Exception) { 0 }

    val date = try {
        record.javaClass.getMethod("getDate").invoke(record) as? String ?: ""
    } catch (e: Exception) { "" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isWin) Color(0xFF4CAF50).copy(alpha = 0.3f)
                            else Color(0xFFEF5350).copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isWin) "✓" else "✗",
                        color = if (isWin) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (isWin) "فوز" else "خسارة",
                        color = if (isWin) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "$durationSeconds ثانية",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                date,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
