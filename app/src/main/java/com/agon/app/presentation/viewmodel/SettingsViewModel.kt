package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.model.AppSettings
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.settings.ResetSettingsUseCase
import com.agon.app.domain.usecase.settings.SaveSettingsUseCase
import com.agon.app.domain.usecase.settings.UpdateSettingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen with Hilt
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val updateSettingUseCase: UpdateSettingUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase
) : ViewModel() {

    data class SettingsUiState(
        val settings: AppSettings = AppSettings.DEFAULT,
        val hasChanges: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val showResetConfirmation: Boolean = false,
        val saveSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val settings = loadSettingsUseCase()
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false,
                    hasChanges = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateVolume(volume: Float) {
        updateSettings(_uiState.value.settings.copy(volume = volume))
    }

    fun toggleEffects(enabled: Boolean) {
        updateSettings(_uiState.value.settings.copy(effectsEnabled = enabled))
    }

    fun toggleVibration(enabled: Boolean) {
        updateSettings(_uiState.value.settings.copy(vibrationEnabled = enabled))
    }

    fun setLanguage(language: String) {
        updateSettings(_uiState.value.settings.copy(language = language))
    }

    fun setPreferredMode(mode: GameMode) {
        updateSettings(_uiState.value.settings.copy(preferredMode = mode))
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            saveSettingsUseCase(_uiState.value.settings)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasChanges = false,
                        saveSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            resetSettingsUseCase()
                .onSuccess {
                    loadSettings()
                    _uiState.value = _uiState.value.copy(
                        showResetConfirmation = false,
                        saveSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun showResetConfirmation() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = true)
    }

    fun dismissResetConfirmation() {
        _uiState.value = _uiState.value.copy(showResetConfirmation = false)
    }

    fun dismissSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun updateSettings(newSettings: AppSettings) {
        _uiState.value = _uiState.value.copy(
            settings = newSettings,
            hasChanges = newSettings != _uiState.value.settings
        )
    }
}
