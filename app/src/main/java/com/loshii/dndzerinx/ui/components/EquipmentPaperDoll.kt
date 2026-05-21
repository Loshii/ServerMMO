package com.loshii.dndzerinx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.loshii.dndzerinx.ui.theme.DnDRoleGateTheme

data class EquipmentSlotUi(
    val itemName: String? = null,
    val rarity: String = "common"
)

private data class DragState(
    val fromSlot: String,
    val pointer: Offset
)

private val CoreSlotSize = 86.dp
private val SideSlotSize = 64.dp
private val PanelHorizontalPadding = 4.dp
private val ColumnGap = 8.dp
private val SlotGap = 8.dp
private val RingSize = 14.dp

@Composable
fun EquipmentPaperDoll(
    modifier: Modifier = Modifier,
    slots: Map<String, EquipmentSlotUi> = emptyMap(),
    ringNames: List<String> = emptyList(),
    onItemDropped: (fromSlot: String, toSlot: String) -> Unit = { _, _ -> }
) {
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var tooltipData by remember { mutableStateOf<Pair<String, String>?>(null) }
    var dragState by remember { mutableStateOf<DragState?>(null) }
    val slotBounds = remember { mutableStateMapOf<String, Rect>() }

    fun moveSelection(toSlot: String) {
        val fromSlot = selectedSlot
        if (fromSlot == null) {
            selectedSlot = toSlot
        } else if (fromSlot == toSlot) {
            selectedSlot = null
        } else {
            onItemDropped(fromSlot, toSlot)
            selectedSlot = null
        }
    }

    fun finishDrag() {
        val drag = dragState ?: return
        val target = slotBounds.entries.firstOrNull { (id, bounds) ->
            id != drag.fromSlot && bounds.contains(drag.pointer)
        }?.key
        if (target != null) onItemDropped(drag.fromSlot, target)
        dragState = null
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PanelHorizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SideColumn(
                side = EquipmentSide.Left,
                slots = slots,
                rings = ringNames.take(4),
                selectedSlot = selectedSlot,
                onSlotTapped = ::moveSelection,
                onShowTooltip = { tooltipData = it },
                onHideTooltip = { tooltipData = null },
                onBoundsChanged = { id, rect -> slotBounds[id] = rect },
                onDragStart = { id, pointer -> dragState = DragState(id, pointer) },
                onDrag = { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } },
                onDragEnd = ::finishDrag
            )

            Spacer(modifier = Modifier.width(ColumnGap))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SlotGap)
            ) {
                PaperDollSlot("head", "Cabeza", slots["head"], CoreSlotSize, true, selectedSlot == "head", ::moveSelection, { tooltipData = it }, { tooltipData = null }, { slotBounds[it.first] = it.second }, { id, pointer -> dragState = DragState(id, pointer) }, { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } }, ::finishDrag)
                PaperDollSlot("chest", "Pecho", slots["chest"], CoreSlotSize, true, selectedSlot == "chest", ::moveSelection, { tooltipData = it }, { tooltipData = null }, { slotBounds[it.first] = it.second }, { id, pointer -> dragState = DragState(id, pointer) }, { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } }, ::finishDrag)
                PaperDollSlot("legs", "Piernas", slots["legs"], CoreSlotSize, true, selectedSlot == "legs", ::moveSelection, { tooltipData = it }, { tooltipData = null }, { slotBounds[it.first] = it.second }, { id, pointer -> dragState = DragState(id, pointer) }, { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } }, ::finishDrag)
                PaperDollSlot("feet", "Pies", slots["feet"], CoreSlotSize, true, selectedSlot == "feet", ::moveSelection, { tooltipData = it }, { tooltipData = null }, { slotBounds[it.first] = it.second }, { id, pointer -> dragState = DragState(id, pointer) }, { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } }, ::finishDrag)
            }

            Spacer(modifier = Modifier.width(ColumnGap))

            SideColumn(
                side = EquipmentSide.Right,
                slots = slots,
                rings = ringNames.drop(4).take(4),
                selectedSlot = selectedSlot,
                onSlotTapped = ::moveSelection,
                onShowTooltip = { tooltipData = it },
                onHideTooltip = { tooltipData = null },
                onBoundsChanged = { id, rect -> slotBounds[id] = rect },
                onDragStart = { id, pointer -> dragState = DragState(id, pointer) },
                onDrag = { delta -> dragState = dragState?.let { it.copy(pointer = it.pointer + delta) } },
                onDragEnd = ::finishDrag
            )
        }

        dragState?.let {
            Box(
                modifier = Modifier
                    .offset(x = it.pointer.x.dp, y = it.pointer.y.dp)
                    .size(24.dp)
                    .background(Color(0xFFB97A4A).copy(alpha = 0.35f), CircleShape)
                    .border(1.dp, Color(0xFF8B512D), CircleShape)
            )
        }

        tooltipData?.let { (title, desc) ->
            Popup(alignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF241911).copy(alpha = 0.94f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFB97A4A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(title, color = Color(0xFFF4EBD6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0xFFD7C3A2), fontSize = 11.sp)
                }
            }
        }
    }
}

private enum class EquipmentSide { Left, Right }

@Composable
private fun SideColumn(
    side: EquipmentSide,
    slots: Map<String, EquipmentSlotUi>,
    rings: List<String>,
    selectedSlot: String?,
    onSlotTapped: (String) -> Unit,
    onShowTooltip: (Pair<String, String>) -> Unit,
    onHideTooltip: () -> Unit,
    onBoundsChanged: (String, Rect) -> Unit,
    onDragStart: (String, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val prefix = if (side == EquipmentSide.Left) "left" else "right"
    val accessory = if (side == EquipmentSide.Left) "accessoryLeft" else "accessoryRight"
    val accessoryFallback = if (side == EquipmentSide.Left) {
        slots["collar"] ?: slots["earringLeft"]
    } else {
        slots["cape"] ?: slots["earringRight"]
    }
    val weapon = if (side == EquipmentSide.Left) "weaponLeft" else "weaponRight"
    val glove = if (side == EquipmentSide.Left) "gloveLeft" else "gloveRight"
    val labels = mapOf(
        accessory to "Accesorio",
        weapon to "Arma",
        glove to "Guantes"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SlotGap)
    ) {
        PaperDollSlot(accessory, labels.getValue(accessory), slots[accessory] ?: accessoryFallback, SideSlotSize, false, selectedSlot == accessory, onSlotTapped, onShowTooltip, onHideTooltip, { onBoundsChanged(it.first, it.second) }, onDragStart, onDrag, onDragEnd)
        PaperDollSlot(weapon, labels.getValue(weapon), slots[weapon], SideSlotSize, false, selectedSlot == weapon, onSlotTapped, onShowTooltip, onHideTooltip, { onBoundsChanged(it.first, it.second) }, onDragStart, onDrag, onDragEnd)

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (side == EquipmentSide.Left) RingGroup(prefix, rings)
            PaperDollSlot(glove, labels.getValue(glove), slots[glove], SideSlotSize, false, selectedSlot == glove, onSlotTapped, onShowTooltip, onHideTooltip, { onBoundsChanged(it.first, it.second) }, onDragStart, onDrag, onDragEnd)
            if (side == EquipmentSide.Right) RingGroup(prefix, rings)
        }
    }
}

@Composable
private fun RingGroup(side: String, rings: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(4) { index ->
            val filled = index < rings.size
            Box(
                modifier = Modifier
                    .size(RingSize)
                    .background(if (filled) Color(0xFFF4EBD6) else Color.Transparent, CircleShape)
                    .border(1.5.dp, if (filled) Color(0xFFC9A064) else Color(0xFFB97A4A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Text(
                        rings[index].take(1).uppercase(),
                        color = Color(0xFF6B3D21),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(width = 8.dp, height = 14.dp)
                .fantasyTab(if (side == "left") -1 else 1)
        )
    }
}

@Composable
private fun PaperDollSlot(
    id: String,
    label: String,
    slot: EquipmentSlotUi?,
    size: Dp,
    large: Boolean,
    selected: Boolean,
    onTapped: (String) -> Unit,
    onShowTooltip: (Pair<String, String>) -> Unit,
    onHideTooltip: () -> Unit,
    onBoundsChanged: (Pair<String, Rect>) -> Unit,
    onDragStart: (String, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val rarity = slot?.rarity ?: "common"
    val rarityColor = rarityColor(rarity)
    val content = slot?.itemName
    var rootBounds by remember { mutableStateOf<Rect?>(null) }

    Box(
        modifier = Modifier
            .size(size)
            .aspectRatio(1f)
            .onGloballyPositioned {
                rootBounds = it.boundsInRoot()
                onBoundsChanged(id to it.boundsInRoot())
            }
            .fantasySlot(selected = selected, rarityColor = rarityColor)
            .pointerInput(id) {
                detectTapGestures(
                    onTap = {
                        onHideTooltip()
                        onTapped(id)
                    },
                    onLongPress = {
                        onShowTooltip(
                            label to if (content.isNullOrBlank()) {
                                "Slot vacio - $id"
                            } else {
                                "$content - rareza $rarity"
                            }
                        )
                    }
                )
            }
            .pointerInput(id) {
                detectDragGestures(
                    onDragStart = { localPointer ->
                        onHideTooltip()
                        val rootPointer = rootBounds?.topLeft?.plus(localPointer) ?: localPointer
                        onDragStart(id, rootPointer)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(if (large) 12.dp else 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = content ?: label,
                color = if (content == null) Color(0xFF7C6245).copy(alpha = 0.78f) else rarityColor,
                fontSize = if (large) 13.sp else 10.sp,
                fontWeight = if (content == null) FontWeight.Medium else FontWeight.Bold,
                maxLines = if (large) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun Modifier.fantasySlot(selected: Boolean, rarityColor: Color): Modifier = this.drawBehind {
    val fill = Color(0xFFECEBD9)
    val border = if (selected) Color(0xFF6F8FEF) else Color(0xFFB97A4A)
    val stroke = 3.6.dp.toPx()
    val innerStroke = 1.1.dp.toPx()
    val radius = 24.dp.toPx()
    val notchW = 7.dp.toPx()
    val notchH = 18.dp.toPx()

    drawRoundRect(fill, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius))
    drawRoundRect(border, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius), style = Stroke(stroke))
    drawRoundRect(rarityColor.copy(alpha = 0.42f), topLeft = Offset(stroke * 1.5f, stroke * 1.5f), size = Size(size.width - stroke * 3f, size.height - stroke * 3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.78f, radius * 0.78f), style = Stroke(innerStroke))

    val centers = listOf(size.height * 0.2f, size.height * 0.5f, size.height * 0.8f)
    centers.forEach { y ->
        drawRoundRect(border, topLeft = Offset(-notchW * 0.35f, y - notchH / 2f), size = Size(notchW, notchH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()))
        drawRoundRect(border, topLeft = Offset(size.width - notchW * 0.65f, y - notchH / 2f), size = Size(notchW, notchH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()))
    }
}

private fun Modifier.fantasyTab(direction: Int): Modifier = this.drawBehind {
    val tabColor = Color(0xFFB97A4A)
    val x = if (direction < 0) size.width * 0.35f else 0f
    drawRoundRect(tabColor, topLeft = Offset(x, 0f), size = Size(size.width * 0.65f, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx(), 5.dp.toPx()))
}

private fun rarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "uncommon" -> Color(0xFF5FA06A)
        "rare" -> Color(0xFF4078D8)
        "epic" -> Color(0xFF8C57D6)
        "legendary" -> Color(0xFFE1A13B)
        else -> Color(0xFFB97A4A)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 660)
@Composable
private fun EquipmentPaperDollPreview() {
    DnDRoleGateTheme {
        Surface(color = Color(0xFFC9C9C9)) {
            EquipmentPaperDoll(
                modifier = Modifier.fillMaxSize(),
                slots = mapOf(
                    "head" to EquipmentSlotUi("Capucha", "rare"),
                    "chest" to EquipmentSlotUi("Armadura", "epic"),
                    "weaponLeft" to EquipmentSlotUi("Daga", "uncommon"),
                    "weaponRight" to EquipmentSlotUi("Mandoble", "legendary")
                ),
                ringNames = listOf("Sol", "Luna", "Eco")
            )
        }
    }
}
