package com.loshii.dndzerinx.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object JsonSupport {
    val gson = Gson()

    fun toJson(value: Any): String = gson.toJson(value)

    inline fun <reified T> fromJson(json: String): T? {
        return try {
            gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    inline fun <reified T> fromAsset(context: Context, assetFileName: String): T? {
        return try {
            context.assets.open(assetFileName).use { stream ->
                InputStreamReader(stream).use { reader ->
                    gson.fromJson<T>(reader, object : TypeToken<T>() {}.type)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
