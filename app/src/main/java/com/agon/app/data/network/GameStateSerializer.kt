package com.agon.app.data.network

import com.agon.app.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts GameState <-> JSON for network transmission.
 * Only transmits what's needed (no internal engine state).
 */
object GameStateSerializer {

    fun toJson(state: GameState): JSONObject = JSONObject().apply {
        put("currentPlayerIndex", state.currentPlayerIndex)
        put("turnCount", state.turnCount)
        put("roundOver", state.roundOver)
        put("isBlocked", state.isBlocked)
        put("winnerId", state.winnerId ?: -1)
        put("message", state.message)
        put("gameMode", state.gameMode.name)
        put("stockCount", state.stock.size)
        put("board", boardToJson(state.board))
        put("players", playersToJson(state.players))
        put("matchScore", matchScoreToJson(state.matchScore))
    }

    fun fromJson(obj: JSONObject): GameState {
        val players = playersFromJson(obj.getJSONArray("players"))
        val board = boardFromJson(obj.getJSONObject("board"))
        val matchScore = matchScoreFromJson(obj.getJSONObject("matchScore"))
        val winnerId = obj.getInt("winnerId").takeIf { it >= 0 }
        val gameMode = try { GameMode.valueOf(obj.getString("gameMode")) } catch (e: Exception) { GameMode.HUMAN_VS_AI }

        return GameState(
            players = players,
            board = board,
            stock = emptyList(), // clients don't get stock contents
            currentPlayerIndex = obj.getInt("currentPlayerIndex"),
            turnCount = obj.getInt("turnCount"),
            roundOver = obj.getBoolean("roundOver"),
            isBlocked = obj.getBoolean("isBlocked"),
            winnerId = winnerId,
            message = obj.optString("message", ""),
            gameMode = gameMode,
            matchScore = matchScore
        )
    }

    // ── Board ──────────────────────────────────────
    private fun boardToJson(board: BoardState): JSONObject = JSONObject().apply {
        put("leftEnd", board.leftEnd ?: -1)
        put("rightEnd", board.rightEnd ?: -1)
        put("tileCount", board.tiles.size)
        val tilesArray = JSONArray()
        board.tiles.forEach { placed ->
            tilesArray.put(JSONObject().apply {
                put("top", placed.tile.top)
                put("bot", placed.tile.bottom)
                put("id", placed.tile.id)
                put("side", placed.side.name)
            })
        }
        put("tiles", tilesArray)
    }

    private fun boardFromJson(obj: JSONObject): BoardState {
        val tiles = mutableListOf<BoardState.PlacedTile>()
        val arr = obj.getJSONArray("tiles")
        for (i in 0 until arr.length()) {
            val t = arr.getJSONObject(i)
            tiles.add(BoardState.PlacedTile(
                tile = DominoTile(t.getInt("top"), t.getInt("bot"), t.getInt("id")),
                side = try { BoardSide.valueOf(t.getString("side")) } catch (e: Exception) { BoardSide.RIGHT },
                orientation = TileOrientation.HORIZONTAL
            ))
        }
        return BoardState(
            tiles = tiles,
            leftEnd = obj.getInt("leftEnd").takeIf { it >= 0 },
            rightEnd = obj.getInt("rightEnd").takeIf { it >= 0 }
        )
    }

    // ── Players ────────────────────────────────────
    private fun playersToJson(players: List<Player>): JSONArray {
        val arr = JSONArray()
        players.forEach { player ->
            arr.put(JSONObject().apply {
                put("id", player.id)
                put("name", player.name)
                put("isAi", player.isAi)
                put("handSize", player.hand.size)
                put("handValue", player.handValue)
                // Only transmit full hand to the player's own device
                // Server sends filtered state per player
                val handArr = JSONArray()
                player.hand.forEach { tile ->
                    handArr.put(JSONObject().apply {
                        put("id", tile.id); put("top", tile.top); put("bot", tile.bottom)
                    })
                }
                put("hand", handArr)
            })
        }
        return arr
    }

    private fun playersFromJson(arr: JSONArray): List<Player> {
        val players = mutableListOf<Player>()
        for (i in 0 until arr.length()) {
            val p = arr.getJSONObject(i)
            val hand = mutableListOf<DominoTile>()
            val handArr = p.optJSONArray("hand") ?: JSONArray()
            for (j in 0 until handArr.length()) {
                val t = handArr.getJSONObject(j)
                hand.add(DominoTile(t.getInt("top"), t.getInt("bot"), t.getInt("id")))
            }
            players.add(Player(
                id = p.getInt("id"),
                name = p.getString("name"),
                isAi = p.getBoolean("isAi"),
                hand = hand
            ))
        }
        return players
    }

    // ── MatchScore ─────────────────────────────────
    private fun matchScoreToJson(score: MatchScore): JSONObject = JSONObject().apply {
        val scoresObj = JSONObject()
        score.scores.forEach { (k, v) -> scoresObj.put(k.toString(), v) }
        val roundsObj = JSONObject()
        score.roundsWon.forEach { (k, v) -> roundsObj.put(k.toString(), v) }
        put("scores", scoresObj)
        put("roundsWon", roundsObj)
        put("targetScore", score.targetScore)
        put("currentRound", score.currentRound)
        put("matchWinnerId", score.matchWinnerId ?: -1)
    }

    private fun matchScoreFromJson(obj: JSONObject): MatchScore {
        val scores = mutableMapOf<Int, Int>()
        val scoresObj = obj.getJSONObject("scores")
        scoresObj.keys().forEach { k -> scores[k.toInt()] = scoresObj.getInt(k) }
        val rounds = mutableMapOf<Int, Int>()
        val roundsObj = obj.getJSONObject("roundsWon")
        roundsObj.keys().forEach { k -> rounds[k.toInt()] = roundsObj.getInt(k) }
        return MatchScore(
            scores = scores,
            roundsWon = rounds,
            targetScore = obj.getInt("targetScore"),
            currentRound = obj.getInt("currentRound"),
            matchWinnerId = obj.getInt("matchWinnerId").takeIf { it >= 0 }
        )
    }
}

object GameActionSerializer {

    fun toJson(action: GameAction): JSONObject = when (action) {
        is GameAction.PlayTile -> JSONObject().apply {
            put("type", "PLAY_TILE")
            put("playerId", action.playerId)
            put("tileId", action.tile.id)
            put("tileTop", action.tile.top)
            put("tileBot", action.tile.bottom)
            put("side", action.side.name)
        }
        is GameAction.DrawTile -> JSONObject().apply {
            put("type", "DRAW")
            put("playerId", action.playerId)
        }
        is GameAction.PassTurn -> JSONObject().apply {
            put("type", "PASS")
            put("playerId", action.playerId)
        }
        is GameAction.WinRound -> JSONObject().apply {
            put("type", "WIN_ROUND")
            put("winnerId", action.winnerId)
        }
    }

    fun fromJson(obj: JSONObject): GameAction? = try {
        when (obj.getString("type")) {
            "PLAY_TILE" -> GameAction.PlayTile(
                playerId = obj.getInt("playerId"),
                tile = DominoTile(obj.getInt("tileTop"), obj.getInt("tileBot"), obj.getInt("tileId")),
                side = BoardSide.valueOf(obj.getString("side"))
            )
            "DRAW" -> GameAction.DrawTile(obj.getInt("playerId"), null)
            "PASS" -> GameAction.PassTurn(obj.getInt("playerId"))
            "WIN_ROUND" -> GameAction.WinRound(obj.getInt("winnerId"), emptyMap())
            else -> null
        }
    } catch (e: Exception) { null }
}
