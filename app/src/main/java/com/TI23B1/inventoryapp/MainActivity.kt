package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {


    private lateinit var databaseInventory: DatabaseInventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false) // This replaces enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize DatabaseInventory
        databaseInventory = DatabaseInventory()

        // Initialize QR Scanner with success callback



        val ActivityBarangMasuk = findViewById<Button>(R.id.barangMasukButton)
        ActivityBarangMasuk.setOnClickListener {
            val intent = Intent(this, BarangMasuk::class.java)
            startActivity(intent);
        }

        val ActivityBarangKeluar = findViewById<Button>(R.id.barangKeluarButton)
        ActivityBarangKeluar.setOnClickListener {
            val intent = Intent(this, BarangKeluar::class.java)
            startActivity(intent);
        }

        // You could also have other buttons for different functions
        // For example, view all cargo button:
//        val viewAllButton = findViewById<Button>(R.id.viewAllButton)
//        viewAllButton.setOnClickListener {
//            databaseInventory.getAllCargo { cargoList ->
//                val intent = Intent(this, CargoListActivity::class.java)
//                // You'd need to make CargoInfo Parcelable or Serializable to pass the list
//                // Or use a singleton pattern to hold the data temporarily
//                startActivity(intent)
//            }
//        }
    }
}