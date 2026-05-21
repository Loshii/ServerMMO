package com.loshii.dndzerinx.model.game

import kotlin.math.sqrt

data class Vector2(
    val x: Float = 0f,
    val y: Float = 0f
) {
    fun length(): Float = sqrt(x * x + y * y)
    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scale: Float) = Vector2(x * scale, y * scale)
    fun distanceTo(other: Vector2): Float = (this - other).length()
}

enum class MonsterType(val displayName: String, val color: Long, val baseHp: Int, val baseAtk: Int, val baseDef: Int, val xpReward: Int, val aggressionRange: Float) {
    SLIME("Slime", 0xFF4CAF50, 30, 5, 2, 10, 150f),
    WOLF("Lobo", 0xFF9E9E9E, 50, 10, 5, 25, 200f),
    BEAR("Oso", 0xFF795548, 100, 15, 10, 50, 180f),
    SKELETON("Esqueleto", 0xFFE0E0E0, 70, 12, 8, 35, 250f),
    DRAGON("Dragón", 0xFFFF5722, 200, 25, 15, 100, 300f)
}

enum class EntityState {
    IDLE, PATROL, CHASE, ATTACK, DEAD
}

data class Monster(
    val id: String,
    val type: MonsterType,
    var position: Vector2,
    var patrolTarget: Vector2,
    var hp: Int = type.baseHp,
    val maxHp: Int = type.baseHp,
    var state: EntityState = EntityState.PATROL,
    var lastAttackTime: Long = 0,
    var respawnTimer: Long = 0,
    val level: Int = 1
) {
    val attackCooldown: Long = 1500
    val attackRange: Float = 40f
    val speed: Float = 60f
    val patrolRadius: Float = 200f

    fun isDead(): Boolean = hp <= 0
}

data class DamageNumber(
    val position: Vector2,
    val value: Int,
    val isCrit: Boolean,
    val startTime: Long,
    val color: Long
)

data class WorldBounds(
    val width: Float,
    val height: Float
)
