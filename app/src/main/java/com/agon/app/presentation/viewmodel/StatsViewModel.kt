package com.agon.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.domain.model.StatsData
import com.agon.app.domain.usecase.stats.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val loadStatsUseCase: LoadStatsUseCase,
    private val clearStatsUseCase: ClearStatsUseCase,
    private val exportStatsUseCase: ExportStatsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    data class StatsUiState(
        val stats: StatsData = StatsData(),
        val achievements: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showClearConfirmation: Boolean = false,
        val exportedJson: String? = null,
        val showExportDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        // Real-time stats observation from Room
        viewModelScope.launch {
            loadStatsUseCase.observe()
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message) }
                .collect { stats ->
                    _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
                }
        }
        loadAchievements()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val stats = loadStatsUseCase()
                _uiState.value = _uiState.value.copy(stats = stats, isLoading = false)
                loadAchievements()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearStats() {
        viewModelScope.launch {
            try {
                clearStatsUseCase()
                _uiState.value = _uiState.value.copy(showClearConfirmation = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun exportStats() {
        viewModelScope.launch {
            try {
                val exported = exportStatsUseCase()
                _uiState.value = _uiState.value.copy(
                    exportedJson = exported, showExportDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                val achievements = getAchievementsUseCase()
                _uiState.value = _uiState.value.copy(achievements = achievements)
            } catch (e: Exception) { /* silent */ }
        }
    }

    fun showClearConfirmation() { _uiState.value = _uiState.value.copy(showClearConfirmation = true) }
    fun dismissClearConfirmation() { _uiState.value = _uiState.value.copy(showClearConfirmation = false) }
    fun dismissExportDialog() { _uiState.value = _uiState.value.copy(showExportDialog = false) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
