package com.agon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey val id: Int = 1,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalPoints: Int = 0,
    val longestWinStreak: Int = 0,
    val currentWinStreak: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
