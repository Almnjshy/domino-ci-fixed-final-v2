package com.agon.app.domain.usecase.settings

import com.agon.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for resetting settings to defaults
 */
class ResetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return settingsRepository.resetToDefaults()
    }
}
