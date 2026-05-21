package com.loshii.dndzerinx.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.localProfileStore: DataStore<Preferences> by preferencesDataStore(name = "local_profile")

data class LocalProfile(
    val displayName: String = "Jugador",
    val level: Int = 1,
    val xp: Int = 0,
    val gold: Int = 0,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val attackPower: Int = 10,
    val defense: Int = 5,
    val avatarUrl: String = "",
    val characterClass: String = "Guerrero",
    val race: String = "Humano"
)

class LocalProfileManager(private val context: Context) {
    companion object {
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_LEVEL = intPreferencesKey("level")
        private val KEY_XP = intPreferencesKey("xp")
        private val KEY_GOLD = intPreferencesKey("gold")
        private val KEY_HP = intPreferencesKey("hp")
        private val KEY_MAX_HP = intPreferencesKey("max_hp")
        private val KEY_ATK = intPreferencesKey("attack_power")
        private val KEY_DEF = intPreferencesKey("defense")
        private val KEY_AVATAR_URL = stringPreferencesKey("avatar_url")
        private val KEY_CLASS = stringPreferencesKey("character_class")
        private val KEY_RACE = stringPreferencesKey("race")
    }

    val profile: Flow<LocalProfile> = context.localProfileStore.data.map { prefs ->
        LocalProfile(
            displayName = prefs[KEY_DISPLAY_NAME] ?: "Jugador",
            level = prefs[KEY_LEVEL] ?: 1,
            xp = prefs[KEY_XP] ?: 0,
            gold = prefs[KEY_GOLD] ?: 0,
            hp = prefs[KEY_HP] ?: 100,
            maxHp = prefs[KEY_MAX_HP] ?: 100,
            attackPower = prefs[KEY_ATK] ?: 10,
            defense = prefs[KEY_DEF] ?: 5,
            avatarUrl = prefs[KEY_AVATAR_URL] ?: "",
            characterClass = prefs[KEY_CLASS] ?: "Guerrero",
            race = prefs[KEY_RACE] ?: "Humano"
        )
    }

    suspend fun updateName(name: String) {
        context.localProfileStore.edit { it[KEY_DISPLAY_NAME] = name }
    }

    suspend fun updateLevel(level: Int) {
        context.localProfileStore.edit { it[KEY_LEVEL] = level }
    }

    suspend fun updateXp(xp: Int) {
        context.localProfileStore.edit { it[KEY_XP] = xp }
    }

    suspend fun updateGold(gold: Int) {
        context.localProfileStore.edit { it[KEY_GOLD] = gold }
    }

    suspend fun updateHp(hp: Int) {
        context.localProfileStore.edit { it[KEY_HP] = hp }
    }

    suspend fun updateMaxHp(maxHp: Int) {
        context.localProfileStore.edit { it[KEY_MAX_HP] = maxHp }
    }

    suspend fun updateAttackPower(atk: Int) {
        context.localProfileStore.edit { it[KEY_ATK] = atk }
    }

    suspend fun updateDefense(def: Int) {
        context.localProfileStore.edit { it[KEY_DEF] = def }
    }

    suspend fun updateAvatarUrl(url: String) {
        context.localProfileStore.edit { it[KEY_AVATAR_URL] = url }
    }

    suspend fun updateCharacterClass(cls: String) {
        context.localProfileStore.edit { it[KEY_CLASS] = cls }
    }

    suspend fun updateRace(race: String) {
        context.localProfileStore.edit { it[KEY_RACE] = race }
    }

    suspend fun addXp(amount: Int) {
        context.localProfileStore.edit { prefs ->
            val currentXp = prefs[KEY_XP] ?: 0
            val currentLevel = prefs[KEY_LEVEL] ?: 1
            val newXp = currentXp + amount
            val xpToLevel = currentLevel * 100
            if (newXp >= xpToLevel) {
                prefs[KEY_LEVEL] = currentLevel + 1
                prefs[KEY_XP] = newXp - xpToLevel
                prefs[KEY_MAX_HP] = (prefs[KEY_MAX_HP] ?: 100) + 10
                prefs[KEY_HP] = prefs[KEY_MAX_HP] ?: 110
                prefs[KEY_ATK] = (prefs[KEY_ATK] ?: 10) + 2
                prefs[KEY_DEF] = (prefs[KEY_DEF] ?: 5) + 1
            } else {
                prefs[KEY_XP] = newXp
            }
        }
    }

    suspend fun addGold(amount: Int) {
        context.localProfileStore.edit { prefs ->
            prefs[KEY_GOLD] = (prefs[KEY_GOLD] ?: 0) + amount
        }
    }
}
