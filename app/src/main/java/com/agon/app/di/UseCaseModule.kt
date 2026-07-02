package com.agon.app.di

import com.agon.app.domain.repository.GameRepository
import com.agon.app.domain.repository.NetworkRepository
import com.agon.app.domain.repository.SettingsRepository
import com.agon.app.domain.repository.StatsRepository
import com.agon.app.domain.usecase.ai.AIPlayUseCase
import com.agon.app.domain.usecase.ai.AISelectDifficultyUseCase
import com.agon.app.domain.usecase.game.CheckGameOverUseCase
import com.agon.app.domain.usecase.game.DrawOrPassUseCase
import com.agon.app.domain.usecase.game.GetLegalMovesUseCase
import com.agon.app.domain.usecase.game.NewGameUseCase
import com.agon.app.domain.usecase.game.PlayTileUseCase
import com.agon.app.domain.usecase.network.CreateRoomUseCase
import com.agon.app.domain.usecase.network.DiscoverRoomsUseCase
import com.agon.app.domain.usecase.network.GetNetworkStateUseCase
import com.agon.app.domain.usecase.network.JoinRoomUseCase
import com.agon.app.domain.usecase.network.LeaveRoomUseCase
import com.agon.app.domain.usecase.network.ObserveNetworkEventsUseCase
import com.agon.app.domain.usecase.network.SendGameActionUseCase
import com.agon.app.domain.usecase.network.StartNetworkGameUseCase
import com.agon.app.domain.usecase.network.SendChatMessageUseCase
import com.agon.app.domain.usecase.network.SyncGameStateUseCase
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.settings.ResetSettingsUseCase
import com.agon.app.domain.usecase.settings.SaveSettingsUseCase
import com.agon.app.domain.usecase.settings.UpdateSettingUseCase
import com.agon.app.domain.usecase.stats.ClearStatsUseCase
import com.agon.app.domain.usecase.stats.ExportStatsUseCase
import com.agon.app.domain.usecase.stats.GetAchievementsUseCase
import com.agon.app.domain.usecase.stats.LoadStatsUseCase
import com.agon.app.domain.usecase.stats.RecordGameUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Module providing UseCase dependencies for ViewModels
 */
@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    // Game UseCases
    @Provides
    @ViewModelScoped
    fun provideNewGameUseCase(repo: GameRepository): NewGameUseCase = NewGameUseCase(repo)

    @Provides
    @ViewModelScoped
    fun providePlayTileUseCase(repo: GameRepository): PlayTileUseCase = PlayTileUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideDrawOrPassUseCase(repo: GameRepository): DrawOrPassUseCase = DrawOrPassUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetLegalMovesUseCase(repo: GameRepository): GetLegalMovesUseCase = GetLegalMovesUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideCheckGameOverUseCase(repo: GameRepository): CheckGameOverUseCase = CheckGameOverUseCase(repo)

    // AI UseCases
    @Provides
    @ViewModelScoped
    fun provideAIPlayUseCase(aiEngine: com.agon.app.domain.engine.ai.DominoAIEngine): AIPlayUseCase = AIPlayUseCase(aiEngine)

    @Provides
    @ViewModelScoped
    fun provideAISelectDifficultyUseCase(): AISelectDifficultyUseCase = AISelectDifficultyUseCase()

    // Network UseCases
    @Provides
    @ViewModelScoped
    fun provideCreateRoomUseCase(repo: NetworkRepository): CreateRoomUseCase = CreateRoomUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideDiscoverRoomsUseCase(repo: NetworkRepository): DiscoverRoomsUseCase = DiscoverRoomsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideJoinRoomUseCase(repo: NetworkRepository): JoinRoomUseCase = JoinRoomUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideLeaveRoomUseCase(repo: NetworkRepository): LeaveRoomUseCase = LeaveRoomUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSyncGameStateUseCase(repo: NetworkRepository): SyncGameStateUseCase = SyncGameStateUseCase(repo)

    // NEW: Network Game UseCases
    @Provides
    @ViewModelScoped
    fun provideSendGameActionUseCase(repo: NetworkRepository): SendGameActionUseCase = SendGameActionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideObserveNetworkEventsUseCase(repo: NetworkRepository): ObserveNetworkEventsUseCase = ObserveNetworkEventsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetNetworkStateUseCase(repo: NetworkRepository): GetNetworkStateUseCase = GetNetworkStateUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSendChatMessageUseCase(repo: NetworkRepository): SendChatMessageUseCase = SendChatMessageUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideStartNetworkGameUseCase(
        repo: NetworkRepository,
        newGameUseCase: NewGameUseCase,
        syncGameStateUseCase: SyncGameStateUseCase
    ): StartNetworkGameUseCase = StartNetworkGameUseCase(repo, newGameUseCase, syncGameStateUseCase)

    // Settings UseCases
    @Provides
    @ViewModelScoped
    fun provideLoadSettingsUseCase(repo: SettingsRepository): LoadSettingsUseCase = LoadSettingsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSaveSettingsUseCase(repo: SettingsRepository): SaveSettingsUseCase = SaveSettingsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideUpdateSettingUseCase(repo: SettingsRepository): UpdateSettingUseCase = UpdateSettingUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideResetSettingsUseCase(repo: SettingsRepository): ResetSettingsUseCase = ResetSettingsUseCase(repo)

    // Stats UseCases
    @Provides
    @ViewModelScoped
    fun provideLoadStatsUseCase(repo: StatsRepository): LoadStatsUseCase = LoadStatsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideRecordGameUseCase(repo: StatsRepository): RecordGameUseCase = RecordGameUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideClearStatsUseCase(repo: StatsRepository): ClearStatsUseCase = ClearStatsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideExportStatsUseCase(repo: StatsRepository): ExportStatsUseCase = ExportStatsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetAchievementsUseCase(repo: StatsRepository): GetAchievementsUseCase = GetAchievementsUseCase(repo)
}
