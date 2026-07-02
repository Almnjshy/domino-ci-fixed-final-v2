package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.stats.LoadStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Splash screen
 * Handles initialization and loading states
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val loadStatsUseCase: LoadStatsUseCase
) : ViewModel() {

    data class SplashUiState(
        val isLoading: Boolean = true,
        val progress: Float = 0f,
        val error: String? = null,
        val isReady: Boolean = false
    )

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Step 1: Load settings (0-40%)
                updateProgress(0.1f)
                loadSettingsUseCase()
                updateProgress(0.4f)

                // Step 2: Load stats (40-70%)
                loadStatsUseCase()
                updateProgress(0.7f)

                // Step 3: Initialize game resources (70-100%)
                delay(500)
                updateProgress(1f)

                // Mark as ready
                _uiState.value = _uiState.value.copy(isReady = true, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun updateProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(progress = progress)
        delay(200) // Small delay for visual feedback
    }

    fun retry() {
        _uiState.value = SplashUiState()
        initializeApp()
    }

    fun markReady() {
        _uiState.value = _uiState.value.copy(isReady = true)
    }
}
