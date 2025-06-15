package com.TI23B1.inventoryapp.models

import com.TI23B1.inventoryapp.CargoIn
import com.google.firebase.database.DataSnapshot
import java.util.HashMap

data class RecentItem(
    val id: Long = 0,
    val name: String = "",
    val stock: Int = 0,
    val type: String = "", // "IN" or "OUT" to distinguish cargo in/out
    val unit: String = "",
    val cargoId: String = ""
) {
    constructor() : this(0, "", 0, "", "", "")

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "stock" to stock,
            "type" to type,
            "unit" to unit,
            "cargoId" to cargoId
        )
    }

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): RecentItem? {
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                RecentItem(
                    id = (map["id"] as? Long) ?: 0,
                    name = map["name"] as? String ?: "",
                    stock = (map["stock"] as? Long)?.toInt() ?: 0,
                    type = map["type"] as? String ?: "",
                    unit = map["unit"] as? String ?: "",
                    cargoId = map["cargoId"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }

        // Helper function to convert CargoIn to RecentItem
        fun fromCargoIn(cargoIn: CargoIn): RecentItem {
            return RecentItem(
                id = cargoIn.cargoId.hashCode().toLong(),
                name = cargoIn.namaBarang,
                stock = cargoIn.quantity,
                type = "IN",
                unit = cargoIn.unit,
                cargoId = cargoIn.cargoId
            )
        }
    }
}