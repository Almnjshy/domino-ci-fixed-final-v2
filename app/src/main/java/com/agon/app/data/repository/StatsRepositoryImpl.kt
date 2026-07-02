package com.agon.app.data.repository

import com.agon.app.data.local.dao.StatsDao
import com.agon.app.data.local.entity.MatchRecordEntity
import com.agon.app.data.local.entity.StatsEntity
import com.agon.app.domain.model.GameResult
import com.agon.app.domain.model.MatchRecord
import com.agon.app.domain.model.StatsData
import com.agon.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val statsDao: StatsDao
) : StatsRepository {

    override fun observeStats(): Flow<StatsData> {
        return combine(
            statsDao.observeStats(),
            statsDao.observeRecentMatches()
        ) { statsEntity, matches ->
            val entity = statsEntity ?: StatsEntity()
            entity.toStatsData(matches.map { it.toMatchRecord() })
        }
    }

    override suspend fun getStats(): StatsData {
        val entity = statsDao.getStats() ?: StatsEntity()
        val matches = statsDao.getRecentMatches()
        return entity.toStatsData(matches.map { it.toMatchRecord() })
    }

    override suspend fun recordGame(result: GameResult, localPlayerId: Int): StatsData {
        val isWin = result.winnerId == localPlayerId
        val current = statsDao.getStats() ?: StatsEntity()

        val newStreak = if (isWin) current.currentWinStreak + 1 else 0
        val updated = current.copy(
            matchesPlayed = current.matchesPlayed + 1,
            wins = current.wins + if (isWin) 1 else 0,
            losses = current.losses + if (!isWin) 1 else 0,
            totalPoints = current.totalPoints + (result.scores[localPlayerId] ?: 0),
            longestWinStreak = maxOf(current.longestWinStreak, newStreak),
            currentWinStreak = newStreak,
            totalPlayTimeSeconds = current.totalPlayTimeSeconds + result.durationSeconds,
            lastUpdated = System.currentTimeMillis()
        )
        statsDao.upsertStats(updated)

        val matchEntity = MatchRecordEntity(
            id = System.currentTimeMillis().toString(),
            date = System.currentTimeMillis(),
            gameMode = result.gameMode.name,
            winnerName = result.winnerName,
            playerScore = result.scores[localPlayerId] ?: 0,
            opponentScore = result.scores.entries.firstOrNull { it.key != localPlayerId }?.value ?: 0,
            durationSeconds = result.durationSeconds,
            isWin = isWin
        )
        statsDao.insertMatch(matchEntity)

        return getStats()
    }

    override suspend fun clearStats() {
        statsDao.clearStats()
        statsDao.clearMatches()
    }

    override suspend fun exportStats(): String {
        val stats = getStats()
        return buildString {
            appendLine("=== إحصائيات دومينو ===")
            appendLine("المباريات: ${stats.matchesPlayed}")
            appendLine("الانتصارات: ${stats.wins}")
            appendLine("الخسائر: ${stats.losses}")
            appendLine("نسبة الفوز: ${"%.1f".format(stats.winRate)}%")
            appendLine("أطول سلسلة: ${stats.longestWinStreak}")
            appendLine("وقت اللعب: ${stats.totalPlayTimeSeconds / 60} دقيقة")
            appendLine("آخر تحديث: ${Date(stats.lastUpdated.time)}")
        }
    }

    override suspend fun getAchievements(): List<String> {
        val stats = getStats()
        return buildList {
            if (stats.wins >= 1) add("🏆 أول انتصار")
            if (stats.wins >= 10) add("⭐ 10 انتصارات")
            if (stats.wins >= 50) add("🌟 50 انتصاراً")
            if (stats.longestWinStreak >= 3) add("🔥 سلسلة 3 انتصارات")
            if (stats.longestWinStreak >= 5) add("💥 سلسلة 5 انتصارات")
            if (stats.longestWinStreak >= 10) add("👑 سلسلة 10 انتصارات")
            if (stats.matchesPlayed >= 100) add("🎮 لاعب متمرس (100 مباراة)")
            if (stats.totalPlayTimeSeconds >= 3600) add("⏰ ساعة كاملة من اللعب")
        }
    }

    private fun StatsEntity.toStatsData(history: List<MatchRecord>) = StatsData(
        matchesPlayed = matchesPlayed,
        wins = wins,
        losses = losses,
        draws = draws,
        totalPoints = totalPoints,
        longestWinStreak = longestWinStreak,
        currentWinStreak = currentWinStreak,
        totalPlayTimeSeconds = totalPlayTimeSeconds,
        history = history,
        lastUpdated = Date(lastUpdated)
    )

    private fun MatchRecordEntity.toMatchRecord() = MatchRecord(
        id = id,
        date = Date(date),
        gameMode = com.agon.app.domain.model.GameMode.valueOf(gameMode),
        winnerName = winnerName,
        playerScore = playerScore,
        opponentScore = opponentScore,
        durationSeconds = durationSeconds,
        isWin = isWin
    )
}
