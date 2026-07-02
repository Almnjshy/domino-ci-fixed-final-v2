package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.NetworkState
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for creating a game room
 */
class CreateRoomUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(roomName: String, maxPlayers: Int = 4): Result<NetworkState> {
        return networkRepository.createRoom(roomName, maxPlayers)
    }
}
