package com.loshii.dndzerinx.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loshii.dndzerinx.model.Equipment
import com.loshii.dndzerinx.model.EquipmentItem
import com.loshii.dndzerinx.model.InventoryItem
import com.loshii.dndzerinx.model.Skill
import com.loshii.dndzerinx.model.SkillType
import com.loshii.dndzerinx.model.effectiveCharacterStatus
import com.loshii.dndzerinx.model.totalStats
import com.loshii.dndzerinx.ui.components.EquipmentPaperDoll
import com.loshii.dndzerinx.ui.components.EquipmentSlotUi
import com.loshii.dndzerinx.viewmodel.AuthViewModel

enum class CharacterTab {
    EQUIPO, INVENTARIO, HABILIDADES
}

@Composable
fun CharacterScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val u = user ?: return

    var selectedTab by remember { mutableStateOf(CharacterTab.EQUIPO) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Personaje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton("Equipo", selectedTab == CharacterTab.EQUIPO, Modifier.weight(1f)) { selectedTab = CharacterTab.EQUIPO }
            TabButton("Inventario", selectedTab == CharacterTab.INVENTARIO, Modifier.weight(1f)) { selectedTab = CharacterTab.INVENTARIO }
            TabButton("Habilidades", selectedTab == CharacterTab.HABILIDADES, Modifier.weight(1f)) { selectedTab = CharacterTab.HABILIDADES }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                CharacterTab.EQUIPO -> EquipmentTab(u, viewModel)
                CharacterTab.INVENTARIO -> InventoryTab(u, viewModel)
                CharacterTab.HABILIDADES -> SkillsTab(u)
            }
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EquipmentTab(u: com.loshii.dndzerinx.model.User, viewModel: AuthViewModel) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Equipamiento", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                EquipmentPaperDoll(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC9C9C9), RoundedCornerShape(10.dp))
                        .padding(vertical = 6.dp),
                    slots = mapOf(
                        "head" to EquipmentSlotUi(u.equipment.head?.name, u.equipment.head?.rarityName() ?: "common"),
                        "chest" to EquipmentSlotUi(u.equipment.chest?.name, u.equipment.chest?.rarityName() ?: "common"),
                        "legs" to EquipmentSlotUi(u.equipment.legs?.name, u.equipment.legs?.rarityName() ?: "common"),
                        "feet" to EquipmentSlotUi(u.equipment.feet?.name, u.equipment.feet?.rarityName() ?: "common"),
                        "accessoryLeft" to accessorySlotUi(u.equipment.collar, u.equipment.earringLeft),
                        "accessoryRight" to accessorySlotUi(u.equipment.cape, u.equipment.earringRight),
                        "weaponLeft" to EquipmentSlotUi(u.equipment.weaponLeft?.name, u.equipment.weaponLeft?.rarityName() ?: "common"),
                        "weaponRight" to EquipmentSlotUi(u.equipment.weaponRight?.name, u.equipment.weaponRight?.rarityName() ?: "common"),
                        "gloveLeft" to EquipmentSlotUi(u.equipment.gloveLeft?.name, u.equipment.gloveLeft?.rarityName() ?: "common"),
                        "gloveRight" to EquipmentSlotUi(u.equipment.gloveRight?.name, u.equipment.gloveRight?.rarityName() ?: "common")
                    ),
                    ringNames = u.equipment.rings
                )
                Spacer(modifier = Modifier.height(10.dp))
                EquipmentSlotSummary(u.equipment, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Estadísticas de Equipo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val effectiveStatus = u.effectiveCharacterStatus()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox("ATQ", "${effectiveStatus.attackPower}")
                    StatBox("MAG", "${effectiveStatus.magicPower}")
                    StatBox("DEF", "${effectiveStatus.defense}")
                    StatBox("VEL", "${effectiveStatus.speed}")
                }
                val itemStats = u.equipment.totalStats()
                if (itemStats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Bonos de piezas", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    StatChipRow(itemStats)
                }
            }
        }
    }
}

@Composable
private fun InventoryTab(u: com.loshii.dndzerinx.model.User, viewModel: AuthViewModel) {
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Inventario", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${u.inventory.items.size}/${u.inventory.maxSlots}", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                InventoryGrid(u.inventory.items, u.inventory.maxSlots) { item ->
                    selectedItem = item
                }
                Spacer(modifier = Modifier.height(14.dp))
                InventoryActionList(u.inventory.items, viewModel) { item ->
                    selectedItem = item
                }
            }
        }
    }

    selectedItem?.let { item ->
        ItemDetailDialog(item, viewModel) { selectedItem = null }
    }
}

@Composable
private fun ItemDetailDialog(item: InventoryItem, viewModel: AuthViewModel, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(getRarityColorInt(item.rarity).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.name.take(1).uppercase(), fontSize = 16.sp, color = getRarityColorInt(item.rarity), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontWeight = FontWeight.Bold, color = getRarityColorInt(item.rarity))
                    Text(item.category, fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        text = {
            Column {
                if (item.description.isNotBlank()) {
                    Text(item.description, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Cantidad: ${item.quantity}", fontSize = 12.sp)
                if (item.stats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estadísticas:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    item.stats.entries.forEach { entry ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.key.replaceFirstChar { it.uppercase() }, fontSize = 11.sp)
                            Text("+${entry.value}", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.isConsumable) {
                    Button(onClick = {
                        viewModel.useInventoryItem(item.id)
                        onDismiss()
                    }) {
                        Text("Usar")
                    }
                }
                if (item.isEquippable) {
                    Button(onClick = {
                        viewModel.equipInventoryItem(item.id)
                        onDismiss()
                    }) {
                        Text("Equipar")
                    }
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun InventoryActionList(items: List<InventoryItem>, viewModel: AuthViewModel, onItemTap: (InventoryItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Acciones de inventario", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text("No tienes items en el inventario.", color = Color.Gray, fontSize = 12.sp)
        } else {
            items.forEach { item ->
                InventoryActionRow(item, viewModel, onItemTap)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InventoryActionRow(item: InventoryItem, viewModel: AuthViewModel, onItemTap: (InventoryItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onItemTap(item) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = getRarityColorInt(item.rarity))
                    Text("${item.quantity} x ${item.category}", fontSize = 11.sp, color = Color.Gray)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.isConsumable) {
                        Button(onClick = { viewModel.useInventoryItem(item.id) }) {
                            Text("Usar")
                        }
                    }
                    if (item.isEquippable) {
                        Button(onClick = { viewModel.equipInventoryItem(item.id) }) {
                            Text("Equipar")
                        }
                    }
                }
            }
            if (item.stats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.stats.entries.take(3).forEach { entry ->
                        Text("${entry.key.replaceFirstChar { it.uppercase() }} +${entry.value}", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(item.description, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun InventoryGrid(items: List<InventoryItem>, maxSlots: Int, onItemTap: (InventoryItem) -> Unit) {
    val slotCount = maxSlots.coerceAtLeast(20)
    val rows = (0 until slotCount).chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { slotIndex ->
                    val item = items.getOrNull(slotIndex)
                    if (item == null) {
                        EmptySlot(slotIndex + 1, Modifier.weight(1f))
                    } else {
                        InventorySlot(item, slotIndex + 1, Modifier.weight(1f), onItemTap)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventorySlot(item: InventoryItem, slotNumber: Int, modifier: Modifier = Modifier, onItemTap: (InventoryItem) -> Unit) {
    val rarityColor = getRarityColorInt(item.rarity)
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.5.dp, rarityColor.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .clickable { onItemTap(item) },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box {
            Text(
                "#$slotNumber",
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                fontSize = 7.sp,
                color = Color.Gray
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(rarityColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.name.take(1).uppercase(), fontSize = 11.sp, color = rarityColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(item.name, fontSize = 9.sp, color = rarityColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("x${item.quantity}", fontSize = 8.sp, color = Color.Gray)
                    if (item.category.isNotBlank()) {
                        Text(item.category.take(3).uppercase(), fontSize = 7.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySlot(slotNumber: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("#$slotNumber", fontSize = 9.sp, color = Color.Gray.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun SkillsTab(u: com.loshii.dndzerinx.model.User) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Habilidades", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("MP ${u.characterStatus.mana}/${u.characterStatus.maxMana}", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text("Pasiva", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                SkillSlot("PAS", u.skills.passive, SkillType.PASSIVE)

                Spacer(modifier = Modifier.height(16.dp))
                Text("Habilidades Activas", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkillSlot("S1", u.skills.skill1, SkillType.ACTIVE, Modifier.weight(1f))
                    SkillSlot("S2", u.skills.skill2, SkillType.ACTIVE, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkillSlot("S3", u.skills.skill3, SkillType.ACTIVE, Modifier.weight(1f))
                    SkillSlot("ULT", u.skills.skill4, SkillType.ULTIMATE, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Las habilidades muestran coste, recarga, potencia y duracion para que el set sea facil de comparar.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SkillSlot(slotLabel: String, skill: Skill?, expectedType: SkillType, modifier: Modifier = Modifier) {
    val hasSkill = skill != null && skill.name.isNotBlank()
    val accent = when (expectedType) {
        SkillType.PASSIVE -> Color(0xFF4CAF50)
        SkillType.ACTIVE -> MaterialTheme.colorScheme.primary
        SkillType.ULTIMATE -> Color(0xFFE65100)
    }
    Surface(
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = if (hasSkill) accent.copy(alpha = 0.13f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(slotLabel, fontSize = 10.sp, color = accent, fontWeight = FontWeight.Black)
                Text(if (hasSkill && skill?.isUnlocked == false) "LOCK" else "Nv ${skill?.level ?: 0}", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(
                    if (hasSkill) accent.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasSkill) skill?.name?.take(2)?.uppercase() ?: "?" else "?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasSkill) accent else Color.Gray
                )
            }
            Text(
                    text = if (hasSkill) skill?.name ?: "Vacío" else "Vacío",
                    fontSize = 11.sp,
                    color = if (hasSkill) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            if (hasSkill) {
                Text(skill?.description.orEmpty().ifBlank { "Sin descripcion" }, fontSize = 9.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                SkillMetaRow(skill!!)
            } else {
                Text("Slot disponible", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun SkillMetaRow(skill: Skill) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        SkillMeta("MP", skill.manaCost.takeIf { it > 0 }?.toString() ?: "-")
        SkillMeta("CD", skill.cooldown.takeIf { it > 0 }?.let { "${it}s" } ?: "-")
        val power = when {
            skill.damage > 0 -> skill.damage
            skill.healAmount > 0 -> skill.healAmount
            else -> 0
        }
        SkillMeta(if (skill.healAmount > 0) "CUR" else "POD", power.takeIf { it > 0 }?.toString() ?: "-")
        SkillMeta("DUR", skill.duration.takeIf { it > 0 }?.let { "${it}t" } ?: "-")
    }
}

@Composable
private fun SkillMeta(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EquipmentSlotSummary(equipment: Equipment, viewModel: AuthViewModel) {
    val slots = listOf(
        "Cabeza" to equipment.head,
        "Pecho" to equipment.chest,
        "Piernas" to equipment.legs,
        "Pies" to equipment.feet,
        "Collar" to equipment.collar,
        "Capa" to equipment.cape,
        "Pendiente I" to equipment.earringLeft,
        "Pendiente II" to equipment.earringRight,
        "Anillo I" to equipment.ringSlot.getOrNull(0),
        "Anillo II" to equipment.ringSlot.getOrNull(1),
        "Arma I" to equipment.weaponLeft,
        "Arma II" to equipment.weaponRight,
        "Guante I" to equipment.gloveLeft,
        "Guante II" to equipment.gloveRight
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slots.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (label, item) ->
                    EquipmentLine(label, item, Modifier.weight(1f)) { eqItem ->
                        // call unequip
                        viewModel.unequipEquipment(eqItem.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentLine(label: String, item: EquipmentItem?, modifier: Modifier = Modifier, onUnequip: (EquipmentItem) -> Unit = {}) {
    val rarityColor = getRarityColorInt(item?.rarity ?: 1)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                .clickable(enabled = item != null) {
                    item?.let { onUnequip(it) }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(rarityColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item?.name?.take(1)?.uppercase() ?: "-", color = rarityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(item?.name?.takeIf { it.isNotBlank() } ?: "Vacio", color = if (item == null) Color.Gray else rarityColor, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun StatChipRow(stats: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        stats.entries.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (key, value) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "${key.uppercase()} +$value",
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun Equipment.totalStats(): Map<String, Int> {
    return listOfNotNull(
        head, chest, legs, feet, collar, earringLeft, earringRight, cape,
        weaponLeft, weaponRight, gloveLeft, gloveRight
    ).plus(ringSlot)
        .flatMap { it.stats.entries }
        .groupBy({ it.key }, { it.value })
        .mapValues { (_, values) -> values.sum() }
        .filterValues { it != 0 }
        .toList()
        .sortedByDescending { (_, value) -> value }
        .take(9)
        .toMap()
}

private fun getRarityColorInt(rarity: Int): Color {
    return when (rarity) {
        2 -> Color(0xFF1EFF00)
        3 -> Color(0xFF0070DD)
        4 -> Color(0xFFA335EE)
        5 -> Color(0xFFFF8000)
        else -> Color.Gray
    }
}

private fun com.loshii.dndzerinx.model.EquipmentItem.rarityName(): String {
    return when (rarity) {
        2 -> "uncommon"
        3 -> "rare"
        4 -> "epic"
        5 -> "legendary"
        else -> "common"
    }
}

private fun accessorySlotUi(primary: com.loshii.dndzerinx.model.EquipmentItem?, secondary: com.loshii.dndzerinx.model.EquipmentItem?): EquipmentSlotUi {
    val item = primary ?: secondary
    return EquipmentSlotUi(item?.name, item?.rarityName() ?: "common")
}
