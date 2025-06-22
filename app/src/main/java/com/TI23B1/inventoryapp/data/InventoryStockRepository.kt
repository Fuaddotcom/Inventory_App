package com.TI23B1.inventoryapp.data

import android.util.Log
import com.TI23B1.inventoryapp.models.InventoryStock
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InventoryStockRepository {

    private val database = FirebaseDatabase.getInstance()
    private val stockRef = database.getReference("stok_barang")

    fun getAvailableItemNames(callback: (List<String>) -> Unit) {
        stockRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemNames = mutableListOf<String>()
                for (stockSnapshot in snapshot.children) {
                    val itemName = stockSnapshot.child("name").getValue(String::class.java)
                    itemName?.let { itemNames.add(it) }
                }
                callback(itemNames.distinct().sorted())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("InventoryStockRepo", "Failed to load item names: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun updateStock(name: String, quantityChange: Int, isIncoming: Boolean, callback: (Boolean, String?) -> Unit) {
        Log.d("InventoryStockRepo", "updateStock called for item: $name, quantityChange: $quantityChange, isIncoming: $isIncoming")
        stockRef.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("InventoryStockRepo", "onDataChange for $name. Snapshot exists: ${snapshot.exists()}, Children count: ${snapshot.childrenCount}")
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val stockKey = childSnapshot.key
                            val currentStock = childSnapshot.getValue(InventoryStock::class.java)
                            Log.d("InventoryStockRepo", "Found existing stock item. Key: $stockKey, Current Stock: $currentStock")

                            if (stockKey != null && currentStock != null) {
                                val newStokTersedia = if (isIncoming) {
                                    currentStock.stokTersedia + quantityChange
                                } else {
                                    currentStock.stokTersedia - quantityChange
                                }

                                Log.d("InventoryStockRepo", "Calculated newStokTersedia: $newStokTersedia (was ${currentStock.stokTersedia})")

                                if (!isIncoming && newStokTersedia < 0) {
                                    Log.w("InventoryStockRepo", "Aborting outgoing transaction: Insufficient stock ($newStokTersedia < 0)")
                                    callback(false, "Stok tidak mencukupi untuk barang keluar.")
                                    return
                                }

                                val newTotalMasuk = if (isIncoming) currentStock.totalMasuk + quantityChange else currentStock.totalMasuk
                                val newTotalKeluar = if (!isIncoming) currentStock.totalKeluar + quantityChange else currentStock.totalKeluar

                                val updates = hashMapOf<String, Any>(
                                    "stokTersedia" to newStokTersedia,
                                    "totalMasuk" to newTotalMasuk,
                                    "totalKeluar" to newTotalKeluar
                                )
                                Log.d("InventoryStockRepo", "Attempting to update children at $stockKey with: $updates")
                                stockRef.child(stockKey).updateChildren(updates)
                                    .addOnSuccessListener {
                                        Log.d("InventoryStockRepo", "Stock update SUCCESS for $name.")
                                        callback(true, "Stok berhasil diperbarui.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("InventoryStockRepo", "Stock update FAILURE for $name: ${e.message}", e)
                                        callback(false, "Gagal memperbarui stok: ${e.message}")
                                    }
                                return // Only update the first match for simplicity
                            } else {
                                Log.e("InventoryStockRepo", "Stock key or currentStock object was null after snapshot.exists() was true. Key: $stockKey, currentStock: $currentStock")
                                callback(false, "Terjadi kesalahan internal saat membaca stok.")
                                return
                            }
                        }
                    } else {
                        // If item doesn't exist, create it for incoming, or error for outgoing
                        Log.d("InventoryStockRepo", "Item '$name' not found via query. Checking if incoming to create.")
                        if (isIncoming) {
                            val newStock = InventoryStock(
                                id = stockRef.push().key ?: "", // Generate a new ID
                                name = name,
                                stokTersedia = quantityChange,
                                totalMasuk = quantityChange,
                                totalKeluar = 0
                            )
                            Log.d("InventoryStockRepo", "Creating new stock entry for $name: $newStock")
                            stockRef.child(newStock.id).setValue(newStock)
                                .addOnSuccessListener {
                                    Log.d("InventoryStockRepo", "New item added and stock updated successfully for $name.")
                                    callback(true, "Item baru berhasil ditambahkan dan stok diperbarui.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("InventoryStockRepo", "Failed to add new item for $name: ${e.message}", e)
                                    callback(false, "Gagal menambahkan item baru: ${e.message}")
                                }
                        } else {
                            Log.w("InventoryStockRepo", "Outgoing transaction attempted for non-existent item: $name")
                            callback(false, "Barang tidak ditemukan dalam stok.")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryStockRepo", "Database error during updateStock for $name: ${error.message}", error.toException())
                    callback(false, "Database error: ${error.message}")
                }
            })
    }

    fun getInventoryStock(itemName: String, callback: (InventoryStock?) -> Unit) {
        stockRef.orderByChild("name").equalTo(itemName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val stockItem = childSnapshot.getValue(InventoryStock::class.java)
                            callback(stockItem)
                            return // Return the first matching item
                        }
                    }
                    callback(null) // Item not found
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryStockRepo", "Failed to get inventory stock for $itemName: ${error.message}")
                    callback(null)
                }
            })
    }


    /**
     * Retrieves all InventoryStock items in real-time.
     * Returns the ValueEventListener so it can be removed later.
     */
    fun getAllStockItemsRealtime(callback: (List<InventoryStock>) -> Unit): ValueEventListener {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stockList = mutableListOf<InventoryStock>()
                for (stockSnapshot in snapshot.children) {
                    val stockItem = stockSnapshot.getValue(InventoryStock::class.java)
                    stockItem?.let { stockList.add(it) }
                }
                callback(stockList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InventoryStockRepo", "Failed to load inventory stock items: ${error.message}")
                callback(emptyList()) // Provide an empty list on error
            }
        }
        stockRef.addValueEventListener(valueEventListener)
        return valueEventListener // Return the listener to be removed later
    }

    /**
     * Removes the real-time listener for stock items.
     */
    fun removeStockRealtimeListener(listener: ValueEventListener) {
        stockRef.removeEventListener(listener)
    }
    // --- END NEW METHODS ---
}
