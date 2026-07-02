package com.agon.app.domain.usecase.network

import com.agon.app.domain.model.ChatMessage
import com.agon.app.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * UseCase for sending a chat message in network multiplayer
 */
class SendChatMessageUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke(message: ChatMessage): Result<Unit> {
        return networkRepository.sendChatMessage(message)
    }
}
