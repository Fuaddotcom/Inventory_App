package com.TI23B1.inventoryapp.data // Your package

import com.TI23B1.inventoryapp.models.CargoIn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log // Added for logging

class CargoInRepository { // No parameters needed in constructor anymore

    private val database = FirebaseDatabase.getInstance()
    private val cargoInRef = database.getReference("barang_masuk")

    // NEW: Get InventoryStockRepository internally
    private val inventoryStockRepository = InventoryStockRepository()

    fun addCargoIn(cargoIn: CargoIn, callback: (Boolean, String?) -> Unit) {
        val newRef = cargoInRef.push()
        cargoIn.cargoId = newRef.key.toString() // Set the ID from Firebase push key
        newRef.setValue(cargoIn)
            .addOnSuccessListener {
                Log.d("CargoInRepository", "Cargo In record added successfully: ${cargoIn.namaBarang}")
                // Now, update stock immediately after saving CargoIn
                inventoryStockRepository.updateStock(
                    name = cargoIn.namaBarang,
                    quantityChange = cargoIn.quantity,
                    isIncoming = true
                ) { updateSuccess, updateMessage ->
                    if (updateSuccess) {
                        Log.d("CargoInRepository", "Stock updated successfully for Cargo In: ${cargoIn.namaBarang}")
                        callback(true, null) // Report success back to caller
                    } else {
                        Log.e("CargoInRepository", "Failed to update stock for Cargo In: $updateMessage")
                        callback(false, "Failed to update stock: $updateMessage")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CargoInRepository", "Failed to add Cargo In record: ${e.message}", e)
                callback(false, e.message)
            }
    }
}
