package com.loshii.dndzerinx.ui.screens.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.loshii.dndzerinx.data.LocalProfile
import com.loshii.dndzerinx.data.LocalProfileManager
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.model.game.DamageNumber
import com.loshii.dndzerinx.model.game.EntityState
import com.loshii.dndzerinx.model.game.GameWorld
import com.loshii.dndzerinx.model.game.Monster
import com.loshii.dndzerinx.model.game.MonsterType
import com.loshii.dndzerinx.model.game.Vector2
import com.loshii.dndzerinx.model.game.WorldBounds
import com.loshii.dndzerinx.network.GameClient
import com.loshii.dndzerinx.network.ServerMessage
import com.loshii.dndzerinx.ui.components.VirtualJoystick
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RemotePlayer(
    val id: String,
    val name: String,
    val position: Vector2,
    val hp: Int,
    val maxHp: Int,
    val level: Int,
    val isDead: Boolean
)

@Composable
fun GameWorldScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val localProfileManager = remember { LocalProfileManager(context) }
    val localProfile by localProfileManager.profile.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val user by viewModel.currentUser.collectAsState()

    val worldBounds = remember { WorldBounds(800f, 1600f) }
    val gameWorld = remember {
        GameWorld(
            bounds = worldBounds,
            playerLevel = localProfile?.level ?: 1,
            playerMaxHp = localProfile?.maxHp ?: 100,
            playerAtk = localProfile?.attackPower ?: 10,
            playerDef = localProfile?.defense ?: 5
        )
    }

    val gameClient = remember { GameClient() }
    val isConnected by gameClient.connected.collectAsState()
    val remotePlayersState by gameClient.remotePlayers.collectAsState()
    val remoteMonstersState by gameClient.remoteMonsters.collectAsState()

    val remotePlayers = remember { mutableStateListOf<RemotePlayer>() }
    val localDamageNumbers = remember { mutableStateListOf<DamageNumber>() }

    var playerAvatar by remember { mutableStateOf<ImageBitmap?>(null) }
    val avatarUrl = localProfile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl

    LaunchedEffect(avatarUrl) {
        if (avatarUrl.isNullOrBlank()) return@LaunchedEffect
        try {
            val loader = ImageLoader.Builder(context).crossfade(true).build()
            val request = ImageRequest.Builder(context).data(avatarUrl).size(128).build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = android.graphics.Bitmap.createBitmap(128, 128, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                playerAvatar = bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            playerAvatar = null
        }
    }

    LaunchedEffect(localProfile) {
        localProfile?.let { profile ->
            gameWorld.playerLevel = profile.level
            gameWorld.playerMaxHp = profile.maxHp
            gameWorld.playerAtk = profile.attackPower
            gameWorld.playerDef = profile.defense
        }
    }

    LaunchedEffect(user?.id) {
        val profile = localProfileManager.profile.first()
        if (user != null && !isConnected) {
            gameClient.connect(
                serverUrl = "wss://servermmo.onrender.com",
                playerId = user!!.id,
                playerName = profile.displayName,
                level = profile.level,
                maxHp = profile.maxHp
            )
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { remotePlayersState.values.toList() }.collect { states ->
            remotePlayers.clear()
            states.forEach { state ->
                remotePlayers.add(
                    RemotePlayer(
                        id = state.id, name = state.name,
                        position = Vector2(state.x, state.y),
                        hp = state.hp, maxHp = state.maxHp,
                        level = state.level, isDead = state.isDead
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { remoteMonstersState.values.toList() }.collect { states ->
            states.forEach { state ->
                val existing = gameWorld.monsters.find { it.id == state.id }
                if (existing != null) {
                    existing.position = Vector2(state.x, state.y)
                    existing.hp = state.hp
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        gameClient.messages.collect { messages ->
            val latest = messages.lastOrNull() ?: return@collect
            when (latest) {
                is ServerMessage.PlayerAttacked -> {
                    val monster = gameWorld.monsters.find { it.id == latest.monsterId }
                    monster?.let {
                        localDamageNumbers.add(
                            DamageNumber(
                                value = latest.damage,
                                position = Vector2(it.position.x, it.position.y - 20f),
                                isCrit = latest.isCrit,
                                color = if (latest.isCrit) 0xFFFF0000 else 0xFFFFFFFF,
                                startTime = System.currentTimeMillis()
                            )
                        )
                    }
                }
                is ServerMessage.PlayerDamaged -> {
                    if (latest.playerId == user?.id) {
                        gameWorld.playerHp = latest.currentHp
                        if (latest.currentHp <= 0) {
                            gameWorld.playerState = EntityState.DEAD
                        }
                        scope.launch { localProfileManager.updateHp(latest.currentHp) }
                        localDamageNumbers.add(
                            DamageNumber(
                                value = latest.damage,
                                position = Vector2(gameWorld.playerPosition.x, gameWorld.playerPosition.y - 30f),
                                isCrit = false, color = 0xFFFF4444,
                                startTime = System.currentTimeMillis()
                            )
                        )
                    }
                }
                is ServerMessage.MonsterDied -> {
                    val monster = gameWorld.monsters.find { it.id == latest.monsterId }
                    monster?.let {
                        it.hp = 0
                        scope.launch {
                            localProfileManager.addXp(latest.xpReward)
                            localProfileManager.addGold(latest.goldReward)
                        }
                        localDamageNumbers.add(
                            DamageNumber(
                                value = latest.xpReward,
                                position = Vector2(it.position.x, it.position.y - 20f),
                                isCrit = false, color = 0xFFFFFF00,
                                startTime = System.currentTimeMillis()
                            )
                        )
                    }
                }
                is ServerMessage.PlayerRespawned -> {
                    if (latest.playerId == user?.id) {
                        gameWorld.playerPosition = Vector2(latest.x, latest.y)
                        gameWorld.playerHp = latest.hp
                        gameWorld.playerState = EntityState.IDLE
                        scope.launch { localProfileManager.updateHp(latest.hp) }
                    }
                }
                else -> {}
            }
        }
    }

    var cameraOffset by remember { mutableStateOf(Vector2(0f, 0f)) }
    var currentTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    var lastMoveSend by remember { mutableStateOf(0L) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (isRunning) {
            currentTime = System.currentTimeMillis()
            gameWorld.update(0.016f, currentTime)

            val now = System.currentTimeMillis()
            if (now - lastMoveSend > 50 && isConnected) {
                gameClient.sendMove(gameWorld.playerPosition.x, gameWorld.playerPosition.y)
                lastMoveSend = now
            }

            if (canvasSize != androidx.compose.ui.geometry.Size.Zero) {
                cameraOffset = Vector2(
                    gameWorld.playerPosition.x - canvasSize.width / 2,
                    gameWorld.playerPosition.y - canvasSize.height / 2
                )
            }
            delay(16)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isRunning = false
            gameClient.disconnect()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2D5016))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { intSize ->
                    canvasSize = ComposeSize(intSize.width.toFloat(), intSize.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val worldX = offset.x + cameraOffset.x
                        val worldY = offset.y + cameraOffset.y
                        val playerScreenX = gameWorld.playerPosition.x - cameraOffset.x
                        val playerScreenY = gameWorld.playerPosition.y - cameraOffset.y
                        val distToPlayer = kotlin.math.sqrt(
                            (worldX - playerScreenX) * (worldX - playerScreenX) +
                            (worldY - playerScreenY) * (worldY - playerScreenY)
                        )
                        if (distToPlayer < 30f) {
                            showProfileDialog = true
                            return@detectTapGestures
                        }
                        val nearest = gameWorld.monsters
                            .filter { !it.isDead() }
                            .minByOrNull { monster ->
                                val dx = monster.position.x - worldX
                                val dy = monster.position.y - worldY
                                dx * dx + dy * dy
                            }
                        nearest?.let { monster ->
                            if (isConnected) {
                                gameClient.sendAttack(monster.id)
                            } else {
                                gameWorld.attackNearestMonster(System.currentTimeMillis())
                            }
                        }
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
                            Color(0xFF2D5016), radius = 8f,
                            center = Offset(x.toFloat() + 40f, y.toFloat() + 40f)
                        )
                    }
                }
            }

            gameWorld.monsters.forEach { monster ->
                if (monster.isDead()) return@forEach
                drawMonster(monster, cameraOffset, canvasWidth, canvasHeight)
            }

            remotePlayers.forEach { player ->
                if (player.isDead) return@forEach
                drawRemotePlayer(player, cameraOffset, canvasWidth, canvasHeight)
            }

            drawPlayer(gameWorld, cameraOffset, canvasWidth, canvasHeight, playerAvatar)

            localDamageNumbers.forEach { dn ->
                drawDamageNumber(dn, cameraOffset, currentTime)
            }
            gameWorld.damageNumbers.forEach { dn ->
                drawDamageNumber(dn, cameraOffset, currentTime)
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
            }
        }

        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .width(200.dp)
                ) {
                    MenuItem(Icons.Default.Person, "Perfil") {
                        showMenu = false
                        showProfileDialog = true
                    }
                    MenuItem(Icons.Default.List, "Inventario") {
                        showMenu = false
                        onNavigateToProfile()
                    }
                    MenuItem(Icons.Default.Settings, "Ajustes") {
                        showMenu = false
                        onNavigateToSettings()
                    }
                    MenuItem(Icons.Default.ExitToApp, "Cerrar sesión") {
                        showMenu = false
                        onSignOut()
                    }
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
                    Text("Has muerto", color = Color.Red, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (isConnected) gameClient.sendRespawn()
                            else gameWorld.respawnPlayer()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Reaparecer", fontSize = 18.sp)
                    }
                }
            }
        }

        if (showProfileDialog) {
            ProfileDialog(
                profile = localProfile,
                user = user,
                onDismiss = { showProfileDialog = false },
                onEditProfile = {
                    showProfileDialog = false
                    onNavigateToProfile()
                }
            )
        }
    }
}

@Composable
private fun MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
private fun ProfileDialog(
    profile: LocalProfile?,
    user: com.loshii.dndzerinx.model.User?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Perfil", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val imgUrl = profile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl
                    if (!imgUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imgUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2196F3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.displayName ?: "Jugador").take(1).uppercase(),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile?.displayName ?: "Jugador",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "${profile?.race ?: "Humano"} - ${profile?.characterClass ?: "Guerrero"}",
                    color = Color(0xFFBBBBBB),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    StatRow("Nivel", "${profile?.level ?: 1}")
                    StatRow("XP", "${profile?.xp ?: 0} / ${(profile?.level ?: 1) * 100}")
                    StatRow("Oro", "${profile?.gold ?: 0}")
                    StatRow("HP", "${profile?.hp ?: 100} / ${profile?.maxHp ?: 100}")
                    StatRow("Ataque", "${profile?.attackPower ?: 10}")
                    StatRow("Defensa", "${profile?.defense ?: 5}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar perfil")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF999999), fontSize = 16.sp)
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

private fun DrawScope.drawRemotePlayer(
    player: RemotePlayer,
    cameraOffset: Vector2,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val screenX = player.position.x - cameraOffset.x
    val screenY = player.position.y - cameraOffset.y
    if (screenX < -50 || screenX > canvasWidth + 50 || screenY < -50 || screenY > canvasHeight + 50) return

    drawCircle(color = Color(0xFF9C27B0).copy(alpha = 0.3f), radius = 35f, center = Offset(screenX, screenY))
    drawCircle(color = Color(0xFF9C27B0), radius = 25f, center = Offset(screenX, screenY))

    val hpPercent = player.hp.toFloat() / player.maxHp
    val barWidth = 60f
    val barHeight = 6f
    val barY = screenY - 40f
    drawRect(color = Color(0xFF333333), topLeft = Offset(screenX - barWidth / 2, barY), size = ComposeSize(barWidth, barHeight))
    drawRect(
        color = if (hpPercent > 0.5f) Color(0xFF4CAF50) else if (hpPercent > 0.25f) Color(0xFFFF9800) else Color(0xFFF44336),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = ComposeSize(barWidth * hpPercent, barHeight)
    )
    drawContext.canvas.nativeCanvas.drawText(
        player.name, screenX - 20f, barY - 4f,
        Paint().apply { color = android.graphics.Color.WHITE; textSize = 11f; isAntiAlias = true }
    )
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

    drawCircle(color = monsterColor.copy(alpha = 0.3f), radius = radius + 8f, center = Offset(screenX, screenY))
    drawCircle(color = monsterColor, radius = radius, center = Offset(screenX, screenY))
    drawCircle(color = Color.White.copy(alpha = 0.3f), radius = radius * 0.4f, center = Offset(screenX - radius * 0.2f, screenY - radius * 0.2f))

    val hpPercent = monster.hp.toFloat() / monster.maxHp
    val barWidth = radius * 2.5f
    val barHeight = 5f
    val barY = screenY - radius - 12f
    drawRect(color = Color(0xFF333333), topLeft = Offset(screenX - barWidth / 2, barY), size = ComposeSize(barWidth, barHeight))
    drawRect(
        color = if (hpPercent > 0.5f) Color(0xFF4CAF50) else if (hpPercent > 0.25f) Color(0xFFFF9800) else Color(0xFFF44336),
        topLeft = Offset(screenX - barWidth / 2, barY),
        size = ComposeSize(barWidth * hpPercent, barHeight)
    )
    drawContext.canvas.nativeCanvas.drawText(
        "${monster.type.displayName} Lv.${monster.level}", screenX - 40f, barY - 4f,
        Paint().apply { color = android.graphics.Color.WHITE; textSize = 10f; isAntiAlias = true }
    )
}

private fun DrawScope.drawPlayer(
    gameWorld: GameWorld,
    cameraOffset: Vector2,
    canvasWidth: Float,
    canvasHeight: Float,
    avatar: ImageBitmap?
) {
    val screenX = gameWorld.playerPosition.x - cameraOffset.x
    val screenY = gameWorld.playerPosition.y - cameraOffset.y

    drawCircle(color = Color(0xFF2196F3).copy(alpha = 0.3f), radius = 35f, center = Offset(screenX, screenY))

    if (avatar != null) {
        val circlePath = Path().apply { addOval(Rect(screenX - 25f, screenY - 25f, screenX + 25f, screenY + 25f)) }
        clipPath(circlePath) {
            drawImage(image = avatar, dstSize = IntSize(50, 50), dstOffset = IntOffset((screenX - 25f).toInt(), (screenY - 25f).toInt()))
        }
        drawCircle(color = Color(0xFF2196F3), radius = 25f, center = Offset(screenX, screenY), style = Stroke(width = 3f))
    } else {
        drawCircle(color = Color(0xFF2196F3), radius = 25f, center = Offset(screenX, screenY))
        drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 10f, center = Offset(screenX - 5f, screenY - 5f))
    }
}

private fun DrawScope.drawDamageNumber(dn: DamageNumber, cameraOffset: Vector2, currentTime: Long) {
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
