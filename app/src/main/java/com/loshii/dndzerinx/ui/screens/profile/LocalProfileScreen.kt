package com.loshii.dndzerinx.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.data.LocalProfileManager
import com.loshii.dndzerinx.util.CoilGifImage
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import com.loshii.dndzerinx.viewmodel.ImageType
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.uploadImage(ImageType.AVATAR, context, it) { url ->
                url?.let { avatarUrl ->
                    scope.launch { profileManager.updateAvatarUrl(avatarUrl) }
                }
            }
        }
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.uploadImage(ImageType.BANNER, context, it) { url ->
                url?.let { bannerUrl ->
                    scope.launch { profileManager.updateAvatarUrl(bannerUrl) }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val avatarUrl = profile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl
            val bannerUrl = user?.bannerUrl?.takeIf { it.isNotBlank() } ?: avatarUrl

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(3.dp, Color.White), RoundedCornerShape(8.dp))
            ) {
                if (!bannerUrl.isNullOrBlank()) {
                    CoilGifImage(
                        model = bannerUrl,
                        contentDescription = "Banner",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFF1E88E5), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D&D", color = Color.White.copy(alpha = 0.3f), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    IconButton(onClick = { bannerPicker.launch("image/*") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Cambiar banner", tint = Color.White)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                val avatarSz = 96.dp
                Box(
                    modifier = Modifier
                        .offset(y = (-avatarSz / 2 - 12.dp))
                        .size(avatarSz)
                        .clip(CircleShape)
                        .border(BorderStroke(3.dp, Color.White), CircleShape)
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        CoilGifImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF2196F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.displayName ?: "J").take(1).uppercase(),
                                color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp),
                        shape = CircleShape,
                        color = Color(0xFF1E88E5)
                    ) {
                        IconButton(onClick = { avatarPicker.launch("image/*") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Cambiar avatar", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = profile?.displayName ?: "Jugador",
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E88E5).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Lv ${profile?.level ?: 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = "${profile?.race ?: "Humano"} - ${profile?.characterClass ?: "Guerrero"}",
                    color = Color(0xFF888888), fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatsColumn("Nivel", "${profile?.level ?: 1}")
                    StatsColumn("Oro", "${profile?.gold ?: 0}")
                    StatsColumn("Ataque", "${profile?.attackPower ?: 10}")
                    StatsColumn("Defensa", "${profile?.defense ?: 5}")
                }

                Spacer(modifier = Modifier.height(20.dp))

                StatsBar("HP", profile?.hp ?: 100, profile?.maxHp ?: 100, Color(0xFFF44336))
                Spacer(modifier = Modifier.height(8.dp))
                StatsRow("XP", "${profile?.xp ?: 0} / ${(profile?.level ?: 1) * 100}")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onOpenCharacter,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Text("Inventario completo", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Cerrar sesión", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Editar nombre", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showEditNameDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Nombre") },
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
private fun StatsColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF888888), fontSize = 11.sp)
    }
}

@Composable
private fun StatsBar(label: String, current: Int, max: Int, color: Color) {
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
private fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF888888), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
