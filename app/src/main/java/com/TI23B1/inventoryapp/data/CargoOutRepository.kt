package com.TI23B1.inventoryapp.data // Your package

import com.TI23B1.inventoryapp.models.CargoOut
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log // Added for logging

class CargoOutRepository { // No parameters needed in constructor anymore

    private val database = FirebaseDatabase.getInstance()
    private val cargoOutRef = database.getReference("barang_keluar")

    // NEW: Get InventoryStockRepository internally
    private val inventoryStockRepository = InventoryStockRepository()

    fun addCargoOut(cargoOut: CargoOut, callback: (Boolean, String?) -> Unit) {
        val newRef = cargoOutRef.push()
        cargoOut.cargoId = newRef.key.toString() // Set the ID from Firebase push key
        newRef.setValue(cargoOut)
            .addOnSuccessListener {
                Log.d("CargoOutRepository", "Cargo Out record added successfully: ${cargoOut.namaBarang}")
                // Now, update stock immediately after saving CargoOut
                inventoryStockRepository.updateStock(
                    name = cargoOut.namaBarang,
                    quantityChange = cargoOut.quantity,
                    isIncoming = false // It's outgoing
                ) { updateSuccess, updateMessage ->
                    if (updateSuccess) {
                        Log.d("CargoOutRepository", "Stock updated successfully for Cargo Out: ${cargoOut.namaBarang}")
                        callback(true, null) // Report success back to caller
                    } else {
                        Log.e("CargoOutRepository", "Failed to update stock for Cargo Out: $updateMessage")
                        callback(false, "Failed to update stock: $updateMessage")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CargoOutRepository", "Failed to add Cargo Out record: ${e.message}", e)
                callback(false, e.message)
            }
    }
}
