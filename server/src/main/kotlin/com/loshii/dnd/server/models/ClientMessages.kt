package com.loshii.dnd.server.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ClientMessage {
    @Serializable
    data class Join(val playerId: String, val playerName: String, val level: Int, val maxHp: Int) : ClientMessage()

    @Serializable
    data class Move(val x: Float, val y: Float) : ClientMessage()

    @Serializable
    data class Attack(val monsterId: String) : ClientMessage()

    @Serializable
    object Respawn : ClientMessage()

    @Serializable
    data class Chat(val message: String) : ClientMessage()

    @Serializable
    object Ping : ClientMessage()
}
