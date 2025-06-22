package com.TI23B1.inventoryapp.models

import com.google.firebase.database.DataSnapshot
import java.util.HashMap
import kotlin.collections.get

data class CargoIn(
    var cargoId: String = "",
    val tanggalMasuk: String = "",
    val namaBarang: String = "",
    val namaSupplier: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val nomorPO: String = ""
) {
    constructor() : this("", "", "", "", 0, "", "")

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "cargoId" to cargoId,
            "tanggalMasuk" to tanggalMasuk,
            "namaBarang" to namaBarang,
            "namaSupplier" to namaSupplier,
            "quantity" to quantity,
            "unit" to unit,
            "nomorPO" to nomorPO
        )
    }

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): CargoIn? {
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                CargoIn(
                    cargoId = map["cargoId"] as? String ?: "",
                    tanggalMasuk = map["tanggalMasuk"] as? String ?: "",
                    namaBarang = map["namaBarang"] as? String ?: "",
                    namaSupplier = map["namaSupplier"] as? String ?: "",
                    quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                    unit = map["unit"] as? String ?: "",
                    nomorPO = map["nomorPO"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}