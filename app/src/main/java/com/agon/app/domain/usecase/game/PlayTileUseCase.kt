package com.agon.app.domain.usecase.game

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.model.GameState
import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case for playing a tile on the board
 */
class PlayTileUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(tile: DominoTile, side: BoardSide): Result<GameState> {
        return gameRepository.playTile(tile, side)
    }
}
