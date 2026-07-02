package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.NetworkRoom
import com.agon.app.domain.model.NetworkState
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for joining a room
 */
class JoinRoomUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(room: NetworkRoom, playerName: String): Result<NetworkState> {
        return networkRepository.joinRoom(room, playerName)
    }
}
