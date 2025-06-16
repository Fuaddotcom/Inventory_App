package com.TI23B1.inventoryapp.models

import com.TI23B1.inventoryapp.CargoIn
import com.TI23B1.inventoryapp.CargoOut
import com.google.firebase.database.DataSnapshot
import java.text.SimpleDateFormat
import android.util.Log
import java.util.*
import kotlin.collections.HashMap

data class RecentItem(
    val id: Long = 0,
    val name: String = "",
    val stock: Int = 0,
    val type: String = "",
    val unit: String = "",
    val cargoId: String = "",
    val timestamp: Long = 0L
) {
    constructor() : this(0, "", 0, "", "", "", 0L)

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "stock" to stock,
            "type" to type,
            "unit" to unit,
            "cargoId" to cargoId,
            "timestamp" to timestamp
        )
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        fun fromSnapshot(snapshot: DataSnapshot): RecentItem? {
            // ... (your existing fromSnapshot)
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                val timestamp = (map["timestamp"] as? Long) ?: 0L
                val recentItem = RecentItem(
                    id = (map["id"] as? Long) ?: 0,
                    name = map["name"] as? String ?: "",
                    stock = (map["stock"] as? Long)?.toInt() ?: 0,
                    type = map["type"] as? String ?: "",
                    unit = map["unit"] as? String ?: "",
                    cargoId = map["cargoId"] as? String ?: "",
                    timestamp = timestamp
                )
                Log.d("RecentItem", "Parsed RecentItem from snapshot: $recentItem")
                recentItem
            } catch (e: Exception) {
                Log.e("RecentItem", "Error parsing RecentItem from snapshot: ${e.message}", e)
                null
            }
        }

        fun fromCargoIn(cargoIn: CargoIn): RecentItem {
            val timestamp = try {
                val date = dateFormat.parse(cargoIn.tanggalMasuk)
                if (date == null) {
                    Log.e("RecentItem", "Parsed date is null for tanggalMasuk: '${cargoIn.tanggalMasuk}'")
                    0L
                } else {
                    Log.d("RecentItem", "Successfully parsed tanggalMasuk '${cargoIn.tanggalMasuk}' to timestamp: ${date.time}")
                    date.time
                }
            } catch (e: Exception) {
                Log.e("RecentItem", "Error parsing tanggalMasuk '${cargoIn.tanggalMasuk}': ${e.message}", e)
                0L
            }

            return RecentItem(
                id = cargoIn.cargoId.hashCode().toLong(),
                name = cargoIn.namaBarang,
                stock = cargoIn.quantity,
                type = "IN",
                unit = cargoIn.unit,
                cargoId = cargoIn.cargoId,
                timestamp = timestamp
            )
        }

        fun fromCargoOut(cargoOut: CargoOut): RecentItem {
            val timestamp = try {
                val date = dateFormat.parse(cargoOut.tanggalKeluar)
                if (date == null) {
                    Log.e("RecentItem", "Parsed date is null for tanggalKeluar: '${cargoOut.tanggalKeluar}'")
                    0L
                } else {
                    Log.d("RecentItem", "Successfully parsed tanggalKeluar '${cargoOut.tanggalKeluar}' to timestamp: ${date.time}")
                    date.time
                }
            } catch (e: Exception) {
                Log.e("RecentItem", "Error parsing tanggalKeluar '${cargoOut.tanggalKeluar}': ${e.message}", e)
                0L
            }

            return RecentItem(
                id = cargoOut.cargoId.hashCode().toLong(),
                name = cargoOut.namaBarang,
                stock = Math.abs(cargoOut.quantity),
                type = "OUT",
                unit = cargoOut.unit,
                cargoId = cargoOut.cargoId,
                timestamp = timestamp
            )
        }
    }
}