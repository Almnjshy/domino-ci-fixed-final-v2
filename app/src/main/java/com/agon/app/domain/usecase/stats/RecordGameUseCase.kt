package com.agon.app.domain.usecase.stats

import com.agon.app.domain.model.GameResult
import com.agon.app.domain.model.StatsData
import com.agon.app.domain.repository.StatsRepository
import javax.inject.Inject

class RecordGameUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend operator fun invoke(result: GameResult, localPlayerId: Int): StatsData =
        statsRepository.recordGame(result, localPlayerId)
}
