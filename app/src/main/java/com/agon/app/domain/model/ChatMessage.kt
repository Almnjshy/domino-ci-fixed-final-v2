package com.agon.app.domain.model

/**
 * Represents a chat message in network multiplayer mode
 */
data class ChatMessage(
    val senderId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
