package com.agon.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agon.app.data.local.dao.StatsDao
import com.agon.app.data.local.entity.MatchRecordEntity
import com.agon.app.data.local.entity.StatsEntity

@Database(
    entities = [StatsEntity::class, MatchRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DominoDatabase : RoomDatabase() {
    abstract fun statsDao(): StatsDao

    companion object {
        const val DATABASE_NAME = "domino_db"
    }
}
