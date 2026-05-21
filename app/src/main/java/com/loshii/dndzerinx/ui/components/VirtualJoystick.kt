package com.loshii.dndzerinx.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loshii.dndzerinx.model.game.Vector2

@Composable
fun VirtualJoystick(
    size: Dp = 150.dp,
    onDirectionChanged: (Vector2) -> Unit
) {
    var joystickCenter by remember { mutableStateOf<Offset?>(null) }
    var joystickCurrent by remember { mutableStateOf<Offset?>(null) }
    val maxRadius = size.value / 2 * 0.8f

    Canvas(
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.firstOrNull()?.position
                        when (event.type) {
                            PointerEventType.Press -> {
                                joystickCenter = position
                                joystickCurrent = position
                                onDirectionChanged(Vector2(0f, 0f))
                            }
                            PointerEventType.Move -> {
                                if (joystickCenter != null && position != null) {
                                    joystickCurrent = position
                                    val delta = position - joystickCenter!!
                                    val dist = delta.getDistance()
                                    val clampedDist = dist.coerceAtMost(maxRadius)
                                    val angle = kotlin.math.atan2(delta.y, delta.x)
                                    joystickCurrent = Offset(
                                        joystickCenter!!.x + kotlin.math.cos(angle) * clampedDist,
                                        joystickCenter!!.y + kotlin.math.sin(angle) * clampedDist
                                    )
                                    val dirX = (joystickCurrent!!.x - joystickCenter!!.x) / maxRadius
                                    val dirY = (joystickCurrent!!.y - joystickCenter!!.y) / maxRadius
                                    onDirectionChanged(Vector2(dirX, dirY))
                                }
                            }
                            PointerEventType.Release -> {
                                joystickCenter = null
                                joystickCurrent = null
                                onDirectionChanged(Vector2(0f, 0f))
                            }
                        }
                    }
                }
            }
    ) {
        joystickCenter?.let { center ->
            val current = joystickCurrent ?: center
            drawCircle(
                color = Color(0xFF333333).copy(alpha = 0.5f),
                radius = maxRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF666666).copy(alpha = 0.8f),
                radius = maxRadius * 0.35f,
                center = current
            )
        }
    }
}
