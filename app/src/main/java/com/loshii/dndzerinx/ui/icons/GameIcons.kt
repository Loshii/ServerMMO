package com.loshii.dndzerinx.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object GameIcons {
    val Inventory: ImageVector = Builder(
        name = "Inventory",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 5f)
            lineTo(20f, 5f)
            lineTo(20f, 19f)
            lineTo(4f, 19f)
            close()
            moveTo(8f, 9f)
            lineTo(16f, 9f)
            lineTo(16f, 11f)
            lineTo(8f, 11f)
            close()
            moveTo(8f, 13f)
            lineTo(16f, 13f)
            lineTo(16f, 15f)
            lineTo(8f, 15f)
            close()
        }
    }.build()

    val Settings: ImageVector = Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 7f)
            lineTo(12f, 4f)
            lineTo(14.5f, 4.5f)
            lineTo(15.5f, 3f)
            lineTo(18f, 4.5f)
            lineTo(17.5f, 7f)
            lineTo(20f, 8f)
            lineTo(20f, 11f)
            lineTo(17.5f, 11.5f)
            lineTo(18f, 14f)
            lineTo(15.5f, 15.5f)
            lineTo(14.5f, 14f)
            lineTo(12f, 14.5f)
            lineTo(12f, 18f)
            lineTo(10f, 18f)
            lineTo(9.5f, 15.5f)
            lineTo(7f, 14f)
            lineTo(7.5f, 11.5f)
            lineTo(5f, 11f)
            lineTo(5f, 8f)
            lineTo(7.5f, 7f)
            lineTo(7f, 4.5f)
            lineTo(9.5f, 3f)
            lineTo(10.5f, 4.5f)
            lineTo(12f, 4f)
            close()
            moveTo(12f, 9.5f)
            lineTo(12f, 14.5f)
            close()
        }
    }.build()

    val Logout: ImageVector = Builder(
        name = "Logout",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(16f, 5f)
            lineTo(16f, 4f)
            lineTo(6f, 4f)
            lineTo(6f, 20f)
            lineTo(16f, 20f)
            lineTo(16f, 19f)
            lineTo(8f, 19f)
            lineTo(8f, 5f)
            close()
            moveTo(18f, 7f)
            lineTo(22f, 12f)
            lineTo(18f, 17f)
            lineTo(18f, 14f)
            lineTo(12f, 14f)
            lineTo(12f, 10f)
            lineTo(18f, 10f)
            close()
        }
    }.build()

    val Attack: ImageVector = Builder(
        name = "Attack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 2f)
            lineTo(10f, 6f)
            lineTo(8f, 8f)
            lineTo(14f, 14f)
            lineTo(16f, 12f)
            lineTo(20f, 16f)
            lineTo(18f, 18f)
            lineTo(14f, 14f)
            lineTo(12f, 16f)
            lineTo(8f, 12f)
            lineTo(10f, 10f)
            lineTo(6f, 6f)
            close()
        }
    }.build()

    val Godot: ImageVector = Builder(
        name = "Godot",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 6f)
            lineTo(18f, 12f)
            lineTo(6f, 18f)
            close()
            moveTo(8f, 8f)
            lineTo(10f, 12f)
            lineTo(8f, 16f)
            close()
        }
    }.build()

    val ArrowUp: ImageVector = Builder(
        name = "ArrowUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 20f)
            lineTo(16f, 20f)
            lineTo(16f, 12f)
            lineTo(20f, 12f)
            close()
        }
    }.build()

    val ArrowDown: ImageVector = Builder(
        name = "ArrowDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 20f)
            lineTo(4f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 4f)
            lineTo(16f, 4f)
            lineTo(16f, 12f)
            lineTo(20f, 12f)
            close()
        }
    }.build()

    val ArrowLeft: ImageVector = Builder(
        name = "ArrowLeft",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 12f)
            lineTo(12f, 4f)
            lineTo(12f, 8f)
            lineTo(20f, 8f)
            lineTo(20f, 16f)
            lineTo(12f, 16f)
            lineTo(12f, 20f)
            close()
        }
    }.build()

    val ArrowRight: ImageVector = Builder(
        name = "ArrowRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(20f, 12f)
            lineTo(12f, 4f)
            lineTo(12f, 8f)
            lineTo(4f, 8f)
            lineTo(4f, 16f)
            lineTo(12f, 16f)
            lineTo(12f, 20f)
            close()
        }
    }.build()
}
