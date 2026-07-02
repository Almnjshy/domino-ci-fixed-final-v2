package com.agon.app.data.local.dao

import androidx.room.*
import com.agon.app.data.local.entity.MatchRecordEntity
import com.agon.app.data.local.entity.StatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    // Stats
    @Query("SELECT * FROM stats WHERE id = 1")
    fun observeStats(): Flow<StatsEntity?>

    @Query("SELECT * FROM stats WHERE id = 1")
    suspend fun getStats(): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: StatsEntity)

    @Query("DELETE FROM stats")
    suspend fun clearStats()

    // Match Records
    @Query("SELECT * FROM match_records ORDER BY date DESC LIMIT 50")
    fun observeRecentMatches(): Flow<List<MatchRecordEntity>>

    @Query("SELECT * FROM match_records ORDER BY date DESC LIMIT 50")
    suspend fun getRecentMatches(): List<MatchRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecordEntity)

    @Query("DELETE FROM match_records")
    suspend fun clearMatches()

    @Query("SELECT COUNT(*) FROM match_records WHERE isWin = 1")
    suspend fun getWinCount(): Int

    @Query("SELECT AVG(durationSeconds) FROM match_records")
    suspend fun getAverageDuration(): Float?
}
