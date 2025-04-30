package com.TI23B1.inventoryapp

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.SerializedName
import java.sql.Date


data class CargoInfo(
    @SerializedName("cargoId") val cargoId: String,
    @SerializedName("type") val type: String,
    @SerializedName("namaPemilik") val namaPemilik: String,
    @SerializedName("tanggalMasuk") val tanggalMasuk: Date,
    @SerializedName("status") val status: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("location") val location: String,
    @SerializedName("shelf") val shelf: String,
)

class DatabaseInventory {
    private val database = FirebaseDatabase.getInstance()
    private val cargoRef = database.getReference("cargo")

    // Insert a cargo item
    fun insertCargo(cargo: CargoInfo, callback: (Boolean) -> Unit) {
        cargoRef.child(cargo.cargoId).setValue(cargo)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Get a specific cargo item by ID
    fun getCargoById(cargoId: String, callback: (CargoInfo?) -> Unit) {
        cargoRef.child(cargoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cargo = snapshot.getValue(CargoInfo::class.java)
                callback(cargo)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    // Get all cargo items
    fun getAllCargo(callback: (List<CargoInfo>) -> Unit) {
        cargoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cargoList = mutableListOf<CargoInfo>()
                for (cargoSnapshot in snapshot.children) {
                    cargoSnapshot.getValue(CargoInfo::class.java)?.let {
                        cargoList.add(it)
                    }
                }
                callback(cargoList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}