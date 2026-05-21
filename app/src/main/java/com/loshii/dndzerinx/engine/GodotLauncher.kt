package com.loshii.dndzerinx.engine

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

object GodotLauncher {
    private const val GODOT_PACKAGE_NAME = "com.loshii.dndzerinx.godot"
    private const val GODOT_ACTIVITY_NAME = "com.loshii.dndzerinx.godot.GodotActivity"

    private fun buildGodotIntent(): Intent {
        return Intent(GodotIntegration.ACTION_LAUNCH_GODOT).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun buildGodotPackageIntent(): Intent {
        return Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            component = ComponentName(GODOT_PACKAGE_NAME, GODOT_ACTIVITY_NAME)
        }
    }

    fun canLaunchGodot(context: Context): Boolean {
        val actionIntent = buildGodotIntent()
        if (actionIntent.resolveActivity(context.packageManager) != null) {
            return true
        }
        return buildGodotPackageIntent().resolveActivity(context.packageManager) != null
    }

    fun launchGodot(
        context: Context,
        playerId: String,
        playerName: String,
        level: Int,
        maxHp: Int,
        accessKey: String,
        serverUrl: String
    ): Boolean {
        val extras = GodotIntegration.createGodotExtras(playerId, playerName, level, maxHp, accessKey, serverUrl)
        val actionIntent = buildGodotIntent().apply {
            val bundle = Bundle()
            extras.forEach { (key, value) ->
                when (value) {
                    is Int -> bundle.putInt(key, value)
                    is String -> bundle.putString(key, value)
                    else -> throw IllegalArgumentException("Unsupported extra type: ${value::class}")
                }
            }
            putExtras(bundle)
        }

        return when {
            actionIntent.resolveActivity(context.packageManager) != null -> {
                try {
                    context.startActivity(actionIntent)
                    true
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al lanzar Godot: ${e.message}", Toast.LENGTH_LONG).show()
                    false
                }
            }
            buildGodotPackageIntent().resolveActivity(context.packageManager) != null -> {
                try {
                    context.startActivity(buildGodotPackageIntent().apply {
                        putExtras(actionIntent.extras ?: Bundle())
                    })
                    true
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al lanzar Godot: ${e.message}", Toast.LENGTH_LONG).show()
                    false
                }
            }
            else -> {
                Toast.makeText(
                    context,
                    "No se encontró Godot instalado. Exporta el juego Godot como APK con paquete '$GODOT_PACKAGE_NAME' o integra un módulo AAR.",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }
}
