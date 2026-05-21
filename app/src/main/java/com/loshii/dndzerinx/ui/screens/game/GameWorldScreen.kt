package com.loshii.dndzerinx.ui.screens.game

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.data.LocalProfile
import com.loshii.dndzerinx.data.LocalProfileManager
import com.loshii.dndzerinx.engine.GodotLauncher
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.model.game.AttackSwing
import com.loshii.dndzerinx.model.game.DamageNumber
import com.loshii.dndzerinx.model.game.GameEngine
import com.loshii.dndzerinx.model.game.GameEvent
import com.loshii.dndzerinx.model.game.MonsterType
import com.loshii.dndzerinx.model.game.Particle
import com.loshii.dndzerinx.model.game.TileType
import com.loshii.dndzerinx.model.game.Vector2
import com.loshii.dndzerinx.model.game.WorldBounds
import com.loshii.dndzerinx.model.game.GameWorld
import com.loshii.dndzerinx.network.GameClient
import com.loshii.dndzerinx.network.ServerConfig
import com.loshii.dndzerinx.ui.components.VirtualJoystick
import com.loshii.dndzerinx.util.CoilGifImage
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameWorldScreen(
    viewModel: AuthViewModel,
    localProfileManager: LocalProfileManager,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onSignOut: () -> Unit,
    onDeath: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState(initial = null)
    val profile by localProfileManager.profile.collectAsState(initial = com.loshii.dndzerinx.data.LocalProfile())
    val client = remember { GameClient() }
    val localProfile = profile
    val currentUser = user

    var showProfileDialog by remember { mutableStateOf(false) }
    var showDeathOverlay by remember { mutableStateOf(false) }
    var levelUpMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var joystickDirection by remember { mutableStateOf(Vector2(0f, 0f)) }

    val worldBounds = remember { WorldBounds(1600f, 2400f) }
    val gameEngine = remember {
        GameWorld(
            bounds = worldBounds,
            playerLevel = localProfile.level,
            playerMaxHp = localProfile.maxHp,
            playerAtk = localProfile.attackPower,
            playerDef = localProfile.defense
        )
    }

    LaunchedEffect(Unit) {
        client.onMessage = { _ -> }
        client.connect(
            serverUrl = ServerConfig.GAME_WEBSOCKET_URL,
            playerId = currentUser?.id ?: "local_${Random.nextInt()}",
            playerName = localProfile.displayName,
            level = localProfile.level,
            maxHp = localProfile.maxHp
        )
    }

    DisposableEffect(Unit) {
        onDispose { client.disconnect() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val events = gameEngine.events.toList()
            gameEngine.clearEvents()
            for (event in events) {
                when (event) {
                    is GameEvent.MonsterDamaged -> {}
                    is GameEvent.MonsterKilled -> {
                        val xpGain = event.xpReward
                        gameEngine.playerGold += event.goldReward
                        if (gameEngine.gainXp(xpGain)) {
                            levelUpMessage = "¡Subiste a nivel ${gameEngine.playerLevel}!"
                        }
                        localProfileManager.addXp(xpGain)
                        localProfileManager.addGold(event.goldReward)
                        localProfileManager.updateHp(gameEngine.playerHp)
                    }
                    is GameEvent.PlayerDamaged -> {
                        if (gameEngine.playerHp <= 0) showDeathOverlay = true
                        localProfileManager.updateHp(gameEngine.playerHp)
                    }
                    is GameEvent.LevelUp -> {
                        localProfileManager.updateLevel(gameEngine.playerLevel)
                        localProfileManager.updateMaxHp(gameEngine.playerMaxHp)
                        localProfileManager.updateAttackPower(gameEngine.playerAtk)
                        localProfileManager.updateDefense(gameEngine.playerDef)
                        localProfileManager.updateHp(gameEngine.playerMaxHp)
                    }
                    is GameEvent.PlayerRespawned -> {
                        localProfileManager.updateHp(gameEngine.playerMaxHp)
                    }
                }
            }
            delay(50)
        }
    }

    LaunchedEffect(Unit) {
        val targetFps = 60L
        val frameDuration = 1000L / targetFps
        var lastTime = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val deltaTime = ((now - lastTime) / 1_000_000_000f).coerceAtMost(0.05f)
            lastTime = now
            currentTime = System.currentTimeMillis()
            gameEngine.update(deltaTime, currentTime)
            delay(frameDuration)
        }
    }

    LaunchedEffect(joystickDirection) {
        while (true) {
            if (joystickDirection.length() > 0.1f) {
                gameEngine.movePlayer(joystickDirection, 0.016f)
                client.sendMove(gameEngine.playerPosition.x, gameEngine.playerPosition.y)
            }
            delay(16)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (gameEngine.playerHp <= 0) return@detectTapGestures
                    val worldX = offset.x + gameEngine.cameraX - size.width / 2
                    val worldY = offset.y + gameEngine.cameraY - size.height / 2
                    val tapped = gameEngine.monsters
                        .filter { !it.isDead() }
                        .firstOrNull { m ->
                            val dx = worldX - m.position.x
                            val dy = worldY - m.position.y
                            kotlin.math.sqrt(dx * dx + dy * dy) < 60f
                        }
                    if (tapped != null) {
                        gameEngine.attackNearestMonster(currentTime)
                    }
                }
            }
        ) {
            val shakeOffset = gameEngine.getShakeOffset()
            val camX = gameEngine.cameraX
            val camY = gameEngine.cameraY
            val screenW = size.width
            val screenH = size.height

            clipRect {
                withTransform({
                    translate(
                        left = -camX + screenW / 2 + shakeOffset.first,
                        top = -camY + screenH / 2 + shakeOffset.second
                    )
                }) {
                    drawTileMap(gameEngine)
                    drawMonsters(gameEngine, currentTime)
                    drawMonsterHealthBars(gameEngine)
                    drawPlayer(gameEngine, currentUser, currentTime)
                    drawDamageNumbers(gameEngine, currentTime)
                    drawParticles(gameEngine)
                    drawAttackSwings(gameEngine)
                }
            }

            drawMinimap(gameEngine, size.width, size.height)
        }

        GameHud(
            engine = gameEngine,
            profile = localProfile,
            user = currentUser,
            levelUpMessage = levelUpMessage,
            showMenu = showMenu,
            onToggleMenu = { showMenu = !showMenu },
            onProfile = { showProfileDialog = true },
            onSettings = onNavigateToSettings,
            onChat = onNavigateToChat,
            onLibrary = onNavigateToLibrary,
            onSignOut = onSignOut,
            onLaunchGodot = {
                if (currentUser != null) {
                    GodotLauncher.launchGodot(
                        context = context,
                        playerId = currentUser.id,
                        playerName = localProfile.displayName,
                        level = localProfile.level,
                        maxHp = localProfile.maxHp,
                        accessKey = currentUser.accessKey.ifBlank { "local" },
                        serverUrl = ServerConfig.GAME_WEBSOCKET_URL
                    )
                }
            },
            onAttack = {
                val attacked = gameEngine.attackNearestMonster(currentTime)
                if (!attacked) {
                    Toast.makeText(context, "No hay enemigos cerca", Toast.LENGTH_SHORT).show()
                }
            }
        )

        VirtualJoystick(
            size = 150.dp,
            onDirectionChanged = { dir -> joystickDirection = dir }
        )

        if (showDeathOverlay) {
            DeathOverlay(
                onRespawn = {
                    gameEngine.respawnPlayer()
                    client.sendRespawn()
                    showDeathOverlay = false
                }
            )
        }

        if (showProfileDialog && currentUser != null) {
            ProfileDialog(
                profile = localProfile,
                user = currentUser,
                onDismiss = { showProfileDialog = false },
                onEditProfile = {
                    showProfileDialog = false
                    onNavigateToProfile()
                }
            )
        }

        if (levelUpMessage != null) {
            Box(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 100.dp)
            ) {
                AnimatedVisibility(
                    visible = levelUpMessage != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF1A1A1A))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = levelUpMessage ?: "",
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                LaunchedEffect(levelUpMessage) {
                    if (levelUpMessage != null) {
                        delay(3000)
                        levelUpMessage = null
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HP", color = Color(0xFF888888), fontSize = 10.sp)
                    Text("${gameEngine.playerHp}/${gameEngine.playerMaxHp}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LV", color = Color(0xFF888888), fontSize = 10.sp)
                    Text("${gameEngine.playerLevel}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ORO", color = Color(0xFF888888), fontSize = 10.sp)
                    Text("${gameEngine.playerGold}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ATK", color = Color(0xFF888888), fontSize = 10.sp)
                    Text("${gameEngine.playerAtk}", color = Color(0xFFFF6666), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEF", color = Color(0xFF888888), fontSize = 10.sp)
                    Text("${gameEngine.playerDef}", color = Color(0xFF66BBFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun DrawScope.drawTileMap(engine: GameEngine) {
    val map = engine.tileMap
    val tileSize = map.tileSize

    val startX = 0.coerceAtLeast(0)
    val startY = 0.coerceAtLeast(0)
    val endX = map.width
    val endY = map.height

    for (ty in startY until endY) {
        for (tx in startX until endX) {
            val tileType = TileType.values()[map.tiles[ty][tx]]
            val x = tx * tileSize
            val y = ty * tileSize
            val color = when (tileType) {
                TileType.GRASS -> Color(0xFF2D5016)
                TileType.DIRT -> Color(0xFF6B4423)
                TileType.STONE -> Color(0xFF4A4A4A)
                TileType.WATER -> Color(0xFF1A4A6B)
                TileType.SAND -> Color(0xFFC4A85A)
                TileType.DARK_STONE -> Color(0xFF2A2A2A)
            }
            drawRect(
                color = color,
                topLeft = Offset(x.toFloat(), y.toFloat()),
                size = Size(tileSize.toFloat(), tileSize.toFloat())
            )
            if (tileType == TileType.GRASS) {
                val shade = if ((tx + ty) % 2 == 0) Color(0xFF2A4A14) else Color(0xFF305518)
                drawRect(
                    color = shade,
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = Size(tileSize.toFloat(), tileSize.toFloat())
                )
            }
            if (tileType == TileType.STONE && (tx + ty) % 3 == 0) {
                drawRect(
                    color = Color(0xFF3E3E3E),
                    topLeft = Offset(x + 4f, y + 4f),
                    size = Size(tileSize - 8f, tileSize - 8f)
                )
            }
        }
    }
}

private fun DrawScope.drawMonsters(engine: GameEngine, currentTime: Long) {
    for (monster in engine.monsters) {
        if (monster.isDead()) continue
        val pos = monster.position
        val mc = Color(monster.type.color)
        val bodyRadius = 18f
        val pulse = sin(currentTime * 0.003 + pos.x) * 1.5f
        val pF = pulse.toFloat()

        drawCircle(color = mc.copy(alpha = 0.3f), radius = bodyRadius + 8f + pF, center = Offset(pos.x, pos.y + 4f))
        drawCircle(color = mc, radius = bodyRadius + pF, center = Offset(pos.x, pos.y))
        drawCircle(color = mc.copy(alpha = 0.7f), radius = bodyRadius * 0.6f, center = Offset(pos.x - 3f, pos.y - 2f))

        drawCircle(color = Color.White, radius = 4f, center = Offset(pos.x - 6f, pos.y - 5f))
        drawCircle(color = Color.White, radius = 4f, center = Offset(pos.x + 6f, pos.y - 5f))
        drawCircle(color = Color.Black, radius = 2f, center = Offset(pos.x - 6f + 1.5f, pos.y - 5f + 1.5f))
        drawCircle(color = Color.Black, radius = 2f, center = Offset(pos.x + 6f + 1.5f, pos.y - 5f + 1.5f))

        if (monster.state.name == "CHASE" || monster.state.name == "ATTACK") {
            drawCircle(color = mc.copy(alpha = 0.15f), radius = bodyRadius + 12f + pF, center = Offset(pos.x, pos.y))
        }

        val namePaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.WHITE
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            monster.type.displayName,
            pos.x,
            pos.y - bodyRadius - 12f,
            namePaint
        )
    }
}

private fun DrawScope.drawMonsterHealthBars(engine: GameEngine) {
    for (monster in engine.monsters) {
        if (monster.isDead() || monster.hp >= monster.maxHp) continue
        val barWidth = 36f
        val barHeight = 4f
        val x = monster.position.x - barWidth / 2
        val y = monster.position.y - 30f
        drawRoundRect(Color(0xFF333333), Offset(x, y), Size(barWidth, barHeight), CornerRadius(2f))
        val hpFraction = (monster.hp.toFloat() / monster.maxHp).coerceIn(0f, 1f)
        drawRoundRect(
            Color(0xFF44CC44),
            Offset(x, y),
            Size(barWidth * hpFraction, barHeight),
            CornerRadius(2f)
        )
    }
}

private fun DrawScope.drawPlayer(engine: GameEngine, user: User?, currentTime: Long) {
    val pos = engine.playerPosition
    val isHit = engine.playerHitFlash
    val hpFraction = (engine.playerHp.toFloat() / engine.playerMaxHp).coerceIn(0f, 1f)
    val pulse = (sin(currentTime * 0.004) * 2f).toFloat()

    if (isHit) {
        drawCircle(color = Color.Red.copy(alpha = 0.5f), radius = 32f + pulse, center = Offset(pos.x, pos.y))
        return
    }

    drawCircle(color = Color(0xFF222222).copy(alpha = 0.3f), radius = 24f + pulse, center = Offset(pos.x, pos.y + 4f))
    drawCircle(color = Color(0xFF4FC3F7), radius = 24f + pulse, center = Offset(pos.x, pos.y))
    drawCircle(color = Color(0xFF29B6F6).copy(alpha = 0.5f), radius = 18f, center = Offset(pos.x, pos.y))
    drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 12f, center = Offset(pos.x - 5f, pos.y - 5f))

    drawCircle(color = Color.White, radius = 5f, center = Offset(pos.x - 7f, pos.y - 6f))
    drawCircle(color = Color.White, radius = 5f, center = Offset(pos.x + 7f, pos.y - 6f))
    drawCircle(color = Color.Black, radius = 2.5f, center = Offset(pos.x - 7f + 1.5f, pos.y - 6f + 1.5f))
    drawCircle(color = Color.Black, radius = 2.5f, center = Offset(pos.x + 7f + 1.5f, pos.y - 6f + 1.5f))

    val hpBarWidth = 40f
    val hpBarHeight = 4f
    drawRoundRect(color = Color(0xFF333333), topLeft = Offset(pos.x - hpBarWidth / 2, pos.y - 38f), size = Size(hpBarWidth, hpBarHeight), cornerRadius = CornerRadius(2f))
    val hpColor = when {
        hpFraction > 0.5f -> Color(0xFF44CC44)
        hpFraction > 0.25f -> Color(0xFFCCCC00)
        else -> Color(0xFFCC4444)
    }
    drawRoundRect(color = hpColor, topLeft = Offset(pos.x - hpBarWidth / 2, pos.y - 38f), size = Size(hpBarWidth * hpFraction, hpBarHeight), cornerRadius = CornerRadius(2f))
}

private fun DrawScope.drawDamageNumbers(engine: GameEngine, currentTime: Long) {
    for (dn in engine.damageNumbers) {
        val elapsed = (currentTime - dn.startTime) / 1000f
        val alpha = (1f - elapsed / 1.5f).coerceIn(0f, 1f)
        val yOff = -elapsed * 40f
        val paint = android.graphics.Paint().apply {
            val a = (alpha * 255).toInt()
            this.color = android.graphics.Color.argb(
                a,
                ((dn.color shr 16) and 0xFF).toInt(),
                ((dn.color shr 8) and 0xFF).toInt(),
                (dn.color and 0xFF).toInt()
            )
            textSize = if (dn.isCrit) 28f else 22f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = dn.isCrit
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            if (dn.isCrit) "¡${dn.value}!" else "${dn.value}",
            dn.position.x,
            dn.position.y + yOff,
            paint
        )
    }
}

private fun DrawScope.drawParticles(engine: GameEngine) {
    for (p in engine.particles) {
        val color = Color(
            red = ((p.color shr 16) and 0xFF).toInt(),
            green = ((p.color shr 8) and 0xFF).toInt(),
            blue = (p.color and 0xFF).toInt(),
            alpha = (p.alpha * 255).toInt()
        )
        drawCircle(color, p.size, Offset(p.x, p.y))
    }
}

private fun DrawScope.drawAttackSwings(engine: GameEngine) {
    for (swing in engine.attackSwings) {
        val progress = swing.progress
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val sweepAngle = 120f * progress
        val startAngle = swing.angle * 180f / Math.PI.toFloat() - sweepAngle / 2f
        val color = Color(
            red = ((swing.color shr 16) and 0xFF).toInt(),
            green = ((swing.color shr 8) and 0xFF).toInt(),
            blue = (swing.color and 0xFF).toInt(),
            alpha = (alpha * 255).toInt()
        )
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(swing.x - swing.radius, swing.y - swing.radius),
            size = Size(swing.radius * 2, swing.radius * 2),
            style = Stroke(width = 4f * (1f - progress * 0.5f))
        )
    }
}

private fun DrawScope.drawMinimap(engine: GameEngine, screenW: Float, screenH: Float) {
    val mmSize = 100f
    val mmX = screenW - mmSize - 10f
    val mmY = 10f
    val scale = mmSize / engine.bounds.width.coerceAtLeast(engine.bounds.height)

    drawRoundRect(Color(0xCC000000), Offset(mmX, mmY), Size(mmSize, mmSize), CornerRadius(6f))

    for (monster in engine.monsters) {
        if (monster.isDead()) continue
        val mx = mmX + monster.position.x * scale
        val my = mmY + monster.position.y * scale
        drawCircle(Color(monster.type.color).copy(alpha = 0.7f), 2f, Offset(mx, my))
    }

    val px = mmX + engine.playerPosition.x * scale
    val py = mmY + engine.playerPosition.y * scale
    drawCircle(Color(0xFF4FC3F7), 3f, Offset(px, py))
}

@Composable
private fun DeathOverlay(onRespawn: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xCC000000)).clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CAÍSTE EN BATALLA",
                color = Color(0xFFFF4444),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Tu viaje termina aquí... por ahora",
                color = Color(0xFF888888),
                fontSize = 16.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRespawn,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7)),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Reaparecer", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
