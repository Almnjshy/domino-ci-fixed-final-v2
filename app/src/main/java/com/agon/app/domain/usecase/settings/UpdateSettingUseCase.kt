package com.agon.app.domain.usecase.settings

import com.agon.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use case for updating a specific setting
 */
class UpdateSettingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(key: String, value: Any): Result<Unit> {
        return settingsRepository.updateSetting(key, value)
    }
}
