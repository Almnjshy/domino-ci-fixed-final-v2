package com.agon.app.domain.usecase.ai

import com.agon.app.domain.model.AiDifficulty
import javax.inject.Inject

/**
 * Use case for selecting AI difficulty
 */
class AISelectDifficultyUseCase @Inject constructor() {
    operator fun invoke(level: Int): AiDifficulty {
        return when (level) {
            0 -> AiDifficulty.EASY
            1 -> AiDifficulty.MEDIUM
            2 -> AiDifficulty.HARD
            else -> AiDifficulty.MEDIUM
        }
    }

    fun getAllDifficulties(): List<AiDifficulty> = AiDifficulty.values().toList()
}
