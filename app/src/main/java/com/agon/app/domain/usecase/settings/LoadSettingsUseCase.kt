package com.agon.app.domain.usecase.settings

import com.agon.app.domain.model.AppSettings
import com.agon.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for loading application settings
 */
class LoadSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): AppSettings {
        return settingsRepository.loadSettings()
    }
}
