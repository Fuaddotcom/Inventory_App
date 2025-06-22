package com.TI23B1.inventoryapp

import com.TI23B1.inventoryapp.models.CargoIn
import com.TI23B1.inventoryapp.models.CargoOut
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DatabaseInventory {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val cargoRef: DatabaseReference = database.getReference("cargo") // Keep the main cargo ref for general info if needed
    val barangMasukRef: DatabaseReference = database.getReference("barang_masuk")
    val barangKeluarRef: DatabaseReference = database.getReference("barang_keluar")

    companion object {
        @Volatile
        private var INSTANCE: DatabaseInventory? = null

        fun getInstance(): DatabaseInventory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseInventory().also {
                    INSTANCE = it
                }
            }
        }
    }

    fun insertBarangMasuk(cargo: CargoIn, onSuccess: (Boolean) -> Unit) {
        barangMasukRef.child(cargo.cargoId).setValue(cargo.toMap())
            .addOnSuccessListener { onSuccess(true) }
            .addOnFailureListener { onSuccess(false) }
    }

    fun insertBarangKeluar(cargo: CargoOut, onSuccess: (Boolean) -> Unit) {
        barangKeluarRef.child(cargo.cargoId).setValue(cargo.toMap())
            .addOnSuccessListener { onSuccess(true) }
            .addOnFailureListener { onSuccess(false) }
    }

    // You might still have the general insertCargo if you use the 'cargo' node for other purposes
    fun insertCargo(cargo: CargoIn, onSuccess: (Boolean) -> Unit) {
        cargoRef.child(cargo.cargoId).setValue(cargo.toMap())
            .addOnSuccessListener { onSuccess(true) }
            .addOnFailureListener { onSuccess(false) }
    }
}