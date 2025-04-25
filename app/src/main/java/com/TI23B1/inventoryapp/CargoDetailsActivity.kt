package com.TI23B1.inventoryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CargoDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cargo_details)

        val cargoId = intent.getStringExtra("cargoId")
        val type = intent.getStringExtra("type")
        val location = intent.getStringExtra("location")
        val shelf = intent.getStringExtra("shelf")

        val cargoIdView = findViewById<TextView>(R.id.cargoIdTV)
        val cargoTypeView = findViewById<TextView>(R.id.cargoTypeTV)
        val cargoLocView = findViewById<TextView>(R.id.cargoLocTV)
        val cargoShelfView = findViewById<TextView>(R.id.cargoShelfTV)

        cargoIdView.text = "ID:" + cargoId
        cargoTypeView.text = "Tipe: " + type
        cargoLocView.text = "Lokasi: " + location
        cargoShelfView.text = "Penyimpanan: " + shelf


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}