package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.GameAction
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * UseCase for sending a game action over the network.
 * Used by GameViewModel in network multiplayer mode.
 */
class SendGameActionUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(action: GameAction): Result<Unit> {
        return networkRepository.sendGameAction(action)
    }
}
