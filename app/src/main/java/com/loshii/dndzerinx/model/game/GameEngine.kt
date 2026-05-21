package com.loshii.dndzerinx.model.game

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

sealed interface GameEvent {
    data class MonsterDamaged(val monsterId: String, val damage: Int, val isCrit: Boolean, val x: Float, val y: Float) : GameEvent
    data class PlayerDamaged(val damage: Int, val currentHp: Int) : GameEvent
    data class MonsterKilled(val monsterId: String, val xpReward: Int, val goldReward: Int, val x: Float, val y: Float) : GameEvent
    data class PlayerRespawned(val x: Float, val y: Float, val hp: Int) : GameEvent
    data class LevelUp(val newLevel: Int) : GameEvent
}

data class PlayerStats(
    var level: Int = 1, var maxHp: Int = 100, var atk: Int = 10,
    var def: Int = 5, var gold: Int = 0, var xp: Int = 0
)

data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, var maxLife: Float,
    var color: Long, var size: Float,
    var alpha: Float = 1f
) {
    val alive: Boolean get() = life > 0f
}

data class AttackSwing(
    var x: Float, var y: Float,
    var angle: Float, var radius: Float,
    var life: Float, val maxLife: Float,
    var color: Long
) {
    val alive: Boolean get() = life > 0f
    val progress: Float get() = 1f - (life / maxLife)
}

enum class TileType {
    GRASS, DIRT, STONE, WATER, SAND, DARK_STONE
}

data class TileMap(
    val width: Int, val height: Int,
    val tileSize: Int = 64,
    val tiles: Array<IntArray>
)

open class GameEngine(
    bounds: WorldBounds,
    playerLevel: Int = 1,
    playerMaxHp: Int = 100,
    playerAtk: Int = 10,
    playerDef: Int = 5,
    val seed: Int = Random.nextInt()
) {
    var playerPosition = Vector2(bounds.width / 2, bounds.height / 2)
    var playerHp: Int = playerMaxHp
    var playerState: EntityState = EntityState.IDLE
    var playerLastAttackTime: Long = 0
    var playerLastHitTime: Long = 0
    val playerAttackCooldown: Long = 600
    val playerAttackRange: Float = 65f
    val playerSpeed: Float = 140f
    var playerXp: Int = 0
    var playerGold: Int = 0
    var playerLevel: Int = playerLevel
    var playerAtk: Int = playerAtk
    var playerDef: Int = playerDef
    var playerMaxHp: Int = playerMaxHp

    val monsters = mutableListOf<Monster>()
    val damageNumbers = mutableListOf<DamageNumber>()
    val particles = mutableListOf<Particle>()
    val attackSwings = mutableListOf<AttackSwing>()

    var playerFlickerTimer: Float = 0f
    var playerHitFlash: Boolean = false
    var screenShakeTimer: Float = 0f
    var screenShakeIntensity: Float = 0f

    var cameraX: Float = 0f
    var cameraY: Float = 0f
    var cameraTargetX: Float = 0f
    var cameraTargetY: Float = 0f

    val tileMap: TileMap = generateTileMap(bounds.width.toInt(), bounds.height.toInt(), seed)
    val bounds: WorldBounds = bounds

    val events = mutableListOf<GameEvent>()

    private val rng = Random(seed)

    init {
        spawnInitialMonsters()
    }

    private fun generateTileMap(width: Int, height: Int, seed: Int): TileMap {
        val tileSize = 64
        val tw = width / tileSize + 2
        val th = height / tileSize + 2
        val tiles = Array(th) { y ->
            IntArray(tw) { x ->
                val noise = (sin(x * 12.7 + seed) * cos(y * 9.3 + seed * 2) +
                        sin(x * 3.1 - y * 5.7 + seed * 3) * 0.5 +
                        cos(x * 7.5 + y * 2.1 + seed * 1.7) * 0.3)
                when {
                    noise < -0.3f -> TileType.WATER.ordinal
                    noise < 0.0f -> TileType.SAND.ordinal
                    noise < 0.5f -> TileType.GRASS.ordinal
                    noise < 0.8f -> TileType.DIRT.ordinal
                    else -> TileType.STONE.ordinal
                }
            }
        }
        return TileMap(tw, th, tileSize, tiles)
    }

    private fun spawnInitialMonsters() {
        val r = rng
        val commonTypes = MonsterType.values().filter { it.name.contains("RAT") || it.name.contains("SPIDER") || it.name.contains("SLIME") || it.name.contains("BAT") || it.name.contains("GOBLIN") }
        for (i in 0 until 8) {
            val type = if (commonTypes.isNotEmpty()) commonTypes[r.nextInt(commonTypes.size)] else MonsterType.values()[r.nextInt(MonsterType.values().size)]
            spawnMonster(type)
        }
        for (i in 0 until 4) {
            val type = MonsterType.values()[r.nextInt(MonsterType.values().size)]
            if (type.xpReward > 200) continue
            spawnMonster(type)
        }
        val epic = MonsterType.values().firstOrNull { it.xpReward > 500 }
        if (epic != null) spawnMonster(epic)
    }

    fun spawnMonster(type: MonsterType) {
        val tx = rng.nextInt(tileMap.width)
        val ty = rng.nextInt(tileMap.height)
        val px = tx * tileMap.tileSize + rng.nextInt(tileMap.tileSize - 40) + 20f
        val py = ty * tileMap.tileSize + rng.nextInt(tileMap.tileSize - 40) + 20f
        val id = "m_${System.nanoTime()}_${monsters.size}"
        val patrolTarget = Vector2(px + rng.nextFloat() * 200f - 100f, py + rng.nextFloat() * 200f - 100f)
        monsters.add(Monster(id, type, Vector2(px, py), patrolTarget))
    }

    fun update(deltaTime: Float, currentTime: Long) {
        if (playerHp <= 0) return

        updateMonsters(deltaTime, currentTime)
        checkMonsterSpawns(currentTime)
        updateParticles(deltaTime)
        updateAttackSwings(deltaTime)
        updateScreenShake(deltaTime)
        updateHitFlash(deltaTime)
        updateCamera(deltaTime)
    }

    fun movePlayer(direction: Vector2, deltaTime: Float) {
        if (playerHp <= 0) return
        val dir = direction.normalized()
        val move = dir * (playerSpeed * deltaTime)
        val newX = (playerPosition.x + move.x).coerceIn(20f, bounds.width - 20f)
        val newY = (playerPosition.y + move.y).coerceIn(20f, bounds.height - 20f)
        playerPosition = Vector2(newX, newY)
        playerState = if (direction.length() > 0.1f) EntityState.PATROL else EntityState.IDLE
    }

    open fun attackNearestMonster(currentTime: Long): Boolean {
        if (playerHp <= 0 || currentTime - playerLastAttackTime < playerAttackCooldown) return false

        val nearest = monsters
            .filter { !it.isDead() && playerPosition.distanceTo(it.position) < playerAttackRange + 40f }
            .minByOrNull { playerPosition.distanceTo(it.position) }

        if (nearest != null) {
            playerLastAttackTime = currentTime
            playerState = EntityState.ATTACK
            val damage = calculateDamage(playerAtk, nearest.type.baseDef)
            val isCrit = rng.nextInt(100) < 15
            val finalDamage = if (isCrit) (damage * 1.5f).toInt() else damage

            nearest.hp -= finalDamage

            val swingDir = (nearest.position - playerPosition).normalized()
            val angle = kotlin.math.atan2(swingDir.y.toDouble(), swingDir.x.toDouble()).toFloat()
            attackSwings.add(AttackSwing(
                x = playerPosition.x, y = playerPosition.y,
                angle = angle, radius = playerAttackRange,
                life = 0.3f, maxLife = 0.3f,
                color = if (isCrit) 0xFFFFD700.toLong() else 0xFFFFFFFF.toLong()
            ))

            damageNumbers.add(DamageNumber(
                position = Vector2(nearest.position.x, nearest.position.y - 30f),
                value = finalDamage, isCrit = isCrit,
                startTime = currentTime, color = if (isCrit) 0xFFFFD700.toLong() else 0xFFFF4444.toLong()
            ))

            spawnHitParticles(nearest.position.x, nearest.position.y, isCrit)

            if (isCrit) screenShake(0.15f, 4f)

            if (nearest.isDead()) {
                events.add(GameEvent.MonsterKilled(nearest.id, nearest.type.xpReward, nearest.type.baseHp / 10, nearest.position.x, nearest.position.y))
                playerXp += nearest.type.xpReward
                playerGold += nearest.type.baseHp / 10
                spawnDeathParticles(nearest.position.x, nearest.position.y)
                nearest.respawnTimer = currentTime + 8000
            }

            events.add(GameEvent.MonsterDamaged(nearest.id, finalDamage, isCrit, nearest.position.x, nearest.position.y))
            return true
        }
        return false
    }

    private fun calculateDamage(atk: Int, def: Int): Int {
        val base = (atk * 1.5f - def * 0.5f).toInt()
        val variance = (base * 0.2f).toInt().coerceAtLeast(1)
        return (base + rng.nextInt(-variance, variance + 1)).coerceAtLeast(1)
    }

    private fun updateMonsters(deltaTime: Float, currentTime: Long) {
        for (monster in monsters) {
            when {
                monster.isDead() -> continue
                monster.hp <= 0 -> {}
                else -> {
                    val dist = playerPosition.distanceTo(monster.position)
                    when {
                        dist < monster.type.aggressionRange && monster.state != EntityState.ATTACK -> {
                            monster.state = EntityState.CHASE
                        }
                        monster.state == EntityState.CHASE -> {
                            moveToward(monster, playerPosition, monster.speed, deltaTime)
                            if (dist < monster.attackRange) {
                                monster.state = EntityState.ATTACK
                                monster.lastAttackTime = currentTime
                            }
                        }
                        monster.state == EntityState.ATTACK -> {
                            if (currentTime - monster.lastAttackTime > monster.attackCooldown) {
                                if (dist < monster.attackRange * 1.5f) {
                                    val dmg = calculateDamage(monster.type.baseAtk, playerDef)
                                    playerHp -= dmg
                                    playerLastHitTime = currentTime
                                    playerHitFlash = true
                                    playerFlickerTimer = 0.3f

                                    damageNumbers.add(DamageNumber(
                                        position = Vector2(playerPosition.x + 20f, playerPosition.y - 40f),
                                        value = dmg, isCrit = false,
                                        startTime = currentTime, color = 0xFFFF4444.toLong()
                                    ))

                                    screenShake(0.1f, 3f)
                                    events.add(GameEvent.PlayerDamaged(dmg, playerHp))

                                    if (playerHp <= 0) {
                                        events.add(GameEvent.MonsterKilled(monster.id, 0, 0, monster.position.x, monster.position.y))
                                    }
                                } else {
                                    monster.state = EntityState.CHASE
                                }
                                monster.lastAttackTime = currentTime
                            }
                        }
                        monster.state == EntityState.PATROL -> {
                            moveToward(monster, monster.patrolTarget, monster.speed * 0.4f, deltaTime)
                            if (monster.position.distanceTo(monster.patrolTarget) < 20f) {
                                monster.patrolTarget = Vector2(
                                    monster.position.x + rng.nextFloat() * 300f - 150f,
                                    monster.position.y + rng.nextFloat() * 300f - 150f
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun moveToward(monster: Monster, target: Vector2, speed: Float, deltaTime: Float) {
        val dir = (target - monster.position).normalized()
        monster.position += dir * (speed * deltaTime)
    }

    private fun checkMonsterSpawns(currentTime: Long) {
        val aliveCount = monsters.count { !it.isDead() }
        if (aliveCount < 8 && rng.nextInt(200) == 0) {
            val types = MonsterType.values()
            spawnMonster(types[rng.nextInt(types.size)])
        }
        for (monster in monsters) {
            if (monster.isDead() && monster.respawnTimer > 0 && currentTime >= monster.respawnTimer) {
                monster.hp = monster.type.baseHp
                monster.state = EntityState.PATROL
                monster.position = Vector2(
                    rng.nextInt(bounds.width.toInt() - 100) + 50f,
                    rng.nextInt(bounds.height.toInt() - 100) + 50f
                )
                monster.respawnTimer = 0
            }
        }
    }

    fun respawnPlayer() {
        playerHp = playerMaxHp
        playerPosition = Vector2(bounds.width / 2, bounds.height / 2)
        playerState = EntityState.IDLE
        events.add(GameEvent.PlayerRespawned(bounds.width / 2, bounds.height / 2, playerMaxHp))
    }

    fun gainXp(amount: Int): Boolean {
        playerXp += amount
        val xpNeeded = playerLevel * 100
        if (playerXp >= xpNeeded) {
            playerXp -= xpNeeded
            playerLevel++
            playerMaxHp += 10
            playerHp = playerMaxHp
            playerAtk += 2
            playerDef += 1
            events.add(GameEvent.LevelUp(playerLevel))
            spawnLevelUpParticles()
            return true
        }
        return false
    }

    fun screenShake(duration: Float, intensity: Float) {
        screenShakeTimer = duration
        screenShakeIntensity = intensity
    }

    private fun updateScreenShake(deltaTime: Float) {
        if (screenShakeTimer > 0f) {
            screenShakeTimer -= deltaTime
            val intensity = screenShakeIntensity * (screenShakeTimer / screenShakeTimer.coerceAtLeast(0.001f))
            cameraTargetX += rng.nextFloat() * intensity - intensity / 2f
            cameraTargetY += rng.nextFloat() * intensity - intensity / 2f
        }
    }

    private fun updateHitFlash(deltaTime: Float) {
        if (playerFlickerTimer > 0f) {
            playerFlickerTimer -= deltaTime
            playerHitFlash = (playerFlickerTimer * 10f).toInt() % 2 == 0
        } else {
            playerHitFlash = false
        }
    }

    private fun updateCamera(deltaTime: Float) {
        cameraTargetX = playerPosition.x
        cameraTargetY = playerPosition.y
        val lerpSpeed = 5f
        cameraX += (cameraTargetX - cameraX) * lerpSpeed * deltaTime.coerceAtMost(0.1f)
        cameraY += (cameraTargetY - cameraY) * lerpSpeed * deltaTime.coerceAtMost(0.1f)
    }

    private fun updateParticles(deltaTime: Float) {
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.x += p.vx * deltaTime
            p.y += p.vy * deltaTime
            p.vy += 100f * deltaTime
            p.life -= deltaTime
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (!p.alive) it.remove()
        }
    }

    private fun updateAttackSwings(deltaTime: Float) {
        val it = attackSwings.iterator()
        while (it.hasNext()) {
            val s = it.next()
            s.life -= deltaTime
            if (!s.alive) it.remove()
        }
    }

    private fun spawnHitParticles(x: Float, y: Float, crit: Boolean) {
        val count = if (crit) 15 else 6
        val color = if (crit) 0xFFFFD700.toLong() else 0xFFFF6666.toLong()
        repeat(count) {
            val angle = rng.nextFloat() * Math.PI.toFloat() * 2f
            val speed = rng.nextFloat() * (if (crit) 200f else 120f) + 40f
            particles.add(Particle(
                x = x, y = y,
                vx = cos(angle.toDouble()).toFloat() * speed,
                vy = sin(angle.toDouble()).toFloat() * speed - 30f,
                life = rng.nextFloat() * 0.5f + 0.3f,
                maxLife = 0.8f,
                color = color,
                size = rng.nextFloat() * (if (crit) 6f else 4f) + 2f
            ))
        }
    }

    private fun spawnDeathParticles(x: Float, y: Float) {
        repeat(20) {
            val angle = rng.nextFloat() * Math.PI.toFloat() * 2f
            val speed = rng.nextFloat() * 150f + 50f
            val colors = listOf(0xFF666666.toLong(), 0xFF888888.toLong(), 0xFF444444.toLong(), 0xFFFF4444.toLong())
            particles.add(Particle(
                x = x, y = y,
                vx = cos(angle.toDouble()).toFloat() * speed,
                vy = sin(angle.toDouble()).toFloat() * speed - 50f,
                life = rng.nextFloat() * 0.8f + 0.4f,
                maxLife = 1.2f,
                color = colors[rng.nextInt(colors.size)],
                size = rng.nextFloat() * 5f + 2f
            ))
        }
    }

    private fun spawnLevelUpParticles() {
        val x = playerPosition.x
        val y = playerPosition.y - 40f
        repeat(30) {
            val angle = rng.nextFloat() * Math.PI.toFloat() * 2f
            val speed = rng.nextFloat() * 250f + 100f
            particles.add(Particle(
                x = x, y = y,
                vx = cos(angle.toDouble()).toFloat() * speed,
                vy = sin(angle.toDouble()).toFloat() * speed - 80f,
                life = rng.nextFloat() * 1.0f + 0.5f,
                maxLife = 1.5f,
                color = 0xFFFFD700.toLong(),
                size = rng.nextFloat() * 6f + 3f
            ))
        }
    }

    fun clearEvents() {
        events.clear()
    }

    fun getShakeOffset(): Pair<Float, Float> {
        if (screenShakeTimer <= 0f) return Pair(0f, 0f)
        return Pair(
            rng.nextFloat() * screenShakeIntensity - screenShakeIntensity / 2f,
            rng.nextFloat() * screenShakeIntensity - screenShakeIntensity / 2f
        )
    }
}
