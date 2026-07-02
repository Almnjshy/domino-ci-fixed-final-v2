package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.settings.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Main Menu screen with Hilt
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    data class MenuUiState(
        val selectedMode: GameMode = GameMode.HUMAN_VS_AI,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val settings = loadSettingsUseCase()
                _uiState.value = _uiState.value.copy(
                    selectedMode = settings.preferredMode,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectMode(mode: GameMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
        viewModelScope.launch {
            try {
                val currentSettings = loadSettingsUseCase()
                saveSettingsUseCase(currentSettings.copy(preferredMode = mode))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
