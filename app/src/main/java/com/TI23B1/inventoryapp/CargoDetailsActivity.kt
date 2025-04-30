package com.TI23B1.inventoryapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CargoDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cargo_details)

        // Get the data passed from the previous activity
        val cargoId = intent.getStringExtra("cargoId")
        val type = intent.getStringExtra("type")
        val namaPemilik = intent.getStringExtra("namaPemilik")
        val tanggalMasuk = intent.getStringExtra("tanggalMasuk") // Assuming you'll pass this as a String
        val status = intent.getStringExtra("status")
        val quantity = intent.getIntExtra("quantity", 0)
        val location = intent.getStringExtra("location")
        val shelf = intent.getStringExtra("shelf")

        // Find the TextViews in your layout
        val cargoIdTextView = findViewById<TextView>(R.id.cargoIdTextView)
        val typeTextView = findViewById<TextView>(R.id.typeTextView)
        val namaPemilikTextView = findViewById<TextView>(R.id.namaPemilikTextView)
        val tanggalMasukTextView = findViewById<TextView>(R.id.tanggalMasukTextView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val quantityTextView = findViewById<TextView>(R.id.quantityTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val shelfTextView = findViewById<TextView>(R.id.shelfTextView)

        // Set the values to the TextViews
        cargoIdTextView.text = cargoId
        typeTextView.text = type
        namaPemilikTextView.text = namaPemilik
        tanggalMasukTextView.text = tanggalMasuk
        statusTextView.text = status
        quantityTextView.text = quantity.toString()
        locationTextView.text = location
        shelfTextView.text = shelf

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}