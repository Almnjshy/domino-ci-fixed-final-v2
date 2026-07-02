package com.agon.app.domain.usecase

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameMode
import com.agon.app.domain.repository.GameRepository
import com.agon.app.domain.usecase.game.DrawOrPassUseCase
import com.agon.app.domain.usecase.game.GetLegalMovesUseCase
import com.agon.app.domain.usecase.game.NewGameUseCase
import com.agon.app.domain.usecase.game.PlayTileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameUseCaseTest {

    private lateinit var gameRepository: GameRepository
    private lateinit var newGameUseCase: NewGameUseCase
    private lateinit var playTileUseCase: PlayTileUseCase
    private lateinit var drawOrPassUseCase: DrawOrPassUseCase
    private lateinit var getLegalMovesUseCase: GetLegalMovesUseCase

    @Before
    fun setup() {
        gameRepository = mockk(relaxed = true)
        newGameUseCase = NewGameUseCase(gameRepository)
        playTileUseCase = PlayTileUseCase(gameRepository)
        drawOrPassUseCase = DrawOrPassUseCase(gameRepository)
        getLegalMovesUseCase = GetLegalMovesUseCase(gameRepository)
    }

    @Test
    fun `newGame calls repository newGame`() = runTest {
        val mode = GameMode.HUMAN_VS_AI
        val expectedState = com.agon.app.domain.model.GameState(gameMode = mode)

        coEvery { gameRepository.newGame(mode) } returns Result.success(expectedState)

        val result = newGameUseCase(mode)

        assertTrue(result.isSuccess)
        assertEquals(expectedState, result.getOrNull())
        coVerify { gameRepository.newGame(mode) }
    }

    @Test
    fun `playTile calls repository playTile`() = runTest {
        val tile = DominoTile(3, 4)
        val side = BoardSide.LEFT
        val expectedState = com.agon.app.domain.model.GameState()

        coEvery { gameRepository.playTile(tile, side) } returns Result.success(expectedState)

        val result = playTileUseCase(tile, side)

        assertTrue(result.isSuccess)
        coVerify { gameRepository.playTile(tile, side) }
    }

    @Test
    fun `getLegalMoves returns legal sides`() {
        val tile = DominoTile(3, 4)
        val expectedSides = setOf(BoardSide.LEFT, BoardSide.RIGHT)

        every { gameRepository.getLegalSides(tile) } returns expectedSides

        val result = getLegalMovesUseCase(tile)

        assertEquals(expectedSides, result)
    }

    @Test
    fun `drawOrPass calls repository drawOrPass`() = runTest {
        val expectedState = com.agon.app.domain.model.GameState()

        coEvery { gameRepository.drawOrPass() } returns Result.success(expectedState)

        val result = drawOrPassUseCase()

        assertTrue(result.isSuccess)
        coVerify { gameRepository.drawOrPass() }
    }
}
