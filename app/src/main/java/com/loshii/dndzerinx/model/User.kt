package com.loshii.dndzerinx.model

import com.google.firebase.firestore.DocumentSnapshot

enum class EquipmentCategory {
    HEAD, CHEST, LEGS, FEET,
    COLLAR, EARRING_LEFT, EARRING_RIGHT, CAPE,
    WEAPON_LEFT, WEAPON_RIGHT,
    RING_SLOT, GLOVE_LEFT, GLOVE_RIGHT
}

enum class WeaponType {
    LIGHT, HEAVY
}

data class Equipment(
    val head: EquipmentItem? = null,
    val chest: EquipmentItem? = null,
    val legs: EquipmentItem? = null,
    val feet: EquipmentItem? = null,
    val collar: EquipmentItem? = null,
    val earringLeft: EquipmentItem? = null,
    val earringRight: EquipmentItem? = null,
    val cape: EquipmentItem? = null,
    val weaponLeft: EquipmentItem? = null,
    val weaponRight: EquipmentItem? = null,
    val ringSlot: List<EquipmentItem> = emptyList(),
    val gloveLeft: EquipmentItem? = null,
    val gloveRight: EquipmentItem? = null
) {
    val rings: List<String>
        get() = ringSlot.mapNotNull { it.name.takeIf { n -> n.isNotBlank() } }

    companion object {
        fun fromMap(data: Map<String, Any>?): Equipment {
            if (data == null) return Equipment()
            return Equipment(
                head = (data["head"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                chest = (data["chest"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                legs = (data["legs"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                feet = (data["feet"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                collar = (data["collar"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                earringLeft = (data["earringLeft"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                earringRight = (data["earringRight"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                cape = (data["cape"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                weaponLeft = (data["weaponLeft"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                weaponRight = (data["weaponRight"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                ringSlot = (data["ringSlot"] as? List<*>)?.mapNotNull {
                    (it as? Map<String, Any>)?.let { m -> EquipmentItem.fromMap(m) }
                } ?: emptyList(),
                gloveLeft = (data["gloveLeft"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) },
                gloveRight = (data["gloveRight"] as? Map<String, Any>)?.let { EquipmentItem.fromMap(it) }
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "head" to (head?.toMap() ?: mapOf<String, Any>()),
        "chest" to (chest?.toMap() ?: mapOf<String, Any>()),
        "legs" to (legs?.toMap() ?: mapOf<String, Any>()),
        "feet" to (feet?.toMap() ?: mapOf<String, Any>()),
        "collar" to (collar?.toMap() ?: mapOf<String, Any>()),
        "earringLeft" to (earringLeft?.toMap() ?: mapOf<String, Any>()),
        "earringRight" to (earringRight?.toMap() ?: mapOf<String, Any>()),
        "cape" to (cape?.toMap() ?: mapOf<String, Any>()),
        "weaponLeft" to (weaponLeft?.toMap() ?: mapOf<String, Any>()),
        "weaponRight" to (weaponRight?.toMap() ?: mapOf<String, Any>()),
        "ringSlot" to ringSlot.map { it.toMap() },
        "gloveLeft" to (gloveLeft?.toMap() ?: mapOf<String, Any>()),
        "gloveRight" to (gloveRight?.toMap() ?: mapOf<String, Any>())
    )
}

data class EquipmentItem(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val rarity: Int = 1,
    val category: EquipmentCategory = EquipmentCategory.HEAD,
    val stats: Map<String, Int> = emptyMap(),
    val isEquipped: Boolean = false
) {
    companion object {
        fun fromMap(data: Map<String, Any>): EquipmentItem {
            return EquipmentItem(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                icon = data["icon"] as? String,
                rarity = (data["rarity"] as? Long)?.toInt() ?: 1,
                category = try {
                    EquipmentCategory.valueOf(data["category"] as? String ?: "HEAD")
                } catch (e: Exception) {
                    EquipmentCategory.HEAD
                },
                stats = (data["stats"] as? Map<String, Any>)?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap(),
                isEquipped = data["isEquipped"] as? Boolean ?: false
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "icon" to (icon ?: ""),
        "rarity" to rarity,
        "category" to category.name,
        "stats" to stats,
        "isEquipped" to isEquipped
    )
}

data class CharacterStatus(
    val health: Int = 100,
    val maxHealth: Int = 100,
    val energy: Int = 100,
    val maxEnergy: Int = 100,
    val mana: Int = 50,
    val maxMana: Int = 50,
    val stamina: Int = 100,
    val maxStamina: Int = 100,
    val strength: Int = 10,
    val defense: Int = 10,
    val agility: Int = 10,
    val magic: Int = 10,
    val intelligence: Int = 10,
    val charisma: Int = 10,
    val speed: Int = 10,
    val magicResist: Int = 10,
    val physicalResist: Int = 10,
    val tenacity: Int = 10,
    val truth: Int = 10,
    val resonance: Int = 10,
    val spirit: Int = 10,
    val critChance: Int = 5,
    val amplifier: Int = 10,
    val skillPoints: Int = 0,
    val luck: Int = 10,
    val evasion: Int = 10,
    val accuracy: Int = 90,
    val attackPower: Int = 10,
    val magicPower: Int = 10,
    val armorPenetration: Int = 0,
    val magicPenetration: Int = 0,
    val blockChance: Int = 0,
    val criticalDamage: Int = 150,
    val attackSpeed: Float = 1.0f,
    val castSpeed: Float = 1.0f,
    val movementSpeed: Int = 100
) {
    companion object {
        fun fromMap(data: Map<String, Any>): CharacterStatus {
            return CharacterStatus(
                health = (data["health"] as? Long)?.toInt() ?: 100,
                maxHealth = (data["maxHealth"] as? Long)?.toInt() ?: 100,
                energy = (data["energy"] as? Long)?.toInt() ?: 100,
                maxEnergy = (data["maxEnergy"] as? Long)?.toInt() ?: 100,
                mana = (data["mana"] as? Long)?.toInt() ?: 50,
                maxMana = (data["maxMana"] as? Long)?.toInt() ?: 50,
                stamina = (data["stamina"] as? Long)?.toInt() ?: 100,
                maxStamina = (data["maxStamina"] as? Long)?.toInt() ?: 100,
                strength = (data["strength"] as? Long)?.toInt() ?: 10,
                defense = (data["defense"] as? Long)?.toInt() ?: 10,
                agility = (data["agility"] as? Long)?.toInt() ?: 10,
                magic = (data["magic"] as? Long)?.toInt() ?: 10,
                intelligence = (data["intelligence"] as? Long)?.toInt() ?: 10,
                charisma = (data["charisma"] as? Long)?.toInt() ?: 10,
                speed = (data["speed"] as? Long)?.toInt() ?: 10,
                magicResist = (data["magicResist"] as? Long)?.toInt() ?: 10,
                physicalResist = (data["physicalResist"] as? Long)?.toInt() ?: 10,
                tenacity = (data["tenacity"] as? Long)?.toInt() ?: 10,
                truth = (data["truth"] as? Long)?.toInt() ?: 10,
                resonance = (data["resonance"] as? Long)?.toInt() ?: 10,
                spirit = (data["spirit"] as? Long)?.toInt() ?: 10,
                critChance = (data["critChance"] as? Long)?.toInt() ?: 5,
                amplifier = (data["amplifier"] as? Long)?.toInt() ?: 10,
                skillPoints = (data["skillPoints"] as? Long)?.toInt() ?: 0,
                luck = (data["luck"] as? Long)?.toInt() ?: 10,
                evasion = (data["evasion"] as? Long)?.toInt() ?: 10,
                accuracy = (data["accuracy"] as? Long)?.toInt() ?: 90,
                attackPower = (data["attackPower"] as? Long)?.toInt() ?: 10,
                magicPower = (data["magicPower"] as? Long)?.toInt() ?: 10,
                armorPenetration = (data["armorPenetration"] as? Long)?.toInt() ?: 0,
                magicPenetration = (data["magicPenetration"] as? Long)?.toInt() ?: 0,
                blockChance = (data["blockChance"] as? Long)?.toInt() ?: 0,
                criticalDamage = (data["criticalDamage"] as? Long)?.toInt() ?: 150,
                attackSpeed = (data["attackSpeed"] as? Number)?.toFloat() ?: 1.0f,
                castSpeed = (data["castSpeed"] as? Number)?.toFloat() ?: 1.0f,
                movementSpeed = (data["movementSpeed"] as? Long)?.toInt() ?: 100
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "health" to health, "maxHealth" to maxHealth,
        "energy" to energy, "maxEnergy" to maxEnergy,
        "mana" to mana, "maxMana" to maxMana,
        "stamina" to stamina, "maxStamina" to maxStamina,
        "strength" to strength, "defense" to defense,
        "agility" to agility, "magic" to magic,
        "intelligence" to intelligence, "charisma" to charisma,
        "speed" to speed, "magicResist" to magicResist,
        "physicalResist" to physicalResist, "tenacity" to tenacity, "truth" to truth,
        "resonance" to resonance, "spirit" to spirit,
        "critChance" to critChance, "amplifier" to amplifier,
        "skillPoints" to skillPoints, "luck" to luck,
        "evasion" to evasion, "accuracy" to accuracy,
        "attackPower" to attackPower, "magicPower" to magicPower,
        "armorPenetration" to armorPenetration, "magicPenetration" to magicPenetration,
        "blockChance" to blockChance, "criticalDamage" to criticalDamage,
        "attackSpeed" to attackSpeed, "castSpeed" to castSpeed,
        "movementSpeed" to movementSpeed
    )
}

fun Equipment.totalStats(): Map<String, Int> {
    return listOfNotNull(
        head, chest, legs, feet, collar, earringLeft, earringRight, cape,
        weaponLeft, weaponRight, gloveLeft, gloveRight
    ).plus(ringSlot)
        .flatMap { it.stats.entries }
        .groupBy({ it.key }, { it.value })
        .mapValues { (_, values) -> values.sum() }
        .filterValues { it != 0 }
}

fun CharacterStatus.withAddedStats(bonuses: Map<String, Int>): CharacterStatus {
    val totalHealth = (health + (bonuses["health"] ?: bonuses["healAmount"] ?: 0)).coerceAtMost(maxHealth)
    val totalMana = (mana + (bonuses["mana"] ?: bonuses["manaRestore"] ?: 0)).coerceAtMost(maxMana)
    val totalEnergy = (energy + (bonuses["energy"] ?: bonuses["energyRestore"] ?: 0)).coerceAtMost(maxEnergy)
    val totalStamina = (stamina + (bonuses["stamina"] ?: bonuses["staminaRestore"] ?: 0)).coerceAtMost(maxStamina)

    return copy(
        health = totalHealth,
        mana = totalMana,
        energy = totalEnergy,
        stamina = totalStamina,
        strength = strength + (bonuses["strength"] ?: 0),
        defense = defense + (bonuses["defense"] ?: 0),
        agility = agility + (bonuses["agility"] ?: 0),
        magic = magic + (bonuses["magic"] ?: 0),
        intelligence = intelligence + (bonuses["intelligence"] ?: 0),
        charisma = charisma + (bonuses["charisma"] ?: 0),
        speed = speed + (bonuses["speed"] ?: 0),
        magicResist = magicResist + (bonuses["magicResist"] ?: 0),
        physicalResist = physicalResist + (bonuses["physicalResist"] ?: 0),
        tenacity = tenacity + (bonuses["tenacity"] ?: 0),
        truth = truth + (bonuses["truth"] ?: 0),
        resonance = resonance + (bonuses["resonance"] ?: 0),
        spirit = spirit + (bonuses["spirit"] ?: 0),
        critChance = critChance + (bonuses["critChance"] ?: 0),
        amplifier = amplifier + (bonuses["amplifier"] ?: 0),
        luck = luck + (bonuses["luck"] ?: 0),
        evasion = evasion + (bonuses["evasion"] ?: 0),
        accuracy = accuracy + (bonuses["accuracy"] ?: 0),
        attackPower = attackPower + (bonuses["attackPower"] ?: 0),
        magicPower = magicPower + (bonuses["magicPower"] ?: 0),
        armorPenetration = armorPenetration + (bonuses["armorPenetration"] ?: 0),
        magicPenetration = magicPenetration + (bonuses["magicPenetration"] ?: 0),
        blockChance = blockChance + (bonuses["blockChance"] ?: 0),
        movementSpeed = movementSpeed + (bonuses["movementSpeed"] ?: 0)
    )
}

fun User.effectiveCharacterStatus(): CharacterStatus = characterStatus.withAddedStats(equipment.totalStats())

data class Wallet(
    val gold: Int = 0,
    val silver: Int = 0,
    val copper: Int = 0,
    val gems: Int = 0
)

data class Skill(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String? = null,
    val type: SkillType = SkillType.ACTIVE,
    val level: Int = 1,
    val cooldown: Int = 0,
    val manaCost: Int = 0,
    val damage: Int = 0,
    val healAmount: Int = 0,
    val duration: Int = 0,
    val isUnlocked: Boolean = false
) {
    companion object {
        fun fromMap(data: Map<String, Any>): Skill {
            return Skill(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: "",
                icon = data["icon"] as? String,
                type = try { SkillType.valueOf(data["type"] as? String ?: "ACTIVE") } catch (e: Exception) { SkillType.ACTIVE },
                level = (data["level"] as? Long)?.toInt() ?: 1,
                cooldown = (data["cooldown"] as? Long)?.toInt() ?: 0,
                manaCost = (data["manaCost"] as? Long)?.toInt() ?: 0,
                damage = (data["damage"] as? Long)?.toInt() ?: 0,
                healAmount = (data["healAmount"] as? Long)?.toInt() ?: 0,
                duration = (data["duration"] as? Long)?.toInt() ?: 0,
                isUnlocked = data["isUnlocked"] as? Boolean ?: false
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id, "name" to name, "description" to description,
        "icon" to (icon ?: ""), "type" to type.name, "level" to level,
        "cooldown" to cooldown, "manaCost" to manaCost, "damage" to damage,
        "healAmount" to healAmount, "duration" to duration, "isUnlocked" to isUnlocked
    )
}

enum class SkillType {
    PASSIVE, ACTIVE, ULTIMATE
}

data class Skills(
    val passive: Skill? = null,
    val skill1: Skill? = null,
    val skill2: Skill? = null,
    val skill3: Skill? = null,
    val skill4: Skill? = null
) {
    companion object {
        fun fromMap(data: Map<String, Any>?): Skills {
            if (data == null) return Skills()
            return Skills(
                passive = (data["passive"] as? Map<String, Any>)?.let { Skill.fromMap(it) },
                skill1 = (data["skill1"] as? Map<String, Any>)?.let { Skill.fromMap(it) },
                skill2 = (data["skill2"] as? Map<String, Any>)?.let { Skill.fromMap(it) },
                skill3 = (data["skill3"] as? Map<String, Any>)?.let { Skill.fromMap(it) },
                skill4 = (data["skill4"] as? Map<String, Any>)?.let { Skill.fromMap(it) }
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "passive" to (passive?.toMap() ?: mapOf<String, Any>()),
        "skill1" to (skill1?.toMap() ?: mapOf<String, Any>()),
        "skill2" to (skill2?.toMap() ?: mapOf<String, Any>()),
        "skill3" to (skill3?.toMap() ?: mapOf<String, Any>()),
        "skill4" to (skill4?.toMap() ?: mapOf<String, Any>())
    )
}

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String? = null,
    val rarity: Int = 1,
    val quantity: Int = 1,
    val category: String = "",
    val stackable: Boolean = false,
    val sellPrice: Int = 0,
    val stats: Map<String, Int> = emptyMap()
) {
    companion object {
        fun fromMap(data: Map<String, Any>): InventoryItem {
            return InventoryItem(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: "",
                icon = data["icon"] as? String,
                rarity = (data["rarity"] as? Long)?.toInt() ?: 1,
                quantity = (data["quantity"] as? Long)?.toInt() ?: 1,
                category = data["category"] as? String ?: "",
                stackable = data["stackable"] as? Boolean ?: false,
                sellPrice = (data["sellPrice"] as? Long)?.toInt() ?: 0,
                stats = (data["stats"] as? Map<String, Any>)?.mapValues { (it.value as? Long)?.toInt() ?: 0 } ?: emptyMap()
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id, "name" to name, "description" to description,
        "icon" to (icon ?: ""), "rarity" to rarity, "quantity" to quantity,
        "category" to category, "stackable" to stackable, "sellPrice" to sellPrice,
        "stats" to stats
    )

    val isConsumable: Boolean
        get() = category.contains("consumable", ignoreCase = true) || category.contains("potion", ignoreCase = true)

    val isEquippable: Boolean
        get() = category.contains("weapon", ignoreCase = true)
                || category.contains("armor", ignoreCase = true)
                || category.contains("accessory", ignoreCase = true)
                || category.contains("ring", ignoreCase = true)
                || category.contains("glove", ignoreCase = true)
                || category.equals("head", ignoreCase = true)
                || category.equals("chest", ignoreCase = true)
                || category.equals("legs", ignoreCase = true)
                || category.equals("feet", ignoreCase = true)

    fun equipmentCategory(): EquipmentCategory? {
        return when {
            category.contains("weapon", ignoreCase = true) -> EquipmentCategory.WEAPON_LEFT
            category.contains("armor", ignoreCase = true) -> EquipmentCategory.CHEST
            category.contains("accessory", ignoreCase = true) -> EquipmentCategory.COLLAR
            category.contains("ring", ignoreCase = true) -> EquipmentCategory.RING_SLOT
            category.contains("glove", ignoreCase = true) -> EquipmentCategory.GLOVE_LEFT
            category.equals("head", ignoreCase = true) -> EquipmentCategory.HEAD
            category.equals("chest", ignoreCase = true) -> EquipmentCategory.CHEST
            category.equals("legs", ignoreCase = true) -> EquipmentCategory.LEGS
            category.equals("feet", ignoreCase = true) -> EquipmentCategory.FEET
            else -> null
        }
    }
}

data class Inventory(
    val items: List<InventoryItem> = emptyList(),
    val maxSlots: Int = 50
) {
    companion object {
        fun fromMap(data: Map<String, Any>?): Inventory {
            if (data == null) return Inventory()
            val itemsList = (data["items"] as? List<*>)?.mapNotNull {
                (it as? Map<String, Any>)?.let { m -> InventoryItem.fromMap(m) }
            } ?: emptyList()
            return Inventory(
                items = itemsList,
                maxSlots = (data["maxSlots"] as? Long)?.toInt() ?: 50
            )
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "items" to items.map { it.toMap() },
        "maxSlots" to maxSlots
    )
}

enum class UserRole(val displayName: String) {
    INITIAL("Inicial"),
    ADVENTURER("Aventurero"),
    GM("Game Master"),
    ADMIN("Administrador")
}

enum class GuildRole(val displayName: String) {
    LEADER("Líder"),
    OFFICER("Oficial"),
    MEMBER("Miembro")
}

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val level: Int = 1,
    val experience: Int = 0,
    val experienceToNextLevel: Int = LevelProgression.experienceForNextLevel(1),
    val role: UserRole = UserRole.INITIAL,
    val guildId: String? = null,
    val guildName: String? = null,
    val guildRole: GuildRole? = null,
    val guildLeaderRoleName: String? = null,
    val accessKey: String = "",
    val isActive: Boolean = true,
    val bio: String = "",
    val location: String = "",
    val website: String = "",
    val tags: List<String> = emptyList(),
    val race: String = "Humano",
    val titles: List<String> = emptyList(),
    val statusEffects: List<String> = emptyList(),
    val wallet: Wallet = Wallet(),
    val characterStatus: CharacterStatus = CharacterStatus(),
    val equipment: Equipment = Equipment(),
    val skills: Skills = Skills(),
    val inventory: Inventory = Inventory(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): User? {
            if (!doc.exists()) return null
            val data = doc.data ?: return null

            val roleStr = data["role"] as? String
            val role = when (roleStr) {
                "INITIAL" -> UserRole.INITIAL
                "GM" -> UserRole.GM
                "ADMIN" -> UserRole.ADMIN
                else -> UserRole.ADVENTURER
            }

            val guildRoleStr = data["guildRole"] as? String
            val guildRole = when (guildRoleStr) {
                "LEADER" -> GuildRole.LEADER
                "OFFICER" -> GuildRole.OFFICER
                "MEMBER" -> GuildRole.MEMBER
                else -> null
            }

            val walletData = data["wallet"] as? Map<String, Any>
            val wallet = if (walletData != null) {
                Wallet(
                    gold = (walletData["gold"] as? Long)?.toInt() ?: 0,
                    silver = (walletData["silver"] as? Long)?.toInt() ?: 0,
                    copper = (walletData["copper"] as? Long)?.toInt() ?: 0,
                    gems = (walletData["gems"] as? Long)?.toInt() ?: 0
                )
            } else Wallet()

            val tagsList = data["tags"] as? List<*>
            val tags = tagsList?.filterIsInstance<String>() ?: emptyList()
            val titlesList = data["titles"] as? List<*>
            val titles = titlesList?.filterIsInstance<String>() ?: emptyList()
            val statusEffectsList = data["statusEffects"] as? List<*>
            val statusEffects = statusEffectsList?.filterIsInstance<String>() ?: emptyList()

            val statsData = data["characterStatus"] as? Map<String, Any>
            val characterStatus = if (statsData != null) CharacterStatus.fromMap(statsData) else CharacterStatus()

            val equipmentData = data["equipment"] as? Map<String, Any>
            val equipment = if (equipmentData != null) Equipment.fromMap(equipmentData) else Equipment()

            val skillsData = data["skills"] as? Map<String, Any>
            val skills = if (skillsData != null) Skills.fromMap(skillsData) else Skills()

            val inventoryData = data["inventory"] as? Map<String, Any>
            val inventory = if (inventoryData != null) Inventory.fromMap(inventoryData) else Inventory()

            return User(
                id = doc.id,
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                avatarUrl = data["avatarUrl"] as? String,
                bannerUrl = data["bannerUrl"] as? String,
                level = (data["level"] as? Long)?.toInt() ?: 1,
                experience = (data["experience"] as? Long)?.toInt() ?: 0,
                experienceToNextLevel = (data["experienceToNextLevel"] as? Long)?.toInt()
                    ?: LevelProgression.experienceForNextLevel((data["level"] as? Long)?.toInt() ?: 1),
                role = role,
                guildId = data["guildId"] as? String,
                guildName = data["guildName"] as? String,
                guildRole = guildRole,
                guildLeaderRoleName = data["guildLeaderRoleName"] as? String,
                accessKey = data["accessKey"] as? String ?: "VHKM0",
                isActive = data["isActive"] as? Boolean ?: true,
                bio = data["bio"] as? String ?: "",
                location = data["location"] as? String ?: "",
                website = data["website"] as? String ?: "",
                tags = tags,
                race = data["race"] as? String ?: "Humano",
                titles = titles,
                statusEffects = statusEffects,
                wallet = wallet,
                characterStatus = characterStatus,
                equipment = equipment,
                skills = skills,
                inventory = inventory,
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        }
    }
}
