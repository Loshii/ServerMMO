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
    ABERRATION("Aberración", 0xFF9C27B0, 60, 12, 8, 30, 200f),
    ANKHEG("Ankheg", 0xFF795548, 80, 18, 12, 50, 220f),
    BASILISK("Basilisco", 0xFF607D8B, 90, 15, 14, 45, 180f),
    BEAR("Oso", 0xFF795548, 100, 15, 10, 50, 180f),
    BEHOLDER("Contemplador", 0xFFE91E63, 250, 30, 20, 150, 350f),
    BUGBEAR("Bugbear", 0xFF8D6E63, 45, 10, 6, 20, 170f),
    CENTAUR("Centauro", 0xFF4CAF50, 70, 14, 8, 35, 250f),
    CHIMERA("Quimera", 0xFFFF9800, 130, 22, 15, 80, 280f),
    COCKATRICE("Cocatriz", 0xFF78909C, 50, 10, 8, 25, 160f),
    COUATL("Couatl", 0xFF00BCD4, 120, 18, 16, 70, 300f),
    DARKWEIR("Espectro Oscuro", 0xFF4A148C, 55, 14, 6, 30, 200f),
    DEATH_KNIGHT("Caballero de la Muerte", 0xFF212121, 200, 28, 22, 120, 320f),
    DEMON_GLABREZU("Glabrezu", 0xFF1A237E, 180, 25, 18, 100, 300f),
    DEMON_HEZROU("Hezrou", 0xFF311B92, 150, 22, 16, 85, 280f),
    DEVIL_BONE("Diablo de Huesos", 0xFFBDBDBD, 100, 18, 14, 60, 250f),
    DEVIL_HORNED("Diablo Cornudo", 0xFFD32F2F, 160, 24, 18, 90, 290f),
    DEVIL_IMP("Diablillo", 0xFF651FFF, 30, 8, 4, 15, 140f),
    DISPLACER("Bestia Displacer", 0xFF303F9F, 85, 16, 12, 45, 230f),
    DRAGON_BLACK("Dragón Negro", 0xFF1B5E20, 220, 28, 18, 120, 320f),
    DRAGON_BLUE("Dragón Azul", 0xFF0D47A1, 240, 30, 20, 130, 340f),
    DRAGON_BRASS("Dragón de Latón", 0xFFFFB300, 200, 26, 16, 110, 300f),
    DRAGON_BRONZE("Dragón de Bronce", 0xFF8D6E63, 210, 27, 17, 115, 310f),
    DRAGON_COPPER("Dragón de Cobre", 0xFFA1887F, 190, 25, 15, 105, 290f),
    DRAGON_GOLD("Dragón Dorado", 0xFFFFF176, 260, 32, 22, 140, 360f),
    DRAGON_GREEN("Dragón Verde", 0xFF2E7D32, 230, 29, 19, 125, 330f),
    DRAGON_RED("Dragón Rojo", 0xFFD32F2F, 280, 35, 24, 150, 380f),
    DRAGON_SILVER("Dragón Plateado", 0xFFB0BEC5, 250, 31, 21, 135, 350f),
    DRAGON_WHITE("Dragón Blanco", 0xFFECEFF1, 200, 26, 16, 110, 300f),
    DRAGON("Dragón", 0xFFFF5722, 200, 25, 15, 100, 300f),
    DRYAD("Dríada", 0xFF66BB6A, 60, 10, 8, 30, 180f),
    DUODAR("Duodár", 0xFF7E57C2, 40, 8, 5, 18, 150f),
    ETTIN("Ettin", 0xFF5D4037, 120, 20, 14, 70, 260f),
    ETTERCAP("Ettercap", 0xFF4E342E, 55, 12, 8, 28, 190f),
    GARGOYLE("Gárgola", 0xFF757575, 75, 14, 12, 40, 210f),
    GELATINOUS_CUBE("Cubo Gelatinoso", 0xFF00E676, 90, 12, 10, 45, 150f),
    GHAST("Ghast", 0xFF827717, 60, 14, 8, 32, 200f),
    GHOST("Fantasma", 0xFFCFD8DC, 70, 16, 10, 40, 240f),
    GIANT_FIRE("Gigante de Fuego", 0xFFFF6F00, 180, 26, 18, 100, 300f),
    GIANT_FROST("Gigante de Escarcha", 0xFF0277BD, 170, 24, 16, 95, 290f),
    GIANT_HILL("Gigante de la Colina", 0xFF8D6E63, 140, 20, 14, 80, 270f),
    GIANT_STONE("Gigante de Piedra", 0xFF616161, 160, 22, 16, 90, 280f),
    GIANT_STORM("Gigante de la Tormenta", 0xFF42A5F5, 200, 28, 20, 110, 320f),
    GIBBERING_MOUTHER("Boca Chillante", 0xFF6D4C41, 80, 14, 6, 40, 180f),
    GITHYANKI("Githyanki", 0xFF9C27B0, 65, 14, 10, 35, 220f),
    GITHZERAI("Githzerai", 0xFF7B1FA2, 60, 12, 10, 32, 210f),
    GNOLL("Gnoll", 0xFF5D4037, 40, 10, 5, 20, 160f),
    GOLEM_CLAY("Golem de Arcilla", 0xFFBF360C, 130, 20, 16, 75, 200f),
    GOLEM_FLESH("Golem de Carne", 0xFF8D6E63, 110, 18, 14, 65, 220f),
    GOLEM_IRON("Golem de Hierro", 0xFF455A64, 180, 24, 20, 100, 250f),
    GOLEM_STONE("Golem de Piedra", 0xFF607D8B, 150, 22, 18, 85, 230f),
    GORGON("Gorgona", 0xFF9E9E9E, 100, 18, 14, 55, 240f),
    GREEN_HAG("Bruja Verde", 0xFF388E3C, 80, 16, 10, 45, 220f),
    GRICK("Grick", 0xFF455A64, 45, 10, 8, 22, 170f),
    GRIFFON("Grifo", 0xFFFFA000, 90, 18, 12, 50, 260f),
    GUARDIAN_NATURE("Guardián Natural", 0xFF4CAF50, 70, 12, 10, 35, 190f),
    GYRENAUT("Gyrenauta", 0xFF0288D1, 55, 12, 8, 28, 180f),
    HARPY("Arpía", 0xFFBDBDBD, 50, 12, 6, 25, 200f),
    HELLDON("Helldon", 0xFFB71C1C, 100, 18, 14, 55, 240f),
    HIPPOGRIFF("Hipogrifo", 0xFF8D6E63, 65, 14, 10, 35, 220f),
    HOBOGOBLIN("Hobgoblin", 0xFF616161, 45, 10, 6, 22, 170f),
    HYDRA("Hidra", 0xFF1B5E20, 160, 22, 16, 90, 280f),
    INVISIBLE_STALKER("Acechador Invisible", 0xFFB3E5FC, 100, 16, 12, 60, 250f),
    KOBOLD("Kobold", 0xFFD84315, 25, 6, 3, 12, 130f),
    LICH("Liche", 0xFF4A148C, 200, 28, 20, 120, 340f),
    LIZARDMAN("Hombre Lagarto", 0xFF33691E, 50, 12, 8, 25, 180f),
    MANTICORE("Mantícora", 0xFF6D4C41, 100, 18, 14, 55, 260f),
    MEDUSA("Medusa", 0xFF66BB6A, 110, 20, 14, 65, 280f),
    MIMIC("Mímico", 0xFF795548, 70, 14, 12, 40, 150f),
    MINOTAUR("Minotauro", 0xFF5D4037, 120, 20, 14, 70, 260f),
    MUMMY("Momia", 0xFF8D6E63, 90, 16, 12, 50, 220f),
    NIGHTMARE("Pesadilla", 0xFF1A237E, 80, 16, 10, 45, 240f),
    OCHRE_JELLY("Gelatina Ocre", 0xFFF57F17, 75, 12, 10, 38, 160f),
    OGRE("Ogro", 0xFF5D4037, 80, 16, 10, 45, 200f),
    ONI("Oni", 0xFF4A148C, 110, 20, 14, 65, 260f),
    Ooze("Cieno", 0xFF00C853, 60, 10, 8, 30, 140f),
    OWLBEAR("Oso Búho", 0xFF6D4C41, 90, 18, 12, 50, 240f),
    PEGASUS("Pegaso", 0xFFECEFF1, 70, 14, 10, 38, 260f),
    PHASE_SPIDER("Araña de Fase", 0xFF311B92, 55, 14, 8, 30, 200f),
    PURPLE_WORM("Gusano Púrpura", 0xFF6A1B9A, 200, 26, 20, 110, 280f),
    RAKSHASA("Rakshasa", 0xFFE91E63, 130, 22, 16, 80, 300f),
    REMORHAZ("Remorhaz", 0xFFFF6F00, 150, 24, 18, 85, 270f),
    ROCS("Roc", 0xFF795548, 180, 24, 18, 100, 300f),
    ROPER("Roper", 0xFF5D4037, 100, 18, 14, 55, 200f),
    RUST_MONSTER("Monstruo Oxidado", 0xFFBF360C, 60, 12, 10, 32, 180f),
    SAHUAGIN("Sahuagin", 0xFF00695C, 45, 10, 6, 22, 170f),
    SALAMANDER("Salamandra", 0xFFFF6F00, 120, 20, 14, 70, 260f),
    SATYR("Sátiro", 0xFF4CAF50, 50, 10, 6, 25, 180f),
    SCORPION("Escorpión Gigante", 0xFF4E342E, 40, 10, 8, 20, 160f),
    SHADOW("Sombra", 0xFF212121, 45, 12, 6, 22, 190f),
    SHIELD_GUARDIAN("Guardián Escudo", 0xFF546E7A, 140, 20, 18, 80, 250f),
    SKELETON("Esqueleto", 0xFFE0E0E0, 70, 12, 8, 35, 250f),
    SLIME("Slime", 0xFF4CAF50, 30, 5, 2, 10, 150f),
    SPECTATOR("Espectador", 0xFF7B1FA2, 80, 14, 10, 42, 220f),
    SPECTER("Espectro", 0xFF9E9E9E, 50, 12, 6, 25, 210f),
    SPIDER("Araña Gigante", 0xFF424242, 35, 8, 4, 18, 150f),
    SPRITE("Duendecillo", 0xFF00E5FF, 20, 6, 3, 10, 120f),
    STIRGE("Stirge", 0xFF455A64, 30, 8, 4, 15, 140f),
    TREANT("Treant", 0xFF2E7D32, 160, 22, 18, 90, 220f),
    TRIPLE("Triple", 0xFF7E57C2, 90, 16, 12, 48, 240f),
    TROLL("Troll", 0xFF388E3C, 110, 18, 12, 65, 250f),
    UNICORN("Unicornio", 0xFFECEFF1, 100, 16, 14, 60, 280f),
    VAMPIRE("Vampiro", 0xFFB71C1C, 180, 26, 18, 100, 320f),
    WEREWOLF("Hombre Lobo", 0xFF616161, 90, 18, 12, 55, 260f),
    WIGHT("Espectro de Hielo", 0xFF546E7A, 70, 14, 10, 40, 230f),
    WILL_O_WISP("Will-o'-Wisp", 0xFFFFEB3B, 35, 10, 4, 18, 180f),
    WOLF("Lobo", 0xFF9E9E9E, 50, 10, 5, 25, 200f),
    WRAITH("Ánima", 0xFF1A237E, 100, 18, 14, 60, 270f),
    WYVERN("Wyvern", 0xFF4CAF50, 130, 22, 16, 75, 290f),
    XORN("Xorn", 0xFF795548, 80, 16, 12, 45, 200f),
    YETI("Yeti", 0xFFECEFF1, 90, 16, 12, 50, 240f),
    YUAN_TI("Yuan-ti", 0xFF33691E, 75, 14, 10, 40, 220f),
    ZOMBIE("Zombi", 0xFF424242, 55, 8, 6, 25, 140f)
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
