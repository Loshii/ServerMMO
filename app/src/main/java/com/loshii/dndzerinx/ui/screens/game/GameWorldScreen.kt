package com.loshii.dndzerinx.ui.screens.game

import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.model.game.DamageNumber
import com.loshii.dndzerinx.model.game.EntityState
import com.loshii.dndzerinx.model.game.GameWorld
import com.loshii.dndzerinx.model.game.Monster
import com.loshii.dndzerinx.model.game.MonsterType
import com.loshii.dndzerinx.model.game.Vector2
import com.loshii.dndzerinx.model.game.WorldBounds
import com.loshii.dndzerinx.ui.components.VirtualJoystick
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun GameWorldScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val u = user

    val worldBounds = remember { WorldBounds(800f, 1600f) }
    val gameWorld = remember {
        GameWorld(
            bounds = worldBounds,
            playerLevel = u?.level ?: 1,
            playerMaxHp = u?.characterStatus?.maxHealth ?: 100,
            playerAtk = u?.characterStatus?.attackPower ?: 10,
            playerDef = u?.characterStatus?.defense ?: 5
        )
    }

    var direction by remember { mutableStateOf(Vector2(0f, 0f)) }
    var cameraOffset by remember { mutableStateOf(Vector2(0f, 0f)) }
    var currentTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }

    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    LaunchedEffect(Unit) {
        while (isRunning) {
            currentTime = System.currentTimeMillis()
            gameWorld.update(0.016f, currentTime)
            if (canvasSize != androidx.compose.ui.geometry.Size.Zero) {
                cameraOffset = Vector2(
                    gameWorld.playerPosition.x - canvasSize.width / 2,
                    gameWorld.playerPosition.y - canvasSize.height / 2
                )
            }
            delay(16)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2D5016))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { intSize ->
                    canvasSize = Size(intSize.width.toFloat(), intSize.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        gameWorld.attackNearestMonster(System.currentTimeMillis())
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawRect(Color(0xFF3A6B18), size = size)

            for (x in 0 until canvasWidth.toInt() step 80) {
                for (y in 0 until canvasHeight.toInt() step 80) {
                    val worldX = x + cameraOffset.x
                    val worldY = y + cameraOffset.y
                    val hash = (worldX.toInt() * 31 + worldY.toInt() * 17) % 100
                    if (hash < 15) {
                        drawCircle(
                            Color(0xFF2D5016),
                            radius = 8f,
                            center = Offset(x.toFloat() + 40f, y.toFloat() + 40f)
                        )
                    }
                }
            }

            gameWorld.monsters.forEach { monster ->
                if (monster.isDead()) return@forEach
                drawMonster(monster, cameraOffset, canvasWidth, canvasHeight)
            }

            drawPlayer(gameWorld, cameraOffset, canvasWidth, canvasHeight)

            gameWorld.damageNumbers.forEach { dn ->
                drawDamageNumber(dn, cameraOffset, currentTime)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                GameHud(
                    hp = gameWorld.playerHp,
                    maxHp = gameWorld.playerMaxHp,
                    xp = gameWorld.playerXp,
                    gold = gameWorld.playerGold,
                    level = u?.level ?: 1,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VirtualJoystick(
                    size = 130.dp,
                    onDirectionChanged = { dir ->
                        direction = dir
                        gameWorld.movePlayer(dir, 0.016f)
                    }
                )
                Button(
                    onClick = {
                        gameWorld.attackNearestMonster(System.currentTimeMillis())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    modifier = Modifier
                        .size(90.dp)
                ) {
                    Text("⚔️", fontSize = 36.sp)
                }
            }
        }

        if (gameWorld.playerState == EntityState.DEAD) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("💀 Has muerto", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            gameWorld.respawnPlayer()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Reaparecer", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMonster(
    monster: Monster,
    cameraOffset: Vector2,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val screenX = monster.position.x - cameraOffset.x
    val screenY = monster.position.y - cameraOffset.y

    if (screenX < -50 || screenX > canvasWidth + 50 || screenY < -50 || screenY > canvasHeight + 50) return

    val monsterColor = Color(monster.type.color)
    val radius = 20f + monster.level * 2f

    drawCircle(
        color = monsterColor.copy(alpha = 0.3f),
        radius = radius + 8f,
        center = Offset(screenX, screenY)
    )

    drawCircle(
        color = monsterColor,
        radius = radius,
        center = Offset(screenX, screenY)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius * 0.4f,
        center = Offset(screenX - radius * 0.2f, screenY - radius * 0.2f)
    )

    val hpPercent = monster.hp.toFloat() / monster.maxHp
    val barWidth = radius * 2.5f
    val barHeight = 5f
    val barY = screenY - radius - 12f

    drawRect(
        color = Color(0xFF333333),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = Size(barWidth, barHeight)
    )
    drawRect(
        color = if (hpPercent > 0.5f) Color(0xFF4CAF50) else if (hpPercent > 0.25f) Color(0xFFFF9800) else Color(0xFFF44336),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = Size(barWidth * hpPercent, barHeight)
    )

    drawContext.canvas.nativeCanvas.drawText(
        "${monster.type.displayName} Lv.${monster.level}",
        screenX - 40f,
        barY - 4f,
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10f
            isAntiAlias = true
        }
    )
}

private fun DrawScope.drawPlayer(
    gameWorld: GameWorld,
    cameraOffset: Vector2,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val screenX = gameWorld.playerPosition.x - cameraOffset.x
    val screenY = gameWorld.playerPosition.y - cameraOffset.y

    drawCircle(
        color = Color(0xFF2196F3).copy(alpha = 0.3f),
        radius = 35f,
        center = Offset(screenX, screenY)
    )

    drawCircle(
        color = Color(0xFF2196F3),
        radius = 25f,
        center = Offset(screenX, screenY)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = 10f,
        center = Offset(screenX - 5f, screenY - 5f)
    )

    val hpPercent = gameWorld.playerHp.toFloat() / gameWorld.playerMaxHp
    val barWidth = 60f
    val barHeight = 6f
    val barY = screenY - 40f

    drawRect(
        color = Color(0xFF333333),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = Size(barWidth, barHeight)
    )
    drawRect(
        color = if (hpPercent > 0.5f) Color(0xFF4CAF50) else if (hpPercent > 0.25f) Color(0xFFFF9800) else Color(0xFFF44336),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = Size(barWidth * hpPercent, barHeight)
    )

    drawContext.canvas.nativeCanvas.drawText(
        "Jugador",
        screenX - 20f,
        barY - 4f,
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            isAntiAlias = true
        }
    )
}

private fun DrawScope.drawDamageNumber(
    dn: DamageNumber,
    cameraOffset: Vector2,
    currentTime: Long
) {
    val elapsed = (currentTime - dn.startTime) / 1000f
    if (elapsed > 1f) return

    val screenX = dn.position.x - cameraOffset.x
    val screenY = dn.position.y - cameraOffset.y - elapsed * 40f

    val paint = Paint().apply {
        color = (dn.color and 0xFFFFFF).toInt()
        textSize = if (dn.isCrit) 22f else 16f
        isAntiAlias = true
        isFakeBoldText = dn.isCrit
    }
    val text = if (dn.isCrit) "💥${dn.value}" else "${dn.value}"
    drawContext.canvas.nativeCanvas.drawText(text, screenX - 15f, screenY, paint)
}

@Composable
private fun GameHud(
    hp: Int,
    maxHp: Int,
    xp: Int,
    gold: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("❤️", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "$hp/$maxHp",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("⭐", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Lv.$level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(12.dp))
            Text("💰", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("$gold", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val xpPercent = (xp % 100) / 100f
        Surface(
            modifier = Modifier.fillMaxWidth().height(6.dp),
            shape = RoundedCornerShape(3.dp),
            color = Color(0xFF333333)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(xpPercent.coerceIn(0f, 1f)).height(6.dp),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFFFFC107)
            ) {}
        }
    }
}
