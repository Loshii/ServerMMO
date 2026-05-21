package com.loshii.dndzerinx.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import com.loshii.dndzerinx.ui.icons.GameIcons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.data.LocalProfile
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.model.game.GameEngine
import com.loshii.dndzerinx.util.CoilGifImage

@Composable
fun GameHud(
    engine: GameEngine,
    profile: LocalProfile?,
    user: User?,
    levelUpMessage: String?,
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onChat: () -> Unit,
    onLibrary: () -> Unit,
    onSignOut: () -> Unit,
    onLaunchGodot: () -> Unit,
    onAttack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333333))
                        .clickable { onProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = profile?.avatarUrl?.takeIf { it.isNotBlank() } ?: user?.avatarUrl
                    if (!avatarUrl.isNullOrBlank()) {
                        CoilGifImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(36.dp),
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = profile?.displayName ?: user?.displayName ?: "Jugador",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nv. ${profile?.level ?: 1} ${profile?.characterClass ?: ""}",
                        color = Color(0xFF888888), fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onToggleMenu) {
                Surface(shape = CircleShape, color = Color(0x66000000)) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onAttack,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCFF4444))
            ) {
                Icon(GameIcons.Attack, contentDescription = "Atacar", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xEE1A1A1A),
                modifier = Modifier.width(160.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    GameMenuItem("Perfil", onClick = onProfile)
                    GameMenuItem("Equipo", onClick = onSettings)
                    GameMenuItem("Chat", onClick = onChat)
                    GameMenuItem("Biblioteca", onClick = onLibrary)
                    GameMenuItem("Godot", onClick = onLaunchGodot)
                    Spacer(Modifier.height(4.dp))
                    GameMenuItem("Salir", onClick = onSignOut)
                }
            }
        }
    }
}

@Composable
private fun GameMenuItem(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Text(
            text = text,
            color = Color(0xFFCCCCCC),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
