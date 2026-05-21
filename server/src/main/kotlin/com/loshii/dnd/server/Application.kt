package com.loshii.dnd.server

import com.loshii.dnd.server.game.GameWorld
import com.loshii.dnd.server.models.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

val gameWorld = GameWorld(width = 800f, height = 1600f)
val playerSessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(30)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(CallLogging) {
        // Enable for debugging
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "Error: ${cause.message}", status = io.ktor.http.HttpStatusCode.InternalServerError)
        }
    }

    routing {
        get("/") {
            call.respondText("D&D MMO Server - WebSocket endpoint: /ws")
        }

        get("/health") {
            call.respondText("OK")
        }

        get("/players") {
            call.respond(gameWorld.players.value.values.map { p ->
                mapOf(
                    "id" to p.id,
                    "name" to p.name,
                    "x" to p.x,
                    "y" to p.y,
                    "hp" to p.hp,
                    "maxHp" to p.maxHp,
                    "level" to p.level,
                    "isDead" to p.isDead
                )
            })
        }

        webSocket("/ws") {
            handleWebSocket(this)
        }
    }
}

suspend fun handleWebSocket(session: DefaultWebSocketServerSession) {
    var playerId: String? = null

    try {
        for (frame in session.incoming) {
            frame as? Frame.Text ?: continue
            val raw = frame.readText()
            val message = Json.decodeFromString<ClientMessage>(raw)

            when (message) {
                is ClientMessage.Join -> {
                    playerId = message.playerId
                    playerSessions[playerId] = session
                    val player = gameWorld.addPlayer(playerId, message.playerName, message.level, message.maxHp)

                    session.sendSerialized(ServerMessage.Welcome(playerId, System.currentTimeMillis()))
                    session.sendSerialized(gameWorld.getWorldState())

                    broadcast(
                        ServerMessage.PlayerJoined(playerId, message.playerName, player.x, player.y, message.level),
                        exclude = playerId
                    )
                }

                is ClientMessage.Move -> {
                    playerId?.let { pid ->
                        gameWorld.movePlayer(pid, message.x, message.y)
                        broadcast(
                            ServerMessage.PlayerMoved(pid, message.x, message.y),
                            exclude = pid
                        )
                    }
                }

                is ClientMessage.Attack -> {
                    playerId?.let { pid ->
                        val result = gameWorld.attackMonster(pid, message.monsterId)
                        result?.let { msg ->
                            broadcast(msg)
                            if (msg is ServerMessage.MonsterDied) {
                                session.sendSerialized(msg)
                            }
                        }
                    }
                }

                is ClientMessage.Respawn -> {
                    playerId?.let { pid ->
                        gameWorld.respawnPlayer(pid)
                        val player = gameWorld.players.value[pid]
                        if (player != null) {
                            session.sendSerialized(
                                ServerMessage.PlayerRespawned(pid, player.x, player.y, player.hp, player.maxHp)
                            )
                        }
                    }
                }

                is ClientMessage.Chat -> {
                    playerId?.let { pid ->
                        val player = gameWorld.players.value[pid]
                        if (player != null) {
                            broadcast(ServerMessage.ChatMessage(pid, player.name, message.message))
                        }
                    }
                }

                is ClientMessage.Ping -> {
                    session.sendSerialized(ServerMessage.Welcome(playerId ?: "unknown", System.currentTimeMillis()))
                }
            }
        }
    } catch (e: Exception) {
        println("WebSocket error: ${e.message}")
    } finally {
        playerId?.let { pid ->
            playerSessions.remove(pid)
            gameWorld.removePlayer(pid)
            broadcast(ServerMessage.PlayerLeft(pid), exclude = pid)
        }
    }
}

suspend fun DefaultWebSocketServerSession.sendSerialized(message: ServerMessage) {
    val json = Json.encodeToString(ServerMessage.serializer(), message)
    send(Frame.Text(json))
}

suspend fun broadcast(message: ServerMessage, exclude: String? = null) {
    val json = Json.encodeToString(ServerMessage.serializer(), message)
    playerSessions.forEach { (id, session) ->
        if (id != exclude) {
            try {
                session.send(Frame.Text(json))
            } catch (e: Exception) {
                println("Failed to send to $id: ${e.message}")
            }
        }
    }
}
