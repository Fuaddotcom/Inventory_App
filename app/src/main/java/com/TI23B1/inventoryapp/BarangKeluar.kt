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

        databaseInventory = DatabaseInventory.getInstance()

        namaBarangKeluarET = findViewById(R.id.NamaBarangKeluarET)
        tujuanBarangKeluarED = findViewById(R.id.tujuanBarangKeluarED)
        nomorSuratJalanED = findViewById(R.id.nomorSuratJalanED)
        quantityValueED = findViewById(R.id.QuantityValueED)
        quantityUnitSpinner = findViewById(R.id.QuantityUnitSpinner)
        statusResultTV = findViewById(R.id.statusResultTV)

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

        saveButton = findViewById(R.id.saveButtonKeluar) // Ensure you have a save button in your layout
        saveButton.setOnClickListener {
            saveBarangKeluarData()
        }


        updateLabel()
    }

    private fun saveBarangKeluarData() {
        val cargoId = idBarangKeluarET.text.toString().trim()
        val namaBarang = namaBarangKeluarET.text.toString().trim()
        val quantity = quantityValueED.text.toString().toIntOrNull() ?: 0
        val unit = quantityUnitSpinner.selectedItem.toString()
        val tanggalKeluar = tanggalKeluarED.text.toString().trim()
        val tujuanPengiriman = tujuanBarangKeluarED.text.toString().trim()
        val nomorSuratJalan = nomorSuratJalanED.text.toString().trim()

        if (cargoId.isEmpty() || tanggalKeluar.isEmpty()) {
            statusResultTV.text = "Failed: Please fill required fields"
            statusResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            return
        }

        val cargoKeluar = CargoOut( // Reusing CargoInfo, but adjust fields as needed
            cargoId = cargoId,
            tanggalKeluar = tanggalKeluar,
            namaBarang = namaBarang, // You might want to fetch this from existing data based on cargoId
            tujuanPengiriman = tujuanPengiriman,
            quantity = -quantity, // Use negative quantity to indicate removal
            unit = unit,
            nomorSuratJalan = nomorSuratJalan // Supplier might not be relevant for keluar
        )

        databaseInventory.insertBarangKeluar(cargoKeluar) { success ->
            if (success) {
                statusResultTV.text = "Success: Item removed"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_green, theme))
                Toast.makeText(this, "Barang Keluar data saved successfully", Toast.LENGTH_SHORT).show()
                clearInputFieldsKeluar() // Implement this function
            } else {
                statusResultTV.text = "Failed: Could not record removal"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_red, theme))
                Toast.makeText(this, "Failed to save Barang Keluar data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputFieldsKeluar() {
        idBarangKeluarET.text.clear()
        namaBarangKeluarET.text.clear()
        quantityValueED.text.clear()
        tanggalKeluarED.text.clear()
        quantityUnitSpinner.setSelection(0)
    }

    private fun updateLabel(){
        val myFormat = "dd MMM yyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tanggalKeluarED.setText(sdf.format(calendar.time))
    }
}