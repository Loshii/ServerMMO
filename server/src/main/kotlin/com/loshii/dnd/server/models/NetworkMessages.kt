package com.loshii.dnd.server.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ServerMessage {
    @Serializable
    data class Welcome(val playerId: String, val worldTime: Long) : ServerMessage()

    @Serializable
    data class PlayerJoined(val playerId: String, val name: String, val x: Float, val y: Float, val level: Int) : ServerMessage()

    @Serializable
    data class PlayerLeft(val playerId: String) : ServerMessage()

    @Serializable
    data class PlayerMoved(val playerId: String, val x: Float, val y: Float) : ServerMessage()

    @Serializable
    data class PlayerAttacked(val playerId: String, val monsterId: String, val damage: Int, val isCrit: Boolean) : ServerMessage()

    @Serializable
    data class MonsterDamaged(val monsterId: String, val damage: Int, val currentHp: Int, val maxHp: Int) : ServerMessage()

    @Serializable
    data class MonsterDied(val monsterId: String, val killedBy: String, val xpReward: Int, val goldReward: Int) : ServerMessage()

    @Serializable
    data class MonsterSpawned(val monsterId: String, val type: String, val x: Float, val y: Float, val level: Int, val hp: Int, val maxHp: Int) : ServerMessage()

    @Serializable
    data class PlayerDamaged(val playerId: String, val damage: Int, val currentHp: Int, val maxHp: Int, val attackerMonsterId: String) : ServerMessage()

    @Serializable
    data class PlayerDied(val playerId: String) : ServerMessage()

    @Serializable
    data class PlayerRespawned(val playerId: String, val x: Float, val y: Float, val hp: Int, val maxHp: Int) : ServerMessage()

    @Serializable
    data class WorldState(
        val players: List<PlayerState>,
        val monsters: List<MonsterState>
    ) : ServerMessage()

    @Serializable
    data class ChatMessage(val playerId: String, val playerName: String, val message: String) : ServerMessage()

    @Serializable
    data class Error(val reason: String) : ServerMessage()
}

@Serializable
data class PlayerState(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val hp: Int,
    val maxHp: Int,
    val level: Int,
    val isDead: Boolean
)

@Serializable
data class MonsterState(
    val id: String,
    val type: String,
    val x: Float,
    val y: Float,
    val hp: Int,
    val maxHp: Int,
    val level: Int,
    val isDead: Boolean
)
