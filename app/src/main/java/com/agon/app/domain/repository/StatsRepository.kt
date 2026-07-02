package com.agon.app.domain.repository

import com.agon.app.domain.model.GameResult
import com.agon.app.domain.model.StatsData
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun observeStats(): Flow<StatsData>
    suspend fun getStats(): StatsData
    suspend fun recordGame(result: GameResult, localPlayerId: Int): StatsData
    suspend fun clearStats()
    suspend fun exportStats(): String
    suspend fun getAchievements(): List<String>
}
