package com.loshii.dndzerinx.model.game

import kotlin.random.Random

class GameWorld(
    val bounds: WorldBounds,
    val playerLevel: Int = 1,
    val playerMaxHp: Int = 100,
    val playerAtk: Int = 10,
    val playerDef: Int = 5
) {
    var playerPosition = Vector2(bounds.width / 2, bounds.height / 2)
    var playerHp = playerMaxHp
    var playerState = EntityState.IDLE
    var playerLastAttackTime: Long = 0
    val playerAttackCooldown: Long = 800
    val playerAttackRange: Float = 60f
    val playerSpeed: Float = 120f
    var playerXp = 0
    var playerGold = 0

    val monsters = mutableListOf<Monster>()
    val damageNumbers = mutableListOf<DamageNumber>()

    private var lastMonsterSpawnTime: Long = 0
    val monsterSpawnInterval: Long = 5000
    val maxMonsters = 15

    init {
        spawnInitialMonsters()
    }

    private fun spawnInitialMonsters() {
        val types = listOf(MonsterType.SLIME, MonsterType.WOLF, MonsterType.SKELETON)
        repeat(8) {
            spawnMonster(types.random())
        }
    }

    fun spawnMonster(type: MonsterType) {
        if (monsters.count { !it.isDead() } >= maxMonsters) return
        val pos = Vector2(
            Random.nextFloat() * (bounds.width - 100) + 50,
            Random.nextFloat() * (bounds.height - 100) + 50
        )
        val monsterLevel = (playerLevel + Random.nextInt(-1, 2)).coerceAtLeast(1)
        val monster = Monster(
            id = "monster_${System.currentTimeMillis()}_${Random.nextInt()}",
            type = type,
            position = pos,
            patrolTarget = pos + Vector2(Random.nextFloat() * 200 - 100, Random.nextFloat() * 200 - 100),
            hp = type.baseHp + (monsterLevel - 1) * 10,
            maxHp = type.baseHp + (monsterLevel - 1) * 10,
            level = monsterLevel
        )
        monsters.add(monster)
    }

    fun update(deltaTime: Float, currentTime: Long) {
        updateMonsters(deltaTime, currentTime)
        updatePlayer(currentTime)
        updateDamageNumbers(currentTime)
        checkMonsterSpawns(currentTime)
    }

    private fun updatePlayer(currentTime: Long) {
        if (playerHp <= 0) {
            playerState = EntityState.DEAD
        }
    }

    fun movePlayer(direction: Vector2, deltaTime: Float) {
        if (playerState == EntityState.DEAD) return
        val normalizedDir = direction.normalized()
        if (normalizedDir.length() > 0.1f) {
            playerState = EntityState.CHASE
            playerPosition = Vector2(
                (playerPosition.x + normalizedDir.x * playerSpeed * deltaTime).coerceIn(20f, bounds.width - 20f),
                (playerPosition.y + normalizedDir.y * playerSpeed * deltaTime).coerceIn(20f, bounds.height - 20f)
            )
        } else {
            playerState = EntityState.IDLE
        }
    }

    fun attackNearestMonster(currentTime: Long): Boolean {
        if (currentTime - playerLastAttackTime < playerAttackCooldown) return false
        if (playerState == EntityState.DEAD) return false

        val nearest = monsters.filter { !it.isDead() }
            .minByOrNull { it.position.distanceTo(playerPosition) }

        if (nearest != null && nearest.position.distanceTo(playerPosition) <= playerAttackRange) {
            playerLastAttackTime = currentTime
            val damage = calculateDamage(playerAtk, nearest.type.baseDef)
            val isCrit = Random.nextFloat() < 0.15f
            val finalDamage = if (isCrit) damage * 2 else damage
            nearest.hp -= finalDamage

            damageNumbers.add(
                DamageNumber(
                    position = nearest.position.copy(),
                    value = finalDamage,
                    isCrit = isCrit,
                    startTime = currentTime,
                    color = if (isCrit) 0xFFFF0000 else 0xFFFFFFFF
                )
            )

            if (nearest.isDead()) {
                nearest.state = EntityState.DEAD
                nearest.respawnTimer = currentTime + 10000
                playerXp += nearest.type.xpReward
                playerGold += Random.nextInt(1, 10) * nearest.level
            }
            return true
        }
        return false
    }

    private fun calculateDamage(atk: Int, def: Int): Int {
        return (atk * 1.5f - def * 0.5f).toInt().coerceAtLeast(1)
    }

    private fun updateMonsters(deltaTime: Float, currentTime: Long) {
        monsters.forEach { monster ->
            if (monster.isDead()) {
                if (currentTime >= monster.respawnTimer) {
                    monster.hp = monster.maxHp
                    monster.state = EntityState.PATROL
                    monster.position = Vector2(
                        Random.nextFloat() * (bounds.width - 100) + 50,
                        Random.nextFloat() * (bounds.height - 100) + 50
                    )
                    monster.patrolTarget = monster.position + Vector2(
                        Random.nextFloat() * 200 - 100,
                        Random.nextFloat() * 200 - 100
                    )
                }
                return@forEach
            }

            val distToPlayer = monster.position.distanceTo(playerPosition)

            when (monster.state) {
                EntityState.PATROL -> {
                    if (distToPlayer < monster.type.aggressionRange) {
                        monster.state = EntityState.CHASE
                    } else {
                        moveToward(monster, monster.patrolTarget, monster.speed * 0.4f, deltaTime)
                        if (monster.position.distanceTo(monster.patrolTarget) < 20f) {
                            monster.patrolTarget = monster.position + Vector2(
                                Random.nextFloat() * monster.patrolRadius * 2 - monster.patrolRadius,
                                Random.nextFloat() * monster.patrolRadius * 2 - monster.patrolRadius
                            )
                        }
                    }
                }
                EntityState.CHASE -> {
                    if (distToPlayer > monster.type.aggressionRange * 2) {
                        monster.state = EntityState.PATROL
                    } else if (distToPlayer <= monster.attackRange) {
                        monster.state = EntityState.ATTACK
                        if (currentTime - monster.lastAttackTime >= monster.attackCooldown) {
                            monster.lastAttackTime = currentTime
                            val damage = calculateDamage(monster.type.baseAtk, playerDef).coerceAtLeast(1)
                            playerHp -= damage
                            damageNumbers.add(
                                DamageNumber(
                                    position = playerPosition.copy(),
                                    value = damage,
                                    isCrit = false,
                                    startTime = currentTime,
                                    color = 0xFFFF4444
                                )
                            )
                        }
                    } else {
                        moveToward(monster, playerPosition, monster.speed, deltaTime)
                    }
                }
                EntityState.ATTACK -> {
                    if (distToPlayer > monster.attackRange * 1.5f) {
                        monster.state = EntityState.CHASE
                    }
                }
                else -> {}
            }
        }
    }

    private fun moveToward(monster: Monster, target: Vector2, speed: Float, deltaTime: Float) {
        val direction = (target - monster.position).normalized()
        monster.position = Vector2(
            (monster.position.x + direction.x * speed * deltaTime).coerceIn(20f, bounds.width - 20f),
            (monster.position.y + direction.y * speed * deltaTime).coerceIn(20f, bounds.height - 20f)
        )
    }

    private fun updateDamageNumbers(currentTime: Long) {
        damageNumbers.removeAll { currentTime - it.startTime > 1000 }
    }

    private fun checkMonsterSpawns(currentTime: Long) {
        if (currentTime - lastMonsterSpawnTime > monsterSpawnInterval) {
            lastMonsterSpawnTime = currentTime
            val types = listOf(MonsterType.SLIME, MonsterType.WOLF, MonsterType.SKELETON, MonsterType.BEAR)
            spawnMonster(types.random())
        }
    }

    fun respawnPlayer() {
        playerHp = playerMaxHp
        playerPosition = Vector2(bounds.width / 2, bounds.height / 2)
        playerState = EntityState.IDLE
    }
}
