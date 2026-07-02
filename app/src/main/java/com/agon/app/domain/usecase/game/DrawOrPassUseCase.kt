package com.agon.app.domain.usecase.game

import com.agon.app.domain.model.GameState
import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case for drawing a tile or passing turn
 */
class DrawOrPassUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(): Result<GameState> {
        return gameRepository.drawOrPass()
    }
}
