package com.loshii.dnd.server.game

import com.loshii.dnd.server.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.sqrt
import kotlin.random.Random

data class ServerPlayer(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var hp: Int,
    val maxHp: Int,
    val level: Int,
    var isDead: Boolean = false,
    var lastAttackTime: Long = 0
) {
    val attackRange: Float = 60f
    val attackCooldown: Long = 800
}

data class ServerMonster(
    val id: String,
    val type: MonsterType,
    var x: Float,
    var y: Float,
    var hp: Int,
    val maxHp: Int,
    val level: Int,
    var isDead: Boolean = false,
    var state: MonsterState = MonsterState.PATROL,
    var patrolTarget: Pair<Float, Float>,
    var lastAttackTime: Long = 0,
    var respawnTime: Long = 0
) {
    val aggressionRange: Float = type.aggressionRange
    val attackRange: Float = 40f
    val attackCooldown: Long = 1500
    val speed: Float = 60f
    val patrolRadius: Float = 200f
}

enum class MonsterState { IDLE, PATROL, CHASE, ATTACK, DEAD }

enum class MonsterType(
    val displayName: String,
    val baseHp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val xpReward: Int,
    val aggressionRange: Float
) {
    SLIME("Slime", 30, 5, 2, 10, 150f),
    WOLF("Lobo", 50, 10, 5, 25, 200f),
    BEAR("Oso", 100, 15, 10, 50, 180f),
    SKELETON("Esqueleto", 70, 12, 8, 35, 250f),
    DRAGON("Dragón", 200, 25, 15, 100, 300f)
}

class GameWorld(
    val width: Float = 800f,
    val height: Float = 1600f
) {
    private val _players = MutableStateFlow<Map<String, ServerPlayer>>(emptyMap())
    val players: StateFlow<Map<String, ServerPlayer>> = _players.asStateFlow()

    private val monsters = mutableListOf<ServerMonster>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var lastSpawnTime: Long = 0
    val spawnInterval: Long = 5000
    val maxMonsters = 15

    init {
        spawnInitialMonsters()
        startGameLoop()
    }

    private fun spawnInitialMonsters() {
        val types = listOf(MonsterType.SLIME, MonsterType.WOLF, MonsterType.SKELETON)
        repeat(8) {
            spawnMonster(types.random())
        }
    }

    fun addPlayer(playerId: String, name: String, level: Int, maxHp: Int): ServerPlayer {
        val player = ServerPlayer(
            id = playerId,
            name = name,
            x = width / 2 + Random.nextFloat() * 200 - 100,
            y = height / 2 + Random.nextFloat() * 200 - 100,
            hp = maxHp,
            maxHp = maxHp,
            level = level
        )
        _players.value = _players.value + (playerId to player)
        return player
    }

    fun removePlayer(playerId: String) {
        _players.value = _players.value - playerId
    }

    fun movePlayer(playerId: String, x: Float, y: Float) {
        val player = _players.value[playerId] ?: return
        if (player.isDead) return
        _players.value = _players.value + (playerId to player.copy(
            x = x.coerceIn(20f, width - 20f),
            y = y.coerceIn(20f, height - 20f)
        ))
    }

    fun attackMonster(playerId: String, monsterId: String): ServerMessage? {
        val player = _players.value[playerId] ?: return null
        if (player.isDead) return null

        val currentTime = System.currentTimeMillis()
        if (currentTime - player.lastAttackTime < player.attackCooldown) return null

        val monster = monsters.find { it.id == monsterId && !it.isDead } ?: return null
        val dist = distance(player.x, player.y, monster.x, monster.y)
        if (dist > player.attackRange) return null

        player.lastAttackTime = currentTime
        val damage = calculateDamage(10 + player.level * 2, monster.type.baseDef)
        val isCrit = Random.nextFloat() < 0.15f
        val finalDamage = if (isCrit) damage * 2 else damage
        monster.hp -= finalDamage

        val attackMsg = ServerMessage.PlayerAttacked(playerId, monsterId, finalDamage, isCrit)

        if (monster.hp <= 0) {
            monster.isDead = true
            monster.state = MonsterState.DEAD
            monster.respawnTime = currentTime + 10000
            return ServerMessage.MonsterDied(
                monsterId = monsterId,
                killedBy = playerId,
                xpReward = monster.type.xpReward * monster.level,
                goldReward = Random.nextInt(1, 10) * monster.level
            )
        }

        return attackMsg
    }

    fun respawnPlayer(playerId: String) {
        val player = _players.value[playerId] ?: return
        _players.value = _players.value + (playerId to player.copy(
            x = width / 2,
            y = height / 2,
            hp = player.maxHp,
            isDead = false
        ))
    }

    private fun startGameLoop() {
        scope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                updateMonsters(currentTime)
                checkSpawns(currentTime)
                delay(50)
            }
        }
    }

    private fun updateMonsters(currentTime: Long) {
        val currentPlayers = _players.value.values.filter { !it.isDead }

        monsters.forEach { monster ->
            if (monster.isDead) return@forEach

            val nearestPlayer = currentPlayers.minByOrNull {
                distance(monster.x, monster.y, it.x, it.y)
            } ?: return@forEach

            val distToPlayer = distance(monster.x, monster.y, nearestPlayer.x, nearestPlayer.y)

            when (monster.state) {
                MonsterState.PATROL -> {
                    if (distToPlayer < monster.aggressionRange) {
                        monster.state = MonsterState.CHASE
                    } else {
                        val (tx, ty) = monster.patrolTarget
                        moveToward(monster, tx, ty, monster.speed * 0.4f)
                        if (distance(monster.x, monster.y, tx, ty) < 20f) {
                            monster.patrolTarget = Pair(
                                (monster.x + Random.nextFloat() * monster.patrolRadius * 2 - monster.patrolRadius).coerceIn(20f, width - 20f),
                                (monster.y + Random.nextFloat() * monster.patrolRadius * 2 - monster.patrolRadius).coerceIn(20f, height - 20f)
                            )
                        }
                    }
                }
                MonsterState.CHASE -> {
                    if (distToPlayer > monster.aggressionRange * 2) {
                        monster.state = MonsterState.PATROL
                    } else if (distToPlayer <= monster.attackRange) {
                        monster.state = MonsterState.ATTACK
                        if (currentTime - monster.lastAttackTime >= monster.attackCooldown) {
                            monster.lastAttackTime = currentTime
                            val damage = calculateDamage(monster.type.baseAtk + monster.level * 2, 5).coerceAtLeast(1)
                            val player = _players.value[nearestPlayer.id]
                            if (player != null) {
                                player.hp -= damage
                                if (player.hp <= 0) {
                                    player.hp = 0
                                    player.isDead = true
                                }
                                _players.value = _players.value + (nearestPlayer.id to player)
                            }
                        }
                    } else {
                        moveToward(monster, nearestPlayer.x, nearestPlayer.y, monster.speed)
                    }
                }
                MonsterState.ATTACK -> {
                    if (distToPlayer > monster.attackRange * 1.5f) {
                        monster.state = MonsterState.CHASE
                    }
                }
                else -> {}
            }
        }
    }

    private fun moveToward(monster: ServerMonster, tx: Float, ty: Float, speed: Float) {
        val dx = tx - monster.x
        val dy = ty - monster.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > 0) {
            monster.x = (monster.x + (dx / dist) * speed * 0.05f).coerceIn(20f, width - 20f)
            monster.y = (monster.y + (dy / dist) * speed * 0.05f).coerceIn(20f, height - 20f)
        }
    }

    private fun checkSpawns(currentTime: Long) {
        if (currentTime - lastSpawnTime > spawnInterval) {
            lastSpawnTime = currentTime
            val aliveCount = monsters.count { !it.isDead }
            if (aliveCount < maxMonsters) {
                val types = listOf(MonsterType.SLIME, MonsterType.WOLF, MonsterType.SKELETON, MonsterType.BEAR)
                spawnMonster(types.random())
            }
        }

        monsters.forEach { monster ->
            if (monster.isDead && currentTime >= monster.respawnTime) {
                monster.hp = monster.maxHp
                monster.isDead = false
                monster.state = MonsterState.PATROL
                monster.x = Random.nextFloat() * (width - 100) + 50
                monster.y = Random.nextFloat() * (height - 100) + 50
                monster.patrolTarget = Pair(
                    (monster.x + Random.nextFloat() * 200 - 100).coerceIn(20f, width - 20f),
                    (monster.y + Random.nextFloat() * 200 - 100).coerceIn(20f, height - 20f)
                )
            }
        }
    }

    private fun spawnMonster(type: MonsterType): ServerMonster {
        val level = Random.nextInt(1, 4)
        val monster = ServerMonster(
            id = "m_${UUID.randomUUID().toString().take(8)}",
            type = type,
            x = Random.nextFloat() * (width - 100) + 50,
            y = Random.nextFloat() * (height - 100) + 50,
            hp = type.baseHp + (level - 1) * 10,
            maxHp = type.baseHp + (level - 1) * 10,
            level = level,
            patrolTarget = Pair(
                Random.nextFloat() * width,
                Random.nextFloat() * height
            )
        )
        monsters.add(monster)
        return monster
    }

    fun getWorldState(): ServerMessage.WorldState {
        return ServerMessage.WorldState(
            players = _players.value.values.map { p ->
                PlayerState(p.id, p.name, p.x, p.y, p.hp, p.maxHp, p.level, p.isDead)
            },
            monsters = monsters.map { m ->
                MonsterState(m.id, m.type.displayName, m.x, m.y, m.hp, m.maxHp, m.level, m.isDead)
            }
        )
    }

    fun getAliveMonsters() = monsters.filter { !it.isDead }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculateDamage(atk: Int, def: Int): Int {
        return (atk * 1.5f - def * 0.5f).toInt().coerceAtLeast(1)
    }

    fun shutdown() {
        scope.cancel()
    }
}
