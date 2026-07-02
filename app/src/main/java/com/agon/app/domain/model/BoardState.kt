package com.agon.app.domain.model

/**
 * Represents the current state of the game board
 * FIXED: Automatic tile flipping + Result return type + proper validation
 */
data class BoardState(
    val tiles: List<PlacedTile> = emptyList(),
    val leftEnd: Int? = null,
    val rightEnd: Int? = null,
    val isEmpty: Boolean = true
) {
    data class PlacedTile(
        val tile: DominoTile,
        val side: BoardSide,
        val orientation: TileOrientation
    )

    /**
     * Check if a tile can be placed on the board
     */
    fun canPlace(tile: DominoTile): Boolean {
        if (isEmpty) return true
        return tile.matches(leftEnd ?: 0) || tile.matches(rightEnd ?: 0)
    }

    /**
     * Get all legal sides for a given tile
     */
    fun getLegalSides(tile: DominoTile): Set<BoardSide> = buildSet {
        if (isEmpty) {
            add(BoardSide.LEFT)
            add(BoardSide.RIGHT)
            return@buildSet
        }
        if (tile.matches(leftEnd ?: 0)) add(BoardSide.LEFT)
        if (tile.matches(rightEnd ?: 0)) add(BoardSide.RIGHT)
    }

    /**
     * Place a tile on the board with automatic flipping to match the chosen side.
     * Returns Result<BoardState> to handle invalid moves gracefully.
     *
     * FIX #1: Automatic tile flipping
     * FIX #2: Validate before placing
     * FIX #3: Return Result type for error handling
     */
    fun place(tile: DominoTile, side: BoardSide): Result<BoardState> {
        // Validate: can this tile be placed at all?
        if (!canPlace(tile)) {
            return Result.failure(
                IllegalArgumentException("Tile ${tile} cannot be placed on board ends [${leftEnd}|${rightEnd}]")
            )
        }

        // Validate: is the chosen side legal?
        val legalSides = getLegalSides(tile)
        if (side !in legalSides) {
            return Result.failure(
                IllegalArgumentException("Side ${side} is not legal for tile ${tile}. Legal sides: ${legalSides}")
            )
        }

        // FIX #1: Flip tile automatically to match the chosen side
        // The matching end must face the board, and the other end becomes the new board end
        val adjustedTile = when {
            isEmpty -> tile
            // LEFT side: the tile's bottom must match leftEnd (or top if flipped)
            side == BoardSide.LEFT && tile.bottom == leftEnd -> tile
            side == BoardSide.LEFT && tile.top == leftEnd -> tile.reversed()
            // RIGHT side: the tile's top must match rightEnd (or bottom if flipped)
            side == BoardSide.RIGHT && tile.top == rightEnd -> tile
            side == BoardSide.RIGHT && tile.bottom == rightEnd -> tile.reversed()
            else -> tile
        }

        val newTiles = tiles + PlacedTile(adjustedTile, side, TileOrientation.HORIZONTAL)

        return Result.success(
            when {
                isEmpty -> BoardState(
                    tiles = newTiles,
                    leftEnd = adjustedTile.top,
                    rightEnd = adjustedTile.bottom,
                    isEmpty = false
                )
                side == BoardSide.LEFT -> {
                    val newLeft = adjustedTile.getMatchingEnd(leftEnd!!)
                    BoardState(newTiles, newLeft, rightEnd, false)
                }
                else -> {
                    val newRight = adjustedTile.getMatchingEnd(rightEnd!!)
                    BoardState(newTiles, leftEnd, newRight, false)
                }
            }
        )
    }

    /**
     * Get the exposed value after placing a tile on a specific side.
     * Useful for AI probability calculations.
     */
    fun getExposedValue(tile: DominoTile, side: BoardSide): Int? {
        if (isEmpty) return null
        val adjusted = when {
            side == BoardSide.LEFT && tile.bottom == leftEnd -> tile
            side == BoardSide.LEFT && tile.top == leftEnd -> tile.reversed()
            side == BoardSide.RIGHT && tile.top == rightEnd -> tile
            side == BoardSide.RIGHT && tile.bottom == rightEnd -> tile.reversed()
            else -> tile
        }
        return adjusted.getMatchingEnd(
            if (side == BoardSide.LEFT) leftEnd!! else rightEnd!!
        )
    }
}

enum class BoardSide { LEFT, RIGHT }
enum class TileOrientation { HORIZONTAL, VERTICAL }
