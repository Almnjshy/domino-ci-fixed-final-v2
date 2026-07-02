package com.agon.app.domain.usecase.game

import com.agon.app.domain.model.BoardSide
import com.agon.app.domain.model.DominoTile
import com.agon.app.domain.repository.GameRepository
import javax.inject.Inject

/**
 * Use case for getting legal moves for a tile
 */
class GetLegalMovesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(tile: DominoTile): Set<BoardSide> {
        return gameRepository.getLegalSides(tile)
    }
}
