package com.agon.app.domain.usecase.stats

import com.agon.app.domain.model.StatsData
import com.agon.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    fun observe(): Flow<StatsData> = statsRepository.observeStats()
    suspend operator fun invoke(): StatsData = statsRepository.getStats()
}
