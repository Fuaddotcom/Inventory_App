package com.TI23B1.inventoryapp

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.HashMap
import java.util.UUID
import java.util.Date

data class CargoInfo(
    val cargoId: String = "",
    val type: String = "",
    val namaPemilik: String = "",
    val tanggalMasuk: String = "", // Changed to String
    val status: String = "",
    val quantity: Int = 0,
    val location: String = "",
    val shelf: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", 0, "", "")

    // Convert to Map for Firebase
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "cargoId" to cargoId,
            "type" to type,
            "namaPemilik" to namaPemilik,
            "tanggalMasuk" to tanggalMasuk, // Already a String, no conversion needed
            "status" to status,
            "quantity" to quantity,
            "location" to location,
            "shelf" to shelf
        )
    }

    companion object {
        // Create CargoInfo from Firebase DataSnapshot
        fun fromSnapshot(snapshot: DataSnapshot): CargoInfo? {
            val map = snapshot.value as? HashMap<*, *> ?: return null

            return try {
                CargoInfo(
                    cargoId = map["cargoId"] as? String ?: "",
                    type = map["type"] as? String ?: "",
                    namaPemilik = map["namaPemilik"] as? String ?: "",
                    tanggalMasuk = map["tanggalMasuk"] as? String ?: "",
                    status = map["status"] as? String ?: "",
                    quantity = (map["quantity"] as? Long)?.toInt() ?: 0,
                    location = map["location"] as? String ?: "",
                    shelf = map["shelf"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

class DatabaseInventory {
    private val database = FirebaseDatabase.getInstance()
    private val cargoRef = database.getReference("cargo")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            val ownerName = parts[1]
            val quantity = parts[2].toIntOrNull() ?: 1 // Default to 1 if parsing fails

            // Generate a unique ID for this cargo
            val cargoId = UUID.randomUUID().toString()

            // Get current date as formatted string
            val currentDate = dateFormat.format(Date())

            // Create cargo object with current date as string
            val cargo = CargoInfo(
                cargoId = cargoId,
                type = type,
                namaPemilik = ownerName,
                tanggalMasuk = currentDate,
                status = "Checked In",
                quantity = quantity,
                location = location,
                shelf = shelf
            )

            // Insert into database
            insertCargo(cargo) { success ->
                callback(success, if (success) cargo else null)
            }

        } catch (e: Exception) {
            callback(false, null)
        }
    }

    /**
     * Process scanned data with custom format
     * @param scannedData The raw scanned data
     * @param dataFormat Format specification (e.g., "type|name|quantity|status")
     * @param location Current warehouse location
     * @param shelf Current shelf identifier
     * @param callback Callback with success status and CargoInfo object
     */
    fun processScannedDataCustomFormat(
        scannedData: String,
        dataFormat: String,
        location: String,
        shelf: String,
        callback: (Boolean, CargoInfo?) -> Unit
    ) {
        try {
            val formatFields = dataFormat.split("|")
            val dataFields = scannedData.split("|")

            if (dataFields.size < formatFields.size) {
                callback(false, null)
                return
            }

            // Create a map of field names to values
            val fieldMap = mutableMapOf<String, String>()
            for (i in formatFields.indices) {
                if (i < dataFields.size) {
                    fieldMap[formatFields[i]] = dataFields[i]
                }
            }

            // Generate a unique ID for this cargo
            val cargoId = UUID.randomUUID().toString()

            // Get current date as formatted string
            val currentDate = dateFormat.format(Date())

            // Create cargo object
            val cargo = CargoInfo(
                cargoId = cargoId,
                type = fieldMap["type"] ?: "",
                namaPemilik = fieldMap["name"] ?: fieldMap["owner"] ?: "",
                tanggalMasuk = currentDate,
                status = fieldMap["status"] ?: "Checked In",
                quantity = (fieldMap["quantity"] ?: "1").toIntOrNull() ?: 1,
                location = location,
                shelf = shelf
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