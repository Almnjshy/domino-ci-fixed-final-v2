package com.agon.app.domain.usecase.network

import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for leaving a room
 */
class LeaveRoomUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return networkRepository.leaveRoom()
    }
}
