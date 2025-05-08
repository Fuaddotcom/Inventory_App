package com.TI23B1.inventoryapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BarangKeluar : AppCompatActivity() {

    private lateinit var  quantityValueED : EditText
    private lateinit var  quantityUnitSpinner : Spinner
    private lateinit var  tanggalKeluarED : EditText
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

        quantityValueED = findViewById(R.id.QuantityValueED)
        quantityUnitSpinner = findViewById(R.id.QuantityUnitSpinner)
        val units = arrayOf("pcs", "kilos", "grams", "liters")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_dropdown, units)
        quantityUnitSpinner.adapter = adapter

        tanggalKeluarED = findViewById(R.id.TanggalKeluarED)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        tanggalKeluarED.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        updateLabel()
    }

    private fun updateLabel(){
        val myFormat = "dd MMM yyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tanggalKeluarED.setText(sdf.format(calendar.time))
    }
}