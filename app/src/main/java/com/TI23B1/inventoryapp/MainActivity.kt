package com.TI23B1.inventoryapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.annotations.SerializedName


class MainActivity : AppCompatActivity() {

    private lateinit var qrScanner: QRScanner
    private lateinit var databaseInventory: DatabaseInventory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        databaseInventory = DatabaseInventory()

        qrScanner = QRScanner(this) { cargoInfo ->
            val intent = Intent(this, CargoDetailsActivity::class.java)
            intent.putExtra("cargoId", cargoInfo.cargoId)
            intent.putExtra("type", cargoInfo.type)
            intent.putExtra("namaPemilik", cargoInfo.namaPemilik)
            intent.putExtra("tanggalMasuk", cargoInfo.tanggalMasuk)
            intent.putExtra("status", cargoInfo.status)
            intent.putExtra("quantity", cargoInfo.quantity)
            intent.putExtra("location", cargoInfo.location)
            intent.putExtra("shelf", cargoInfo.shelf)
            startActivity(intent)
        }


        val qrscanbarcodebutton = findViewById<Button>(R.id.barangMasukButton)
        qrscanbarcodebutton.setOnClickListener {
            qrScanner.startQRScan()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



}
