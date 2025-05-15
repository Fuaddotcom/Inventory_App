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

        databaseInventory = DatabaseInventory.getInstance()

        quantityUnitSpinner = findViewById<Spinner>(R.id.QuantityUnitSpinner).apply {
            val units = arrayOf("pcs", "kilos", "grams", "liters")
            adapter = ArrayAdapter(this@BarangMasuk, R.layout.spinner_item_dropdown, units)
        }

        // Initialize TextViews
        namaBarangTV = findViewById(R.id.namabarangresultTV)
        kodeBarangTV = findViewById(R.id.kodebarangresultTV)
        supplierBarangTV = findViewById(R.id.supplierbarangresultTV)
        qtyBarangTV = findViewById(R.id.quantitybarangresultTV)
        tanggalBarangTV = findViewById(R.id.tanggalBarangKeluarResultTV)
        statusResultTV = findViewById(R.id.statusResultTV)
        nomorPOED = findViewById(R.id.nomorPOED)
        unitBarangTV = findViewById(R.id.unitbarangresultTV)

        idBarangET = findViewById(R.id.IdBarangET)
        namaBarangET = findViewById(R.id.NamaBarangET)
        kodeBarangET = findViewById(R.id.IdBarangET)
        supplierBarangET = findViewById(R.id.SupplierET)
        quantityValueET = findViewById(R.id.QuantityValueET)

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        tanggalMasukET = findViewById<EditText>(R.id.TanggalMasukET).apply {
            setOnClickListener {
                DatePickerDialog(
                    this@BarangMasuk,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        statusResultTextView = findViewById(R.id.statusResultTV) // Initialize here if needed

        val updateResultButton = findViewById<Button>(R.id.updateResultBT).apply {
            setOnClickListener {
                updateDisplayedResultsFromEditText()
            }
        }

        qrScanner = QRScanner(this) { scannedData ->
            if (scannedData != null) {
                processScannedResult(scannedData)
            } else {
                Toast.makeText(this, "Scan failed or cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.scanQRBT).setOnClickListener {
            qrScanner?.startQRScan()
        }


        updateLabel()


        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            saveBarangMasukData() // Changed function name
        }


        resetButton = findViewById(R.id.resetButton)
        resetButton.setOnClickListener {
            clearInputFields()
        }
    }

    private fun processScannedResult(scannedData: String) {
        try {
            val qrInfo = gson.fromJson(scannedData, CargoIn::class.java)

            kodeBarangTV.text = qrInfo.cargoId ?: ""
            namaBarangTV.text = qrInfo.namaBarang ?: ""
            supplierBarangTV.text = qrInfo.namaSupplier ?: ""
            qtyBarangTV.text = qrInfo.quantity?.toString() ?: ""
            unitBarangTV.text = qrInfo.unit ?: ""
            tanggalBarangTV.text = qrInfo.tanggalMasuk ?: SimpleDateFormat("dd MMM yyy", Locale.getDefault()).format(Calendar.getInstance().time)

            // Optionally populate the input fields with scanned data
            idBarangET.setText(qrInfo.cargoId ?: "")
            namaBarangET.setText(qrInfo.namaBarang ?: "")
            supplierBarangET.setText(qrInfo.namaSupplier ?: "")
            quantityValueET.setText(qrInfo.quantity?.toString() ?: "")
            val unitIndex = (quantityUnitSpinner.adapter as ArrayAdapter<String>).getPosition(qrInfo.unit)
            if (unitIndex != -1) {
                quantityUnitSpinner.setSelection(unitIndex)
            }
            tanggalMasukET.setText(qrInfo.tanggalMasuk ?: "")

        } catch (e: Exception) {
            Toast.makeText(this, "Invalid QR code format (not JSON)", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveBarangMasukData() { // Changed function name
        val cargoId = kodeBarangET.text.toString().trim()
        val namaBarang = namaBarangET.text.toString().trim()
        val namaSupplier = supplierBarangET.text.toString().trim()
        val quantity = quantityValueET.text.toString().toIntOrNull() ?: 0
        val unit = quantityUnitSpinner.selectedItem.toString()
        val tanggalMasuk = tanggalMasukET.text.toString().trim()
        val nomorPO = nomorPOED.text.toString().trim()

        if (cargoId.isEmpty() || namaSupplier.isEmpty() || tanggalMasuk.isEmpty()) {
            statusResultTV.text = "Failed: Please fill required fields"
            statusResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            return
        }

        val cargo = CargoIn(
            cargoId = cargoId,
            namaBarang = namaBarang,
            namaSupplier = namaSupplier,
            tanggalMasuk = tanggalMasuk,
            quantity = quantity,
            unit = unit,
            nomorPO = nomorPO
        )

        databaseInventory.insertBarangMasuk(cargo) { success -> // Use the new function
            if (success) {
                statusResultTV.text = "Success: Data saved"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_green, theme))
                Toast.makeText(this, "Barang Masuk data saved successfully", Toast.LENGTH_SHORT).show()
                clearInputFields()
            } else {
                statusResultTV.text = "Failed: Could not save data"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_red, theme))
                Toast.makeText(this, "Failed to save Barang Masuk data", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun clearInputFields() {
        idBarangET.text.clear()
        namaBarangET.text.clear()
        kodeBarangET.text.clear()
        supplierBarangET.text.clear()
        quantityValueET.text.clear()
        tanggalMasukET.text.clear()
        quantityUnitSpinner.setSelection(0)
    }


    private fun updateDisplayedResultsFromEditText() {
        kodeBarangTV.text = kodeBarangET.text.toString().trim()
        namaBarangTV.text = namaBarangET.text.toString().trim()
        supplierBarangTV.text = supplierBarangET.text.toString().trim()
        qtyBarangTV.text = quantityValueET.text.toString().trim()

        val selectedUnit = quantityUnitSpinner.selectedItem.toString()
        unitBarangTV.text = selectedUnit

        val enteredDate = tanggalMasukET.text.toString().trim()
        tanggalBarangTV.text = enteredDate
    }

    private fun updateLabel() {
        val myFormat = "dd MMM yyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        tanggalMasukET.setText(sdf.format(calendar.time))
    }
}