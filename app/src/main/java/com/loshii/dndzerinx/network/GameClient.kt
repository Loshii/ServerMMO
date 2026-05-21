package com.loshii.dndzerinx.network

import android.util.Log
import com.loshii.dndzerinx.model.game.MonsterType
import com.loshii.dndzerinx.model.game.Vector2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.WebSocket
import okio.ByteString
import java.util.concurrent.TimeUnit

@Serializable
sealed class ServerMessage {
    @Serializable data class Welcome(val playerId: String, val worldTime: Long) : ServerMessage()
    @Serializable data class PlayerJoined(val playerId: String, val name: String, val x: Float, val y: Float, val level: Int) : ServerMessage()
    @Serializable data class PlayerLeft(val playerId: String) : ServerMessage()
    @Serializable data class PlayerMoved(val playerId: String, val x: Float, val y: Float) : ServerMessage()
    @Serializable data class PlayerAttacked(val playerId: String, val monsterId: String, val damage: Int, val isCrit: Boolean) : ServerMessage()
    @Serializable data class MonsterDied(val monsterId: String, val killedBy: String, val xpReward: Int, val goldReward: Int) : ServerMessage()
    @Serializable data class MonsterSpawned(val monsterId: String, val type: String, val x: Float, val y: Float, val level: Int, val hp: Int, val maxHp: Int) : ServerMessage()
    @Serializable data class PlayerDamaged(val playerId: String, val damage: Int, val currentHp: Int, val maxHp: Int, val attackerMonsterId: String) : ServerMessage()
    @Serializable data class PlayerRespawned(val playerId: String, val x: Float, val y: Float, val hp: Int, val maxHp: Int) : ServerMessage()
    @Serializable data class WorldState(val players: List<PlayerState>, val monsters: List<MonsterState>) : ServerMessage()
    @Serializable data class ChatMessage(val playerId: String, val playerName: String, val message: String) : ServerMessage()
    @Serializable data class Error(val reason: String) : ServerMessage()
}

@Serializable
data class PlayerState(val id: String, val name: String, val x: Float, val y: Float, val hp: Int, val maxHp: Int, val level: Int, val isDead: Boolean)

@Serializable
data class MonsterState(val id: String, val type: String, val x: Float, val y: Float, val hp: Int, val maxHp: Int, val level: Int, val isDead: Boolean)

@Serializable
sealed class ClientMessage {
    @Serializable data class Join(val playerId: String, val playerName: String, val level: Int, val maxHp: Int) : ClientMessage()
    @Serializable data class Move(val x: Float, val y: Float) : ClientMessage()
    @Serializable data class Attack(val monsterId: String) : ClientMessage()
    @Serializable object Respawn : ClientMessage()
    @Serializable data class Chat(val message: String) : ClientMessage()
    @Serializable object Ping : ClientMessage()
}

class GameClient {
    companion object {
        private const val TAG = "GameClient"
        private val json = Json { ignoreUnknownKeys = true }
    }

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _remotePlayers = MutableStateFlow<Map<String, PlayerState>>(emptyMap())
    val remotePlayers: StateFlow<Map<String, PlayerState>> = _remotePlayers.asStateFlow()

    private val _remoteMonsters = MutableStateFlow<Map<String, MonsterState>>(emptyMap())
    val remoteMonsters: StateFlow<Map<String, MonsterState>> = _remoteMonsters.asStateFlow()

    private val _messages = MutableStateFlow<List<ServerMessage>>(emptyList())
    val messages: StateFlow<List<ServerMessage>> = _messages.asStateFlow()

    var onMessage: ((ServerMessage) -> Unit)? = null

    fun connect(serverUrl: String, playerId: String, playerName: String, level: Int, maxHp: Int) {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
            .newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d(TAG, "WebSocket connected")
                    _connected.value = true
                    send(ClientMessage.Join(playerId, playerName, level, maxHp))
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val message = json.decodeFromString<ServerMessage>(text)
                        handleMessage(message)
                        onMessage?.invoke(message)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing message: ${e.message}")
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: $reason")
                    _connected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket error: ${t.message}")
                    _connected.value = false
                }
            })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connected.value = false
        scope.cancel()
    }

    fun sendMove(x: Float, y: Float) {
        send(ClientMessage.Move(x, y))
    }

    fun sendAttack(monsterId: String) {
        send(ClientMessage.Attack(monsterId))
    }

    fun sendRespawn() {
        send(ClientMessage.Respawn)
    }

    fun sendChat(message: String) {
        send(ClientMessage.Chat(message))
    }

    fun sendPing() {
        send(ClientMessage.Ping)
    }

    private fun send(message: ClientMessage) {
        if (_connected.value) {
            val jsonStr = json.encodeToString(message)
            webSocket?.send(jsonStr)
        }
    }

    private fun handleMessage(message: ServerMessage) {
        when (message) {
            is ServerMessage.WorldState -> {
                _remotePlayers.value = message.players.associateBy { it.id }
                _remoteMonsters.value = message.monsters.associateBy { it.id }
            }
            is ServerMessage.PlayerMoved -> {
                val players = _remotePlayers.value.toMutableMap()
                players[message.playerId]?.let {
                    players[message.playerId] = it.copy(x = message.x, y = message.y)
                }
                _remotePlayers.value = players
            }
            is ServerMessage.PlayerJoined -> {
                val players = _remotePlayers.value.toMutableMap()
                players[message.playerId] = PlayerState(
                    message.playerId, message.name, message.x, message.y,
                    100, 100, message.level, false
                )
                _remotePlayers.value = players
            }
            is ServerMessage.PlayerLeft -> {
                val players = _remotePlayers.value.toMutableMap()
                players.remove(message.playerId)
                _remotePlayers.value = players
            }
            is ServerMessage.MonsterSpawned -> {
                val monsters = _remoteMonsters.value.toMutableMap()
                monsters[message.monsterId] = MonsterState(
                    message.monsterId, message.type, message.x, message.y,
                    message.hp, message.maxHp, message.level, false
                )
                _remoteMonsters.value = monsters
            }
            is ServerMessage.MonsterDied -> {
                val monsters = _remoteMonsters.value.toMutableMap()
                monsters[message.monsterId]?.let {
                    monsters[message.monsterId] = it.copy(isDead = true)
                }
                _remoteMonsters.value = monsters
            }
            is ServerMessage.PlayerDamaged -> {
                // Local player damage handled by local game world
            }
            is ServerMessage.PlayerRespawned -> {
                // Local player respawn handled by local game world
            }
            else -> {}
        }

        _messages.value = _messages.value + message
    }
}
