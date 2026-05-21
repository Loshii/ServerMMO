package com.loshii.dndzerinx.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.loshii.dndzerinx.model.game.Vector2
import com.loshii.dndzerinx.ui.components.VirtualJoystick
import com.loshii.dndzerinx.ui.icons.GameIcons

@Composable
fun GameHud(
    playerName: String,
    playerLevel: Int,
    playerHp: Int,
    playerMaxHp: Int,
    isConnected: Boolean,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit,
    onLaunchGodot: () -> Unit,
    onAttack: () -> Unit,
    onMoveDirection: (Vector2) -> Unit,
    onStopMoving: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xBB111111)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = playerName.ifBlank { "Aventurero" }, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Nivel $playerLevel", color = Color(0xFFB0B0B0), style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = "HP $playerHp / $playerMaxHp", color = Color(0xFFEEEEEE), style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(
                        progress = (playerHp.coerceAtLeast(0).coerceAtMost(playerMaxHp).toFloat() / playerMaxHp.coerceAtLeast(1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFF333333)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StatusPill(isConnected)
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniActionButton(icon = Icons.Default.Person, description = "Perfil", onClick = onOpenProfile)
            MiniActionButton(icon = Icons.Default.Settings, description = "Ajustes", onClick = onOpenSettings)
            MiniActionButton(icon = GameIcons.Logout, description = "Salir", onClick = onSignOut)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
                .size(170.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xBB111111)
        ) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                VirtualJoystick(onDirectionChanged = { direction ->
                    if (direction == Vector2(0f, 0f)) onStopMoving() else onMoveDirection(direction)
                })
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            GameActionButton(icon = GameIcons.Attack, label = "Atacar", onClick = onAttack)
            GameActionButton(icon = GameIcons.Godot, label = "Godot", onClick = onLaunchGodot)
        }
    }
}

@Composable
private fun MiniActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xDD222222), shape = CircleShape)
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}

@Composable
private fun GameActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(56.dp),
        shape = CircleShape,
        color = Color(0xFF1E88E5)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
    }
}

@Composable
private fun StatusPill(isConnected: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
    ) {
        Text(
            text = if (isConnected) "Conectado" else "Desconectado",
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
