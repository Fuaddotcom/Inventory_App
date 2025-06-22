package com.TI23B1.inventoryapp.models

import com.google.firebase.database.DataSnapshot
import kotlin.collections.HashMap

data class InventoryStock(
    val id: String = "",
    val name: String = "",
    var unit: String = "",
    var stokTersedia: Int = 0,
    var totalKeluar: Int = 0,
    var totalMasuk: Int = 0
) {
    constructor() : this("", "", "", 0, 0, 0)

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "stokTersedia" to stokTersedia,
            "unit" to unit,
            "totalKeluar" to totalKeluar,
            "totalMasuk" to totalMasuk
        )
    }

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): InventoryStock? {
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                InventoryStock(
                    id = map["id"] as? String ?: "",
                    name = map["name"] as? String ?: "",
                    unit = map["unit"] as? String ?: "",
                    stokTersedia = (map["stokTersedia"] as? Long)?.toInt() ?: 0,
                    totalKeluar = (map["totalKeluar"] as? Long)?.toInt() ?: 0,
                    totalMasuk = (map["totalMasuk"] as? Long)?.toInt() ?: 0,
                )
            } catch (e: Exception) {
                // Log the exception to understand why it failed
                null
            }
        }
    }
}
