package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.NetworkState
import com.agon.app.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * UseCase for getting the current network state as a StateFlow.
 */
class GetNetworkStateUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): StateFlow<NetworkState> = networkRepository.networkState
}
