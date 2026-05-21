package com.loshii.dndzerinx.engine

object GodotIntegration {
    const val ACTION_LAUNCH_GODOT = "com.loshii.dndzerinx.action.LAUNCH_GODOT"
    const val EXTRA_PLAYER_ID = "com.loshii.dndzerinx.extra.PLAYER_ID"
    const val EXTRA_PLAYER_NAME = "com.loshii.dndzerinx.extra.PLAYER_NAME"
    const val EXTRA_PLAYER_LEVEL = "com.loshii.dndzerinx.extra.PLAYER_LEVEL"
    const val EXTRA_PLAYER_MAX_HP = "com.loshii.dndzerinx.extra.PLAYER_MAX_HP"
    const val EXTRA_ACCESS_KEY = "com.loshii.dndzerinx.extra.ACCESS_KEY"
    const val EXTRA_SERVER_URL = "com.loshii.dndzerinx.extra.SERVER_URL"

    fun createGodotExtras(
        playerId: String,
        playerName: String,
        level: Int,
        maxHp: Int,
        accessKey: String,
        serverUrl: String
    ): Map<String, Any> {
        return mapOf(
            EXTRA_PLAYER_ID to playerId,
            EXTRA_PLAYER_NAME to playerName,
            EXTRA_PLAYER_LEVEL to level,
            EXTRA_PLAYER_MAX_HP to maxHp,
            EXTRA_ACCESS_KEY to accessKey,
            EXTRA_SERVER_URL to serverUrl
        )
    }
}
