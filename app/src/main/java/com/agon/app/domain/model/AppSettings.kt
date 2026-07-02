package com.agon.app.domain.model

/**
 * Application settings
 */
data class AppSettings(
    val volume: Float = 0.8f,
    val effectsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: String = "ar",
    val preferredMode: GameMode = GameMode.HUMAN_VS_AI,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val animationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val darkMode: Boolean = false
) {
    companion object {
        val DEFAULT = AppSettings()
    }
}

enum class AiDifficulty {
    EASY,
    MEDIUM,
    HARD;

    val delayMs: Long
        get() = when (this) {
            EASY -> 500L
            MEDIUM -> 1200L
            HARD -> 2000L
        }

    val mistakeProbability: Float
        get() = when (this) {
            EASY -> 0.3f
            MEDIUM -> 0.1f
            HARD -> 0.0f
        }
}
