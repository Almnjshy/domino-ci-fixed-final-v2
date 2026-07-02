package com.agon.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.agon.app.domain.model.AppSettings
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.model.AiDifficulty
import com.agon.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using SharedPreferences
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadFromPrefs())

    companion object {
        private const val PREFS_NAME = "domino_settings"
        private const val KEY_VOLUME = "volume"
        private const val KEY_EFFECTS = "effects"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PREFERRED_MODE = "preferred_mode"
        private const val KEY_AI_DIFFICULTY = "ai_difficulty"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_SOUND = "sound"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    override val settings: Flow<AppSettings> = _settings.asStateFlow()

    override suspend fun loadSettings(): AppSettings {
        return loadFromPrefs()
    }

    override suspend fun saveSettings(settings: AppSettings): Result<Unit> {
        return try {
            prefs.edit().apply {
                putFloat(KEY_VOLUME, settings.volume)
                putBoolean(KEY_EFFECTS, settings.effectsEnabled)
                putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
                putString(KEY_LANGUAGE, settings.language)
                putString(KEY_PREFERRED_MODE, settings.preferredMode.name)
                putString(KEY_AI_DIFFICULTY, settings.aiDifficulty.name)
                putBoolean(KEY_ANIMATIONS, settings.animationsEnabled)
                putBoolean(KEY_SOUND, settings.soundEnabled)
                putBoolean(KEY_DARK_MODE, settings.darkMode)
                apply()
            }
            _settings.value = settings
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSetting(key: String, value: Any): Result<Unit> {
        return try {
            val currentSettings = _settings.value
            val newSettings = when (key) {
                KEY_VOLUME -> currentSettings.copy(volume = value as Float)
                KEY_EFFECTS -> currentSettings.copy(effectsEnabled = value as Boolean)
                KEY_VIBRATION -> currentSettings.copy(vibrationEnabled = value as Boolean)
                KEY_LANGUAGE -> currentSettings.copy(language = value as String)
                KEY_PREFERRED_MODE -> currentSettings.copy(preferredMode = GameMode.valueOf(value as String))
                KEY_AI_DIFFICULTY -> currentSettings.copy(aiDifficulty = AiDifficulty.valueOf(value as String))
                KEY_ANIMATIONS -> currentSettings.copy(animationsEnabled = value as Boolean)
                KEY_SOUND -> currentSettings.copy(soundEnabled = value as Boolean)
                KEY_DARK_MODE -> currentSettings.copy(darkMode = value as Boolean)
                else -> currentSettings
            }
            saveSettings(newSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetToDefaults(): Result<Unit> {
        return saveSettings(AppSettings.DEFAULT)
    }

    override fun observeSettings(): Flow<AppSettings> = settings

    private fun loadFromPrefs(): AppSettings {
        return AppSettings(
            volume = prefs.getFloat(KEY_VOLUME, 0.8f),
            effectsEnabled = prefs.getBoolean(KEY_EFFECTS, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            language = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar",
            preferredMode = GameMode.valueOf(
                prefs.getString(KEY_PREFERRED_MODE, GameMode.HUMAN_VS_AI.name) 
                    ?: GameMode.HUMAN_VS_AI.name
            ),
            aiDifficulty = AiDifficulty.valueOf(
                prefs.getString(KEY_AI_DIFFICULTY, AiDifficulty.MEDIUM.name) 
                    ?: AiDifficulty.MEDIUM.name
            ),
            animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            darkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        )
    }
}
