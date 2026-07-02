package com.agon.app.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agon.app.domain.model.AiDifficulty
import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.usecase.ai.AIPlayUseCase
import com.agon.app.domain.usecase.game.CheckGameOverUseCase
import com.agon.app.domain.usecase.game.DrawOrPassUseCase
import com.agon.app.domain.usecase.game.GetLegalMovesUseCase
import com.agon.app.domain.usecase.game.NewGameUseCase
import com.agon.app.domain.usecase.game.PlayTileUseCase
import com.agon.app.domain.usecase.settings.LoadSettingsUseCase
import com.agon.app.domain.usecase.stats.RecordGameUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var newGameUseCase: NewGameUseCase
    private lateinit var playTileUseCase: PlayTileUseCase
    private lateinit var drawOrPassUseCase: DrawOrPassUseCase
    private lateinit var getLegalMovesUseCase: GetLegalMovesUseCase
    private lateinit var checkGameOverUseCase: CheckGameOverUseCase
    private lateinit var aiPlayUseCase: AIPlayUseCase
    private lateinit var loadSettingsUseCase: LoadSettingsUseCase
    private lateinit var recordGameUseCase: RecordGameUseCase

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        newGameUseCase = mockk()
        playTileUseCase = mockk()
        drawOrPassUseCase = mockk()
        getLegalMovesUseCase = mockk()
        checkGameOverUseCase = mockk()
        aiPlayUseCase = mockk()
        loadSettingsUseCase = mockk()
        recordGameUseCase = mockk()

        coEvery { loadSettingsUseCase() } returns com.agon.app.domain.model.AppSettings.DEFAULT

        viewModel = GameViewModel(
            newGameUseCase, playTileUseCase, drawOrPassUseCase,
            getLegalMovesUseCase, checkGameOverUseCase, aiPlayUseCase,
            loadSettingsUseCase, recordGameUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `newGame updates game state`() = runTest {
        val expectedState = com.agon.app.domain.model.GameState(
            gameMode = GameMode.HUMAN_VS_AI
        )
        coEvery { newGameUseCase(GameMode.HUMAN_VS_AI) } returns Result.success(expectedState)

        viewModel.newGame(GameMode.HUMAN_VS_AI)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(expectedState, state.gameState)
        assertFalse(state.isLoading)
    }

    @Test
    fun `getLegalSides delegates to use case`() {
        val tile = DominoTile(3, 4)
        val expectedSides = setOf(BoardSide.LEFT)

        every { getLegalMovesUseCase(tile) } returns expectedSides

        val result = viewModel.getLegalSides(tile)

        assertEquals(expectedSides, result)
    }

    @Test
    fun `clearError removes error`() = runTest {
        viewModel.clearError()

        val state = viewModel.uiState.value
        assertNull(state.error)
    }
}
