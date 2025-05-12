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
import com.google.firebase.FirebaseNetworkException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class BarangMasuk : AppCompatActivity() {

    private lateinit var idBarangET: EditText
    private lateinit var namaBarangET: EditText
    private lateinit var kodeBarangET: EditText
    private lateinit var supplierBarangET: EditText
    private lateinit var tanggalMasukET: EditText
    private lateinit var quantityValueET: EditText
    private lateinit var quantityUnitSpinner: Spinner
    private lateinit var statusResultTextView: TextView
    private var qrScanner: QRScanner? = null
    private lateinit var databaseInventory: DatabaseInventory
    private lateinit var statusResultTV: TextView
    private lateinit var keteranganResultTV: TextView
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
        keteranganResultTV= findViewById(R.id.keteranganResultTV)
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
            saveCargoData()
        }


        resetButton = findViewById(R.id.resetButton)

        resetButton.setOnClickListener {
            clearInputFields()
        }
    }

    private fun processScannedResult(scannedData: String) {
        try {
            val qrInfo = gson.fromJson(scannedData, CargoInfo::class.java)

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

    private fun saveCargoData() {
        val cargoId = kodeBarangET.text.toString().trim()
        val namaBarang = namaBarangET.text.toString().trim()
        val namaSupplier = supplierBarangET.text.toString().trim()
        val quantity = quantityValueET.text.toString().toIntOrNull() ?: 0
        val unit = quantityUnitSpinner.selectedItem.toString()
        val tanggalMasuk = tanggalMasukET.text.toString().trim()

        if (cargoId.isEmpty() || namaSupplier.isEmpty() || tanggalMasuk.isEmpty()) {
            statusResultTV.text = "Failed"
            statusResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            keteranganResultTV.text = "Please fill in required fields"
            keteranganResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            return
        }

        val cargo = CargoInfo(
            cargoId = cargoId,
            namaBarang = namaBarang,
            namaSupplier = namaSupplier,
            tanggalMasuk = tanggalMasuk,
            status = "Incoming",
            quantity = quantity,
            unit = unit,
        )

        databaseInventory.insertCargo(cargo) { success ->
            if (success) {
                statusResultTV.text = "Success"
                statusResultTV.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
                keteranganResultTV.text = "Data successfully saved"
                keteranganResultTV.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
                Toast.makeText(this, "Cargo data saved successfully", Toast.LENGTH_SHORT).show()
                clearInputFields()
            } else {
                statusResultTV.text = "Failed"
                statusResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
                keteranganResultTV.text = "Failed to save data" // A general failure message initially
                keteranganResultTV.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
                Toast.makeText(this, "Failed to save cargo data", Toast.LENGTH_SHORT).show()
                // We might get more specific error information in the onFailureListener
            }
        }

        databaseInventory.cargoRef.child(cargo.cargoId).setValue(cargo.toMap())
            .addOnSuccessListener {
                statusResultTV.text = "Success"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_green, theme))
                keteranganResultTV.text = "Data successfully saved"
                keteranganResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_green, theme))
                Toast.makeText(this, "Cargo data saved successfully", Toast.LENGTH_SHORT).show()
                clearInputFields()
            }
            .addOnFailureListener { e ->
                statusResultTV.text = "Failed"
                statusResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_red, theme))
                keteranganResultTV.setTextColor(resources.getColor(R.color.gruvbox_bright_red, theme))

                if (e is FirebaseNetworkException) {
                    keteranganResultTV.text = "No network connection. Please check your internet."
                } else if (e.message?.contains("permission_denied") == true) {
                    keteranganResultTV.text = "Permission denied to write to the database."
                } else {
                    keteranganResultTV.text = "Failed to save data. Please try again."
                    e.printStackTrace() // Log the full error for debugging
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