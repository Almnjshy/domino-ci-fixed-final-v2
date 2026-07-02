package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.GameMode
import com.agon.app.domain.model.GameState
import com.agon.app.domain.model.NetworkState
import com.agon.app.domain.repository.NetworkRepository
import com.agon.app.domain.usecase.game.NewGameUseCase
import javax.inject.Inject

/**
 * UseCase for starting a network multiplayer game.
 * Host creates the game state and syncs it to all clients.
 */
class StartNetworkGameUseCase @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val newGameUseCase: NewGameUseCase,
    private val syncGameStateUseCase: SyncGameStateUseCase
) {
    suspend operator fun invoke(networkState: NetworkState): Result<GameState> {
        return try {
            if (!networkState.isHost) {
                return Result.failure(IllegalStateException("فقط المضيف يمكنه بدء اللعبة"))
            }

            // Create game with number of connected players
            val playerCount = networkState.connectedPlayers.size.coerceIn(2, 4)
            val gameMode = when (playerCount) {
                2 -> GameMode.HUMAN_VS_HUMAN
                else -> GameMode.FOUR_HUMANS
            }

            val gameState = newGameUseCase(gameMode).getOrThrow()

            // Sync to all clients
            syncGameStateUseCase(gameState)

            Result.success(gameState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
