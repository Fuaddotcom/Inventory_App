package com.TI23B1.inventoryapp

import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.Date


class DatabaseInventory {
     val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    val cargoRef = database.child("cargo")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        @Volatile
        private var instance: DatabaseInventory? = null

        fun getInstance(): DatabaseInventory {
            return instance ?: synchronized(this) {
                instance ?: DatabaseInventory().also { instance = it }
            }
        }
    }


    // Insert a cargo item
    fun insertCargo(cargo: CargoInfo, callback: (Boolean) -> Unit) {
        cargoRef.child(cargo.cargoId).setValue(cargo.toMap())
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
                val cargo = CargoInfo.fromSnapshot(snapshot)
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
                    CargoInfo.fromSnapshot(cargoSnapshot)?.let {
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

    /**
     * Process scanned barcode data and save to database
     * @param scannedData The barcode data scanned
     * @param location Current warehouse location
     * @param shelf Current shelf identifier
     * @param callback Callback with success status and CargoInfo object
     */
    fun processScannedData(
        scannedData: String,
        location: String,
        shelf: String,
        callback: (Boolean, CargoInfo?) -> Unit
    ) {
        try {
            // Parse the scanned data - assuming format: "type|ownerName|quantity"
            val parts = scannedData.split("|")

            if (parts.size < 3) {
                callback(false, null)
                return
            }

            val type = parts[0]
            val supplierName = parts[1] // Assuming the second part is the supplier's name
            val quantity = parts[2].toIntOrNull() ?: 1 // Default to 1 if parsing fails

            // Generate a unique ID for this cargo
            val cargoId = UUID.randomUUID().toString()

            // Get current date as formatted string
            val currentDate = dateFormat.format(Date())

            // Create cargo object with current date as string
            val cargo = CargoInfo( // Using the CargoInfo defined at the top level
                cargoId = cargoId,
                tanggalMasuk = currentDate,
                namaSupplier = supplierName,
                status = "Checked In",
                quantity = quantity,
                unit = "",
            )

            // Insert into database
            insertCargo(cargo) { success ->
                callback(success, if (success) cargo else null)
            }

        } catch (e: Exception) {
            callback(false, null)
        }
    }
}