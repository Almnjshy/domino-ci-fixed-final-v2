package com.agon.app.di

import android.content.Context
import androidx.room.Room
import com.agon.app.data.local.dao.StatsDao
import com.agon.app.data.local.database.DominoDatabase
import com.agon.app.data.repository.GameRepositoryImpl
import com.agon.app.data.repository.NetworkRepositoryImpl
import com.agon.app.data.repository.SettingsRepositoryImpl
import com.agon.app.data.repository.StatsRepositoryImpl
import com.agon.app.domain.engine.ai.DominoAIEngine
import com.agon.app.domain.engine.game.DominoGameEngine
import com.agon.app.domain.engine.game.GameClock
import com.agon.app.domain.engine.game.SystemClock
import com.agon.app.domain.repository.GameRepository
import com.agon.app.domain.repository.NetworkRepository
import com.agon.app.domain.repository.SettingsRepository
import com.agon.app.domain.repository.StatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Clock ──────────────────────────────────
    @Provides
    @Singleton
    fun provideGameClock(): GameClock = SystemClock()

    // ── Room ───────────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DominoDatabase =
        Room.databaseBuilder(context, DominoDatabase::class.java, DominoDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideStatsDao(db: DominoDatabase): StatsDao = db.statsDao()

    // ── Engines ────────────────────────────────
    @Provides
    @Singleton
    fun provideDominoGameEngine(clock: GameClock): DominoGameEngine = DominoGameEngine(clock)

    @Provides
    @Singleton
    fun provideDominoAIEngine(): DominoAIEngine = DominoAIEngine()

    // ── Repositories ───────────────────────────
    @Provides
    @Singleton
    fun provideGameRepository(
        engine: DominoGameEngine,
        aiEngine: DominoAIEngine
    ): GameRepository = GameRepositoryImpl(engine, aiEngine)

    @Provides
    @Singleton
    fun provideNetworkRepository(@ApplicationContext context: Context): NetworkRepository = NetworkRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = SettingsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideStatsRepository(dao: StatsDao): StatsRepository = StatsRepositoryImpl(dao)
}
