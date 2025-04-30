package com.TI23B1.inventoryapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat




class MainActivity : AppCompatActivity() {

    private lateinit var qrScanner: QRScanner
    private lateinit var databaseInventory: DatabaseInventory
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        databaseInventory = DatabaseInventory()

        testDatabase()

        qrScanner = QRScanner(this) { cargoInfo ->
            val intent = Intent(this, CargoDetailsActivity::class.java)
            intent.putExtra("cargoId", cargoInfo.cargoId)
            intent.putExtra("type", cargoInfo.type)
            intent.putExtra("location", cargoInfo.location)
            intent.putExtra("shelf", cargoInfo.shelf)
            startActivity(intent)
        }


        val qrscanbarcodebutton: LinearLayout = findViewById(R.id.qrScanButtonLayout)
        qrscanbarcodebutton.setOnClickListener {
            qrScanner.startQRScan()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun testDatabase() {
        val testCargo = CargoInfo(
            cargoId = "TEST002",
            type = "Electronics",
            location = "Warehouse A",
            shelf = "Shelf 1"
        )

        databaseInventory.insertCargo(testCargo) { success ->
            if (success) {
                Log.d(TAG, "Test cargo inserted successfully")
                Toast.makeText(this, "Test data inserted", Toast.LENGTH_SHORT).show()

                // Now try to retrieve it
                databaseInventory.getCargoById("TEST001") { cargo ->
                    if (cargo != null) {
                        Log.d(TAG, "Retrieved cargo: ${cargo.cargoId}, ${cargo.type}, ${cargo.location}, ${cargo.shelf}")
                        Toast.makeText(this, "Test data retrieved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to retrieve test cargo")
                        Toast.makeText(this, "Failed to retrieve test data", Toast.LENGTH_SHORT).show()
                    }
                }

                // Get all cargo items
                databaseInventory.getAllCargo { cargoList ->
                    Log.d(TAG, "Total cargo items: ${cargoList.size}")
                    cargoList.forEach {
                        Log.d(TAG, "Cargo item: ${it.cargoId}, ${it.type}")
                    }
                }
            } else {
                Log.e(TAG, "Failed to insert test cargo")
                Toast.makeText(this, "Failed to insert test data", Toast.LENGTH_SHORT).show()
            }
        }

    }


}
