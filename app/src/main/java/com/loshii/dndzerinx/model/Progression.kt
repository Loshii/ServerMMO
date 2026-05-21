package com.loshii.dndzerinx.model

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

enum class ExperienceActivityType(val displayName: String, val baseExperience: Int) {
    HUNT("Hunt", 120),
    MONSTER_EVENT("Evento de monstruos", 220),
    RAID("Raid", 520),
    DUNGEON("Dungeon", 380),
    MISSION("Mision", 180)
}

enum class EncounterDifficulty(val displayName: String, val multiplier: Float) {
    EASY("Facil", 0.75f),
    NORMAL("Normal", 1.0f),
    HARD("Dificil", 1.35f),
    ELITE("Elite", 1.75f),
    BOSS("Boss", 2.35f),
    NIGHTMARE("Pesadilla", 3.1f)
}

data class ExperienceReward(
    val baseExperience: Int,
    val difficultyBonus: Int,
    val firstKillBonus: Int,
    val teamContributionBonus: Int,
    val eventBonus: Int,
    val total: Int
)

object LevelProgression {
    const val MAX_LEVEL = 100
    const val SKILL_POINTS_PER_LEVEL = 3
    const val MILESTONE_SKILL_BONUS = 2

    fun experienceForNextLevel(level: Int): Int {
        val safeLevel = level.coerceAtLeast(1)
        return (420 + 95 * safeLevel + 36.0 * safeLevel.toDouble().pow(1.55)).roundToInt()
    }

    fun calculateReward(
        activityType: ExperienceActivityType,
        difficulty: EncounterDifficulty,
        participantLevel: Int,
        monsterLevel: Int,
        contributionPercent: Int,
        isFirstKill: Boolean,
        teamSize: Int,
        eventMultiplier: Float = 1.0f
    ): ExperienceReward {
        val levelDelta = monsterLevel - participantLevel
        val levelMultiplier = (1f + levelDelta * 0.05f).coerceIn(0.45f, 1.65f)
        val contributionMultiplier = (contributionPercent.coerceIn(10, 100) / 100f)
        val teamMultiplier = when {
            teamSize <= 1 -> 1f
            teamSize <= 3 -> 1.08f
            teamSize <= 5 -> 1.16f
            else -> 1.22f
        }

        val scaledBase = (activityType.baseExperience * levelMultiplier).roundToInt()
        val difficultyBonus = (scaledBase * (difficulty.multiplier - 1f)).roundToInt().coerceAtLeast(0)
        val firstKillBonus = if (isFirstKill) (scaledBase * 0.25f).roundToInt() else 0
        val teamContributionBonus = (scaledBase * (teamMultiplier - 1f) * contributionMultiplier).roundToInt()
        val eventBonus = (scaledBase * (eventMultiplier.coerceAtLeast(1f) - 1f)).roundToInt()
        val total = max(
            1,
            ((scaledBase + difficultyBonus + firstKillBonus + teamContributionBonus + eventBonus) * contributionMultiplier).roundToInt()
        )

        return ExperienceReward(
            baseExperience = scaledBase,
            difficultyBonus = difficultyBonus,
            firstKillBonus = firstKillBonus,
            teamContributionBonus = teamContributionBonus,
            eventBonus = eventBonus,
            total = total
        )
    }

    fun skillPointsForLevel(level: Int): Int {
        return SKILL_POINTS_PER_LEVEL + if (level % 5 == 0) MILESTONE_SKILL_BONUS else 0
    }
}
