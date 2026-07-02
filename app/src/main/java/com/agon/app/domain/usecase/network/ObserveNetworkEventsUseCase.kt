package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.NetworkEvent
import com.agon.app.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for observing network events (player actions, state sync, etc.)
 */
class ObserveNetworkEventsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Flow<NetworkEvent> = networkRepository.events
}
