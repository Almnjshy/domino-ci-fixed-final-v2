package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.NetworkRoom
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for discovering available rooms
 */
class DiscoverRoomsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(): Result<List<NetworkRoom>> {
        return networkRepository.discoverRooms()
    }
}
