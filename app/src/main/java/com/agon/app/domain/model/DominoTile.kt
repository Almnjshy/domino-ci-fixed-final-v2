package com.agon.app.domain.model

/**
 * Represents a single domino tile
 */
data class DominoTile(
    val top: Int,
    val bottom: Int,
    val id: Int = generateId(top, bottom)
) {
    val isDouble: Boolean get() = top == bottom
    val total: Int get() = top + bottom

    fun reversed(): DominoTile = DominoTile(bottom, top, id)

    fun matches(value: Int): Boolean = top == value || bottom == value

    fun getMatchingEnd(value: Int): Int = if (top == value) bottom else top

    override fun toString(): String = "[$top|$bottom]"

    companion object {
        private fun generateId(top: Int, bottom: Int): Int = top * 10 + bottom

        fun createDeck(): List<DominoTile> = buildList {
            for (i in 0..6) {
                for (j in i..6) {
                    add(DominoTile(i, j))
                }
            }
        }
    }
}
