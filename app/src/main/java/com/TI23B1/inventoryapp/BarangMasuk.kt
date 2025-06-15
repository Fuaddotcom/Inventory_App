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
import com.google.gson.Gson
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class BarangMasuk : AppCompatActivity() {

    private lateinit var idBarangET: EditText
    private lateinit var namaBarangET: EditText
    private lateinit var kodeBarangET: EditText
    private lateinit var supplierBarangET: EditText
    private lateinit var tanggalMasukET: EditText
    private lateinit var nomorPOED: EditText
    private lateinit var quantityValueET: EditText
    private lateinit var quantityUnitSpinner: Spinner
    private lateinit var statusResultTextView: TextView
    private var qrScanner: QRScanner? = null
    private lateinit var databaseInventory: DatabaseInventory
    private lateinit var statusResultTV: TextView
    private lateinit var namaBarangTV: TextView
    private lateinit var kodeBarangTV: TextView
    private lateinit var supplierBarangTV: TextView
    private lateinit var qtyBarangTV: TextView
    private lateinit var tanggalBarangTV: TextView
    private lateinit var unitBarangTV: TextView
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private val calendar = Calendar.getInstance()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_barang_masuk)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

}