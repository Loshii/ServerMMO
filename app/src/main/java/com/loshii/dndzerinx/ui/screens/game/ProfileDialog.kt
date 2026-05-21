package com.loshii.dndzerinx.ui.screens.game

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.loshii.dndzerinx.data.LocalProfile
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.util.CoilGifImage

@Composable
fun ProfileDialog(
    profile: LocalProfile?,
    user: User?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxSize(0.82f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val avatarUrl = profile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl
                val bannerUrl = user?.bannerUrl?.takeIf { it.isNotBlank() } ?: avatarUrl

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    if (!bannerUrl.isNullOrBlank()) {
                        CoilGifImage(
                            model = bannerUrl,
                            contentDescription = "Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1E88E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("D&D", color = Color.White.copy(alpha = 0.2f), fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.4f)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    val avatarSz = 80.dp
                    Box(
                        modifier = Modifier
                            .offset(y = (-avatarSz / 2 - 8.dp))
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
                                    color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-20).dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profile?.displayName ?: user?.displayName ?: "Jugador",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E88E5).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Lv ${profile?.level ?: 1}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = "${profile?.race ?: "Humano"} - ${profile?.characterClass ?: "Guerrero"}",
                        color = Color(0xFF888888), fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge("Nivel", "${profile?.level ?: 1}")
                        StatBadge("Oro", "${profile?.gold ?: 0}")
                        StatBadge("Ataque", "${profile?.attackPower ?: 10}")
                        StatBadge("Defensa", "${profile?.defense ?: 5}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).fillMaxWidth()
                    ) {
                        DialogStatBar("HP", profile?.hp ?: 100, profile?.maxHp ?: 100, Color(0xFFF44336))
                        Spacer(modifier = Modifier.height(6.dp))
                        DialogStatRow("XP", "${profile?.xp ?: 0} / ${(profile?.level ?: 1) * 100}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar perfil", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF888888), fontSize = 10.sp)
    }
}

@Composable
private fun DialogStatBar(label: String, current: Int, max: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color(0xFFBBBBBB), fontSize = 13.sp)
            Text("$current / $max", color = Color(0xFF888888), fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp)
                .background(Color(0xFF333333), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(current.toFloat() / max.toFloat().coerceAtLeast(1f)).height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun DialogStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFBBBBBB), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
