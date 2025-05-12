package com.TI23B1.inventoryapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class CargoDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_cargo_details)

        // Get cargo info from intent
        val cargoId = intent.getStringExtra("cargoId") ?: ""
        val type = intent.getStringExtra("type") ?: ""
        val owner = intent.getStringExtra("namaPemilik") ?: ""
        val date = intent.getStringExtra("tanggalMasuk") ?: ""
        val status = intent.getStringExtra("status") ?: ""
        val quantity = intent.getIntExtra("quantity", 0)
        val location = intent.getStringExtra("location") ?: ""
        val shelf = intent.getStringExtra("shelf") ?: ""

        // Display cargo information in the UI
        findViewById<TextView>(R.id.cargoIdTextView).text = "Cargo ID: $cargoId"
        findViewById<TextView>(R.id.typeTextView).text = "Type: $type"
        findViewById<TextView>(R.id.namaPemilikTextView).text = "Owner: $owner"
        findViewById<TextView>(R.id.tanggalMasukTextView).text = "Date: $date"
        findViewById<TextView>(R.id.statusTextView).text = "Status: $status"
        findViewById<TextView>(R.id.quantityTextView).text = "Quantity: $quantity"
        findViewById<TextView>(R.id.locationTextView).text = "Location: $location"
        findViewById<TextView>(R.id.shelfTextView).text = "Shelf: $shelf"
    }
}