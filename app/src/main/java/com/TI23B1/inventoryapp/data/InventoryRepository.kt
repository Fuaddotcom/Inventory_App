package com.TI23B1.inventoryapp.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.TI23B1.inventoryapp.models.InventoryItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID



class InventoryRepository { // This is your existing InventoryRepository

    private val database = FirebaseDatabase.getInstance()
    private val iteminventoryRef = database.getReference("item_inventory")

    // Your existing InventoryFetchListener interface
    interface InventoryFetchListener {
        fun onInventoryItemFetched(itemKey: String, item: InventoryItem)
        fun onAllInventoryItemsFetched(items: Map<String, InventoryItem>)
        fun onError(errorMessage: String)
    }


    /**
     * Retrieves all inventory items in real-time.
     */
    fun getAllInventoryItemsRealtime(listener: InventoryFetchListener): ValueEventListener {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableMapOf<String, InventoryItem>()
                for (itemSnapshot in snapshot.children) {
                    val itemKey = itemSnapshot.key ?: continue
                    val inventoryItem = InventoryItem.fromSnapshot(itemSnapshot)
                    if (inventoryItem != null) {
                        items[itemKey] = inventoryItem
                    }
                }
                listener.onAllInventoryItemsFetched(items)
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError("Failed to load inventory items: ${error.message}")
            }
        }
        iteminventoryRef.addValueEventListener(valueEventListener)
        return valueEventListener
    }


    /**
     * Removes the real-time listener.
     */
    fun removeRealtimeListener(listener: ValueEventListener) {
        iteminventoryRef.removeEventListener(listener)
    }

    fun getAvailableItemNames(callback: (List<String>) -> Unit) {
        iteminventoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemNames = mutableListOf<String>()
                for (itemSnapshot in snapshot.children) {
                    val itemName = itemSnapshot.child("nama_barang").getValue(String::class.java)
                    itemName?.let { itemNames.add(it) }
                }
                callback(itemNames.distinct().sorted())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InventoryRepository", "Failed to load item names from item_inventory: ${error.message}")
                callback(emptyList())
            }
        })
    }


    /**
     * Retrieves a single InventoryItem by its nama_barang.
     * @param namaBarang The name of the item to fetch.
     * @param callback A lambda that will receive the InventoryItem (or null if not found/error).
     */
    // In InventoryRepository.kt, inside getInventoryItemByNamaBarang:
    fun getInventoryItemByNamaBarang(namaBarang: String, callback: (InventoryItem?) -> Unit) {
        Log.d("InventoryDebug", "Repository: Searching for InventoryItem with namaBarang: '$namaBarang'") // THIS LOG IS VITAL
        iteminventoryRef.orderByChild("nama_barang").equalTo(namaBarang) // Ensure "nama_barang" is correct here too
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val item = childSnapshot.getValue(InventoryItem::class.java)
                            if (item != null) {
                                Log.d("InventoryDebug", "Repository: Found matching InventoryItem: ID=${item.id}, namaBarang=${item.nama_barang}, imageUrl=${item.image_url}") // THIS LOG IS VITAL
                                callback(item)
                                return
                            }
                        }
                    }
                    Log.d("InventoryDebug", "Repository: No InventoryItem found for namaBarang: '$namaBarang' or item is null after parsing.") // THIS LOG IS VITAL
                    callback(null)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("InventoryDebug", "Repository: Error fetching item by namaBarang '$namaBarang': ${error.message}")
                    callback(null)
                }
            })
    }

}