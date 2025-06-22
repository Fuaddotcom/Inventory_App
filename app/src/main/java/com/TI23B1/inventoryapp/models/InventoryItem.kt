// com.TI23B1.inventoryapp.models/InventoryItem.kt
package com.TI23B1.inventoryapp.models

import com.google.firebase.database.DataSnapshot
import kotlin.collections.HashMap

data class InventoryItem(
    val id: String = "",
    val nama_barang: String = "",
    val image_url: String? = null // <--- NEW FIELD
) {
    constructor() : this("", "", null)

    fun toMap(): Map<String, Any?> { // Use Any? because imageUrl can be null
        return hashMapOf(
            "id" to id,
            "nama_barang" to nama_barang,
            "image_url" to image_url
        )
    }

    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): InventoryItem? {
            val map = snapshot.value as? HashMap<*, *> ?: return null
            return try {
                InventoryItem(
                    id = (map["id"] as? String) ?: "",
                    nama_barang = (map["nama_barang"] as? String) ?: "",
                    image_url = (map["image_url"] as? String)
                )
            } catch (e: Exception) {
                // Log the exception to understand why it failed
                null
            }
        }
    }
}