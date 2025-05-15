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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // Initialize DatabaseInventory
        databaseInventory = DatabaseInventory()

        val ActivityBarangMasuk = findViewById<Button>(R.id.barangMasukButton)
        ActivityBarangMasuk.setOnClickListener {
            val intent = Intent(this, BarangMasuk::class.java)
            startActivity(intent)
        }

        val ActivityBarangKeluar = findViewById<Button>(R.id.barangKeluarButton)
        ActivityBarangKeluar.setOnClickListener {
            val intent = Intent(this, BarangKeluar::class.java)
            startActivity(intent)
        }

        val ActivityLaporan = findViewById<Button>(R.id.laporanButton)
        ActivityLaporan.setOnClickListener {
            val intent = Intent(this, Laporan::class.java)
            startActivity(intent)
        }

        val ActivityManajemenBarangdanSupplier = findViewById<Button>(R.id.manajemenBarangSupplierButton)
        ActivityManajemenBarangdanSupplier.setOnClickListener {
            val intent = Intent(this, ManajemenBarangdanSupplier::class.java)
            startActivity(intent)
        }

        val ActivityManajemenUser = findViewById<Button>(R.id.manajemenPenggunaButton)
        ActivityManajemenUser.setOnClickListener {
            val intent = Intent(this, ManajemenUser::class.java)
            startActivity(intent)
        }

    }
}