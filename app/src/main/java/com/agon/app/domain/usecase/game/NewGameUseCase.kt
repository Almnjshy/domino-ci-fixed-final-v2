package com.agon.app.domain.usecase.game

import com.agon.app.domain.model.GameMode
import com.agon.app.domain.model.GameState
import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

class NewGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(mode: GameMode): Result<GameState> =
        gameRepository.newGame(mode)

    suspend fun newRound(mode: GameMode): Result<GameState> =
        gameRepository.newRound(mode)
}
