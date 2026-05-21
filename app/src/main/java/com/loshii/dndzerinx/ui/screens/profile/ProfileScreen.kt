package com.loshii.dndzerinx.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.model.EquipmentItem
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.util.CoilGifImage
import com.loshii.dndzerinx.ui.components.EquipmentPaperDoll
import com.loshii.dndzerinx.ui.components.EquipmentSlotUi
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import com.loshii.dndzerinx.viewmodel.ImageType

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSignOut: () -> Unit,
    onOpenCharacter: () -> Unit = {},
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val u = user ?: return Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }

    val context = LocalContext.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showBannerMenu by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf(u.displayName) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(ImageType.AVATAR, context, it) {} }
    }

    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(ImageType.BANNER, context, it) {} }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            CoilGifImage(
                model = u.bannerUrl?.takeIf { it.isNotBlank() } ?: "https://picsum.photos/1000/300",
                contentDescription = "Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            IconButton(
                onClick = { showBannerMenu = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configuración del banner",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showBannerMenu,
                onDismissRequest = { showBannerMenu = false },
                modifier = Modifier.width(200.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        showBannerMenu = false
                        viewModel.signOut()
                        onSignOut()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cambiar contraseña") },
                    onClick = {
                        showBannerMenu = false
                        showPasswordDialog = true
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset(y = (-40).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(3.dp)
                    .clickable { avatarPicker.launch("image/*") }
            ) {
                CoilGifImage(
                    model = u.avatarUrl ?: "",
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(24.dp)
                    .clickable { showEditDialog = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-25).dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = u.guildLeaderRoleName?.takeIf { it.isNotBlank() } ?: u.role.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = u.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "@${u.displayName.lowercase().replace(" ", "")}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Nv ${u.level}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterInfoCard(u, viewModel)
                InlineBar(label = "Vida", current = u.characterStatus.health, max = u.characterStatus.maxHealth, color = Color(0xFF4CAF50))
                InlineBar(label = "Energia", current = u.characterStatus.energy, max = u.characterStatus.maxEnergy, color = Color(0xFFFFC107))
                InlineBar(label = "Mana", current = u.characterStatus.mana, max = u.characterStatus.maxMana, color = Color(0xFF2196F3))
                InlineBar(label = "Estamina", current = u.characterStatus.stamina, max = u.characterStatus.maxStamina, color = Color(0xFF9C27B0))
                val xpPct = if (u.experienceToNextLevel > 0) (u.experience.toFloat() / u.experienceToNextLevel.toFloat()) else 0f
                InlineBar(label = "Experiencia", current = (xpPct * u.experienceToNextLevel).toInt(), max = u.experienceToNextLevel, color = MaterialTheme.colorScheme.tertiary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estados", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (u.characterStatus.skillPoints > 0) MaterialTheme.colorScheme.primary else Color.Gray
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${u.characterStatus.skillPoints}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier.clickable { viewModel.addExperience(100) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                        ) {
                            Text(
                                "+100 XP",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusStatTile(StatIconKind.Strength, "FUE", u.characterStatus.strength, "strength", viewModel, u.characterStatus.skillPoints, Color(0xFFD36B4C), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Defense, "DEF", u.characterStatus.defense, "defense", viewModel, u.characterStatus.skillPoints, Color(0xFF607D8B), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Agility, "DES", u.characterStatus.agility, "agility", viewModel, u.characterStatus.skillPoints, Color(0xFF4CAF50), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusStatTile(StatIconKind.Magic, "MAG", u.characterStatus.magic, "magic", viewModel, u.characterStatus.skillPoints, Color(0xFF7E57C2), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Intelligence, "INT", u.characterStatus.intelligence, "intelligence", viewModel, u.characterStatus.skillPoints, Color(0xFF3F51B5), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Charisma, "CAR", u.characterStatus.charisma, "charisma", viewModel, u.characterStatus.skillPoints, Color(0xFFE91E63), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusStatTile(StatIconKind.Speed, "VEL", u.characterStatus.speed, "speed", viewModel, u.characterStatus.skillPoints, Color(0xFF009688), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Spirit, "ESP", u.characterStatus.spirit, "spirit", viewModel, u.characterStatus.skillPoints, Color(0xFF00ACC1), Modifier.weight(1f))
                    StatusStatTile(StatIconKind.Tenacity, "TEN", u.characterStatus.tenacity, "tenacity", viewModel, u.characterStatus.skillPoints, Color(0xFF795548), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusStatTile(StatIconKind.MagicShield, "RM", u.characterStatus.magicResist, "magicResist", viewModel, u.characterStatus.skillPoints, Color(0xFF5E35B1), Modifier.weight(1f), true)
                    StatusStatTile(StatIconKind.Shield, "RF", u.characterStatus.physicalResist, "physicalResist", viewModel, u.characterStatus.skillPoints, Color(0xFF546E7A), Modifier.weight(1f), true)
                    StatusStatTile(StatIconKind.Amplifier, "AMP", u.characterStatus.amplifier, "amplifier", viewModel, u.characterStatus.skillPoints, Color(0xFFF57C00), Modifier.weight(1f), true)
                }
                Spacer(modifier = Modifier.height(10.dp))
                AlteredStatusEffects(u.statusEffects)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable { onOpenCharacter() },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Personaje", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Equipo, Inventario y Habilidades", fontSize = 12.sp, color = Color.Gray)
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Monedero", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniCoin(u.wallet.gold, "O", Color(0xFFFFD700))
                    MiniCoin(u.wallet.silver, "P", Color(0xFFC0C0C0))
                    MiniCoin(u.wallet.copper, "C", Color(0xFFB87333))
                    MiniCoin(u.wallet.gems, "G", Color(0xFF00BCD4))
                }
            }
        }
    }

    if (showEditNameDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Cambiar Nombre") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (newName.isNotBlank() && newName.length >= 2) {
                        viewModel.updateDisplayName(newName)
                        showEditNameDialog = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditNameDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Cambiar contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword == confirmPassword && newPassword.length >= 6) {
                        viewModel.changePassword(newPassword)
                        showPasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Perfil") },
            text = {
                Column {
                    androidx.compose.material3.TextButton(onClick = { showEditDialog = false; avatarPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Cambiar Avatar") }
                    androidx.compose.material3.TextButton(onClick = { showEditDialog = false; bannerPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Cambiar Banner") }
                    androidx.compose.material3.TextButton(onClick = { showEditDialog = false; newName = u.displayName; showEditNameDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Cambiar Nombre") }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditDialog = false }) { Text("Cerrar") }
            }
        )
    }
}

private enum class StatIconKind {
    Strength, Defense, Agility, Magic, Intelligence, Charisma,
    Speed, Spirit, Tenacity, MagicShield, Shield, Crit, Amplifier, Luck,
    Poison, Burn, Freeze, Stun, Blind, Clear
}

@Composable
private fun CharacterInfoCard(user: User, viewModel: AuthViewModel) {
    val titles = (user.titles.ifEmpty { user.tags }).take(3)
    val context = LocalContext.current
    var showSpeciesDialog by remember { mutableStateOf(false) }
    val speciesList = remember {
        com.loshii.dndzerinx.util.JsonSupport.fromAsset<List<com.loshii.dndzerinx.model.SpeciesDefinition>>(context, "species.json") ?: emptyList()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Informacion de personaje", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(user.race.ifBlank { "Humano" }, modifier = Modifier.clickable { showSpeciesDialog = true }, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                Text(
                    user.guildLeaderRoleName?.takeIf { it.isNotBlank() } ?: user.role.displayName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (titles.isEmpty()) {
                TitleChip("Sin titulo")
            } else {
                titles.forEach { TitleChip(it) }
            }
        }
    }

    if (showSpeciesDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSpeciesDialog = false },
            title = { Text("Seleccionar Especie") },
            text = {
                Column {
                    if (speciesList.isEmpty()) {
                        Text("No hay especies definidas en assets/species.json")
                    } else {
                        speciesList.forEach { s ->
                            androidx.compose.material3.TextButton(onClick = {
                                showSpeciesDialog = false
                                viewModel.applySpecies(s)
                            }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                                    Text(s.name, fontWeight = FontWeight.Bold)
                                    if (s.stats.isNotEmpty()) Text(s.stats.entries.joinToString { "${it.key}: ${it.value}" }, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showSpeciesDialog = false }) { Text("Cerrar") }
            }
        )
    }
}

@Composable
private fun TitleChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            DrawStatIcon(StatIconKind.Luck, MaterialTheme.colorScheme.tertiary, Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text, color = MaterialTheme.colorScheme.tertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun IconStateStat(kind: StatIconKind, label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawStatIcon(kind, color, Modifier.size(24.dp))
            Text(value.toString(), color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatusStatTile(
    kind: StatIconKind,
    label: String,
    value: Int,
    statKey: String,
    viewModel: AuthViewModel,
    availablePoints: Int,
    color: Color,
    modifier: Modifier = Modifier,
    isPercent: Boolean = false
) {
    val hasPoints = availablePoints > 0
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawStatIcon(kind, color, Modifier.size(23.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("$value${if (isPercent) "%" else ""}", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (hasPoints) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(modifier = Modifier.size(18.dp).clickable { viewModel.updateCharacterStat(statKey, 1) }, shape = CircleShape, color = color) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Subir $label", tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AlteredStatusEffects(effects: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Estados alterados", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        val visibleEffects = if (effects.isEmpty()) listOf("Sin efectos") else effects.take(6)
        visibleEffects.chunked(3).forEach { rowEffects ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowEffects.forEach { effect ->
                    EffectChip(effect, Modifier.weight(1f), effects.isEmpty())
                }
                repeat(3 - rowEffects.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EffectChip(effect: String, modifier: Modifier = Modifier, clear: Boolean = false) {
    val color = if (clear) Color(0xFF4CAF50) else effectColor(effect)
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            DrawStatIcon(if (clear) StatIconKind.Clear else effectKind(effect), color, Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(effect, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

private fun effectKind(effect: String): StatIconKind {
    val normalized = effect.lowercase()
    return when {
        "ven" in normalized || "poison" in normalized -> StatIconKind.Poison
        "quem" in normalized || "burn" in normalized || "fuego" in normalized -> StatIconKind.Burn
        "cong" in normalized || "freeze" in normalized || "hielo" in normalized -> StatIconKind.Freeze
        "atur" in normalized || "stun" in normalized -> StatIconKind.Stun
        "cie" in normalized || "blind" in normalized -> StatIconKind.Blind
        else -> StatIconKind.Stun
    }
}

private fun effectColor(effect: String): Color {
    val normalized = effect.lowercase()
    return when {
        "ven" in normalized || "poison" in normalized -> Color(0xFF7CB342)
        "quem" in normalized || "burn" in normalized || "fuego" in normalized -> Color(0xFFE65100)
        "cong" in normalized || "freeze" in normalized || "hielo" in normalized -> Color(0xFF039BE5)
        "cie" in normalized || "blind" in normalized -> Color(0xFF5D4037)
        else -> Color(0xFF7E57C2)
    }
}

@Composable
private fun DrawStatIcon(kind: StatIconKind, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.12f, cap = StrokeCap.Round)
        when (kind) {
            StatIconKind.Strength -> {
                drawLine(color, Offset(size.width * 0.25f, size.height * 0.72f), Offset(size.width * 0.72f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(color, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.72f, size.height * 0.25f), style = Stroke(stroke.width))
            }
            StatIconKind.Defense, StatIconKind.Shield, StatIconKind.MagicShield, StatIconKind.Tenacity -> {
                drawRoundRect(color, topLeft = Offset(size.width * 0.2f, size.height * 0.16f), size = Size(size.width * 0.6f, size.height * 0.68f), cornerRadius = CornerRadius(size.width * 0.14f), style = Stroke(stroke.width))
                if (kind == StatIconKind.MagicShield) drawCircle(color, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.48f))
                if (kind == StatIconKind.Tenacity) drawLine(color, Offset(size.width * 0.34f, size.height * 0.5f), Offset(size.width * 0.66f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Agility, StatIconKind.Speed -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.62f), Offset(size.width * 0.82f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.45f, size.height * 0.32f), Offset(size.width * 0.82f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.45f, size.height * 0.82f), Offset(size.width * 0.82f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Magic, StatIconKind.Spirit, StatIconKind.Amplifier -> {
                drawCircle(color, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(stroke.width))
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.18f), Offset(size.width * 0.5f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.5f), Offset(size.width * 0.82f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                if (kind == StatIconKind.Spirit) drawCircle(color, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            }
            StatIconKind.Intelligence -> {
                drawCircle(color, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.36f), style = Stroke(stroke.width))
                drawLine(color, Offset(size.width * 0.35f, size.height * 0.62f), Offset(size.width * 0.65f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.58f), Offset(size.width * 0.5f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Charisma, StatIconKind.Luck -> {
                drawCircle(color, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.5f, size.height * 0.42f), style = Stroke(stroke.width))
                drawLine(color, Offset(size.width * 0.26f, size.height * 0.76f), Offset(size.width * 0.74f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Crit -> {
                drawCircle(color, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(stroke.width))
                drawCircle(color, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            }
            StatIconKind.Poison -> drawCircle(color, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(stroke.width))
            StatIconKind.Burn -> {
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.18f), Offset(size.width * 0.32f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.18f), Offset(size.width * 0.68f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Freeze -> {
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.16f), Offset(size.width * 0.5f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.2f, size.height * 0.34f), Offset(size.width * 0.8f, size.height * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.8f, size.height * 0.34f), Offset(size.width * 0.2f, size.height * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Stun -> {
                drawLine(color, Offset(size.width * 0.42f, size.height * 0.16f), Offset(size.width * 0.62f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.62f, size.height * 0.5f), Offset(size.width * 0.38f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Blind -> {
                drawCircle(color, radius = size.minDimension * 0.24f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(stroke.width))
                drawLine(color, Offset(size.width * 0.2f, size.height * 0.8f), Offset(size.width * 0.8f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            StatIconKind.Clear -> {
                drawLine(color, Offset(size.width * 0.2f, size.height * 0.52f), Offset(size.width * 0.42f, size.height * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.42f, size.height * 0.74f), Offset(size.width * 0.82f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun InlineBar(label: String, current: Int, max: Int, color: Color) {
    val progress = if (max > 0) current.toFloat() / max.toFloat() else 0f
    Box(
        modifier = Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(9.dp)).background(color.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxSize().clip(RoundedCornerShape(8.dp)).background(color))
        Text(
            "$label  $current/$max",
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun CompactStat(label: String, value: Int, statKey: String, viewModel: AuthViewModel, availablePoints: Int, modifier: Modifier = Modifier, isPercent: Boolean = false) {
    val hasPoints = availablePoints > 0
    Column(modifier = modifier.padding(horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$value${if (isPercent) "%" else ""}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (hasPoints) {
                Surface(modifier = Modifier.size(18.dp).clickable { viewModel.updateCharacterStat(statKey, 1) }, shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Subir", tint = Color.White, modifier = Modifier.size(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CompactStateStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 8.sp, color = Color.Gray)
        Text("$value", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun MiniCoin(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 8.sp, color = Color.Gray)
    }
}

private fun EquipmentItem.rarityName(): String {
    return when (rarity) {
        2 -> "uncommon"
        3 -> "rare"
        4 -> "epic"
        5 -> "legendary"
        else -> "common"
    }
}

private fun accessorySlotUi(primary: EquipmentItem?, secondary: EquipmentItem?): EquipmentSlotUi {
    val item = primary ?: secondary
    return EquipmentSlotUi(item?.name, item?.rarityName() ?: "common")
}
