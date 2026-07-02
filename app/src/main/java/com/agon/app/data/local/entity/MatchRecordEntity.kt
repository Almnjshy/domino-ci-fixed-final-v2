package com.agon.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val gameMode: String,
    val winnerName: String,
    val playerScore: Int,
    val opponentScore: Int,
    val durationSeconds: Long,
    val isWin: Boolean
)
