package com.agon.app.domain.repository

import com.agon.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for settings operations
 */
interface SettingsRepository {

    /** Current settings */
    val settings: Flow<AppSettings>

    /**
     * Load settings from storage
     */
    suspend fun loadSettings(): AppSettings

    /**
     * Save settings to storage
     */
    suspend fun saveSettings(settings: AppSettings): Result<Unit>

    /**
     * Update specific setting
     */
    suspend fun updateSetting(key: String, value: Any): Result<Unit>

    /**
     * Reset to default settings
     */
    suspend fun resetToDefaults(): Result<Unit>

    /**
     * Observe settings changes
     */
    fun observeSettings(): Flow<AppSettings>
}
