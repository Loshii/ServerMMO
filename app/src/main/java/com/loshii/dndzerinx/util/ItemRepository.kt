package com.loshii.dndzerinx.util

import android.content.Context
import com.loshii.dndzerinx.model.InventoryItem
import com.loshii.dndzerinx.model.EquipmentItem

object ItemRepository {
    fun loadSampleInventory(context: Context): List<InventoryItem> {
        return JsonSupport.fromAsset(context, "items.json") ?: emptyList()
    }

    fun loadSampleEquipment(context: Context): List<EquipmentItem> {
        return JsonSupport.fromAsset(context, "equipment.json") ?: emptyList()
    }
}
