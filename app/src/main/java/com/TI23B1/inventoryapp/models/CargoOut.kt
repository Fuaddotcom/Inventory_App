package com.TI23B1.inventoryapp.models

import com.google.firebase.database.DataSnapshot
import java.util.HashMap
import kotlin.collections.get

data class CargoOut(
    var cargoId: String = "",
    val tanggalKeluar: String = "",
    val namaBarang: String = "",
    val tujuanPengiriman: String = "",
    val quantity: Int = 0,
    val unit: String = "",
    val nomorSuratJalan: String = ""
) {
    constructor() : this("", "", "", "", 0, "", "")

    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "cargoId" to cargoId,
            "tanggalKeluar" to tanggalKeluar,
            "namaBarang" to namaBarang,
            "tujuanPengiriman" to tujuanPengiriman,
            "quantity" to quantity,
            "unit" to unit,
            "nomorSuratJalan" to nomorSuratJalan
        )
    }

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): CargoOut? {
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                CargoOut(
                    cargoId = map["cargoId"] as? String ?: "",
                    tanggalKeluar = map["tanggalKeluar"] as? String ?: "",
                    namaBarang = map["namaBarang"] as? String ?: "",
                    tujuanPengiriman = map["tujuanPengiriman"] as? String ?: "",
                    quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                    unit = map["unit"] as? String ?: "",
                    nomorSuratJalan = map["nomorSuratJalan"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}