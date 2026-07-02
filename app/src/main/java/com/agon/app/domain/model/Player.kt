package com.agon.app.domain.model

/**
 * Represents a player in the game
 */
data class Player(
    val id: Int,
    val name: String,
    val isAi: Boolean = false,
    val hand: List<DominoTile> = emptyList(),
    val score: Int = 0,
    val isConnected: Boolean = true
) {
    val handValue: Int get() = hand.sumOf { it.total }
    val hasTiles: Boolean get() = hand.isNotEmpty()

    fun displayName(): String = when {
        isAi -> "AI $name"
        else -> name
    }

    fun withHand(newHand: List<DominoTile>): Player = copy(hand = newHand)
    fun withScore(newScore: Int): Player = copy(score = newScore)
    fun withoutTile(tile: DominoTile): Player = copy(hand = hand.filter { it.id != tile.id })
    fun withTile(tile: DominoTile): Player = copy(hand = hand + tile)
}
