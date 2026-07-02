package com.agon.app.domain.usecase.game

import com.agon.app.domain.model.GameState
import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

/**
 * UseCase for passing turn without drawing.
 * Used in network mode when remote player explicitly sends PassTurn.
 */
class PassTurnUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(): Result<GameState> =
        gameRepository.passTurn()
}