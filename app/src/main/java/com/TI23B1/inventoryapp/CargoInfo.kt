package com.TI23B1.inventoryapp

import com.google.firebase.database.DataSnapshot
import java.util.HashMap

data class CargoInfo(
    val cargoId: String = "",
    val tanggalMasuk: String = "", // Changed to String
    val namaBarang: String = "",
    val namaSupplier: String = "",
    val status: String = "",
    val quantity: Int = 0,
    val unit: String = "",
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", 0, "")

    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "cargoId" to cargoId,
            "tanggalMasuk" to tanggalMasuk, // Already a String, no conversion needed
            "namaBarang" to namaBarang,
            "namaSupplier" to namaSupplier,
            "status" to status,
            "quantity" to quantity,
            "unit" to unit,
        )
    }

    companion object {
        // Create CargoInfo from Firebase DataSnapshot
        fun fromSnapshot(snapshot: DataSnapshot): CargoInfo? {
            val map = snapshot.value as? HashMap<*, *> ?: return null

            return try {
                CargoInfo(
                    cargoId = map["cargoId"] as? String ?: "",
                    tanggalMasuk = map["tanggalMasuk"] as? String ?: "",
                    namaBarang = map["namaBarang"] as? String ?: "",
                    namaSupplier = map["namaSupplier"] as? String ?: "",
                    status = map["status"] as? String ?: "",
                    quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                    unit = map["unit"] as? String ?: "",
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}