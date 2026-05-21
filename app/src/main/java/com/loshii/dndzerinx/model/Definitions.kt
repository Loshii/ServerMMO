package com.loshii.dndzerinx.model

data class SpeciesDefinition(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)

data class ClassDefinition(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)

data class ItemDefinition(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String? = null,
    val rarity: Int = 1,
    val category: String = "",
    val stats: Map<String, Int> = emptyMap()
)

data class AbilityDefinition(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)

data class RuneDefinition(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)

data class MonsterDefinition(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)

data class MonsterCoreDefinition(
    val id: String = "",
    val name: String = "",
    val icon: String? = null,
    val stats: Map<String, Int> = emptyMap()
)
