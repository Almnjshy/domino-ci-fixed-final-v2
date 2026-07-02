package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.GameState
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for syncing game state across network
 */
class SyncGameStateUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(state: GameState): Result<Unit> {
        return networkRepository.syncGameState(state)
    }
}
