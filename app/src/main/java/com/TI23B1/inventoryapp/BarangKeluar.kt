package com.TI23B1.inventoryapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BarangKeluar : AppCompatActivity() {

    private lateinit var idBarangKeluarET: EditText
    private lateinit var namaBarangKeluarET: EditText
    private lateinit var tujuanBarangKeluarED: EditText
    private lateinit var nomorSuratJalanED: EditText
    private lateinit var quantityValueED: EditText
    private lateinit var quantityUnitSpinner: Spinner
    private lateinit var tanggalKeluarED: EditText
    private lateinit var saveButton: Button
    private lateinit var databaseInventory: DatabaseInventory
    private lateinit var statusResultTV: TextView
    private val calendar = Calendar.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_barang_keluar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

}