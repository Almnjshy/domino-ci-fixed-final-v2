package com.agon.app.domain.usecase.settings

import com.agon.app.domain.model.AppSettings
import com.agon.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for saving application settings
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings): Result<Unit> {
        return settingsRepository.saveSettings(settings)
    }
}
