package com.loshii.dndzerinx.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.auth.FirebaseAuth
import com.loshii.dndzerinx.data.LocalProfileManager
import com.loshii.dndzerinx.util.CoilGifImage
import com.loshii.dndzerinx.util.ImageUploader
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import com.loshii.dndzerinx.viewmodel.ImageType
import kotlinx.coroutines.launch

enum class ProfileTab { STATS, EQUIPO, INVENTARIO, HABILIDADES }

@Composable
fun LocalProfileScreen(
    onBack: () -> Unit,
    onOpenCharacter: () -> Unit = {},
    onSettings: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val profileManager = remember { LocalProfileManager(context) }
    val profile by profileManager.profile.collectAsState(initial = null)
    val user by viewModel.currentUser.collectAsState()
    val scope = androidx.lifecycle.viewmodel.compose.viewModel<com.loshii.dndzerinx.viewmodel.LocalProfileViewModel>().scope

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ProfileTab.STATS) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.uploadImage(ImageType.AVATAR, context, it) { url ->
                url?.let { avatarUrl ->
                    scope.launch { profileManager.updateAvatarUrl(avatarUrl) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text("Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Edit, contentDescription = "Ajustes", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable { avatarPicker.launch("image/*") }
            ) {
                val avatarUrl = profile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    CoilGifImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                            .border(3.dp, Color(0xFF2196F3), CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2196F3), CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile?.displayName ?: "J").take(1).uppercase(),
                            color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile?.displayName ?: "Jugador",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    newName = profile?.displayName ?: ""
                    showEditNameDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                }
            }

            Text(
                text = "${profile?.race ?: "Humano"} - ${profile?.characterClass ?: "Guerrero"}",
                color = Color(0xFF888888), fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge("Nivel", "${profile?.level ?: 1}", Color(0xFFFFC107))
                StatBadge("Oro", "${profile?.gold ?: 0}", Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                ProfileTab.STATS -> StatsTab(profile)
                ProfileTab.EQUIPO -> EquipoTab()
                ProfileTab.INVENTARIO -> InventarioTab()
                ProfileTab.HABILIDADES -> HabilidadesTab()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenCharacter,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Ver inventario completo", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Cerrar sesión", fontSize = 16.sp)
            }
        }
    }

    if (showEditNameDialog) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Card(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Editar nombre", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditNameDialog = false }) {
                            Text("Cancelar", color = Color(0xFF888888))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    scope.launch { profileManager.updateName(newName.trim()) }
                                }
                                showEditNameDialog = false
                            }
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF888888), fontSize = 12.sp)
    }
}

@Composable
private fun TabRow(selectedTab: ProfileTab, onTabSelected: (ProfileTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ProfileTab.entries.forEach { tab ->
            Text(
                text = tab.name,
                color = if (tab == selectedTab) Color.White else Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun StatsTab(profile: com.loshii.dndzerinx.data.LocalProfile?) {
    Column {
        StatBar("HP", profile?.hp ?: 100, profile?.maxHp ?: 100, Color(0xFFF44336))
        Spacer(modifier = Modifier.height(8.dp))
        StatRow("Ataque", "${profile?.attackPower ?: 10}")
        StatRow("Defensa", "${profile?.defense ?: 5}")
        StatRow("XP", "${profile?.xp ?: 0} / ${(profile?.level ?: 1) * 100}")
    }
}

@Composable
private fun StatBar(label: String, current: Int, max: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("$current / $max", color = Color(0xFF888888), fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(current.toFloat() / max.toFloat().coerceAtLeast(1f))
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF888888), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EquipoTab() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Equipo del personaje", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Arma: Espada de hierro", color = Color(0xFF888888), fontSize = 14.sp)
        Text("Armadura: Cota de malla", color = Color(0xFF888888), fontSize = 14.sp)
        Text("Escudo: Escudo de madera", color = Color(0xFF888888), fontSize = 14.sp)
        Text("Accesorio: Anillo de poder", color = Color(0xFF888888), fontSize = 14.sp)
    }
}

@Composable
private fun InventarioTab() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Inventario vacío", color = Color(0xFF888888), fontSize = 16.sp)
        Text("Derrota monstruos para obtener objetos", color = Color(0xFF666666), fontSize = 12.sp)
    }
}

@Composable
private fun HabilidadesTab() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Habilidades", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(16.dp))
        SkillItem("Golpe fuerte", "Ataque básico mejorado")
        SkillItem("Defensa férrea", "+5 defensa temporal")
    }
}

@Composable
private fun SkillItem(name: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(desc, color = Color(0xFF888888), fontSize = 12.sp)
        }
    }
}
