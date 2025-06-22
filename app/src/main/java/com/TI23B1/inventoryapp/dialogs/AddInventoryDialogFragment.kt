package com.TI23B1.inventoryapp.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.models.CargoIn
import com.TI23B1.inventoryapp.models.CargoOut
import com.TI23B1.inventoryapp.data.InventoryStockRepository
import com.TI23B1.inventoryapp.data.InventoryRepository
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.*

class AddInventoryDialog(
    private val inventoryRepository: InventoryRepository,
    private val inventoryStockRepository: InventoryStockRepository
) : DialogFragment() {

    private lateinit var tvBarangMasuk: TextView
    private lateinit var tvBarangKeluar: TextView
    private lateinit var spinnerNamaBarang: Spinner
    private lateinit var pbLoadingItems: ProgressBar

    // QR Scan related (based on your XML)
    private lateinit var etKodeBarang: TextInputEditText
    private lateinit var btnScanQr: Button

    private lateinit var etTanggal: TextInputEditText
    private lateinit var etSupplierPenerima: TextInputEditText
    private lateinit var etQuantityValue: TextInputEditText
    private lateinit var spinnerQuantityUnit: Spinner
    private lateinit var etNomorPoSo: TextInputEditText
    private lateinit var tilSupplierPenerima: TextInputLayout
    private lateinit var tilNomorPoSo: TextInputLayout

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var isIncomingMode = true
    private var onSaveListener: OnSaveListener? = null
    private val unitOptions = arrayOf("pcs", "kg", "liter", "meter", "box", "pack")
    private var itemNamesList: MutableList<String> = mutableListOf()
    private var selectedItemName: String? = null


    interface OnSaveListener {
        fun onSaveCargoIn(cargoIn: CargoIn)
        fun onSaveCargoOut(cargoOut: CargoOut)
        fun onCargoOutSavedAndReadyForPrint(cargoOut: CargoOut)
    }

    companion object {
        fun newInstance(
            inventoryRepository: InventoryRepository,
            inventoryStockRepository: InventoryStockRepository
        ): AddInventoryDialog {
            return AddInventoryDialog(inventoryRepository, inventoryStockRepository)
        }
        const val QR_SCAN_REQUEST_CODE = 4937 // Keep this if QR scanner is used
    }

    fun setOnSaveListener(listener: OnSaveListener) {
        this.onSaveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_input_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupToggle()
        setupDatePicker()
        setupSpinner()
        setupItemNameSpinner()
        setupButtons()
        setupQRScanner() // Setup QR scanner

        updateToggleUI()
        updateFormFields()
    }


    private fun setupViews(view: View) {
        tvBarangMasuk = view.findViewById(R.id.tv_barang_masuk)
        tvBarangKeluar = view.findViewById(R.id.tv_barang_keluar)
        spinnerNamaBarang = view.findViewById(R.id.spinner_nama_barang)
        pbLoadingItems = view.findViewById(R.id.pb_loading_items)

        // Binding QR Code fields as per your XML
        etKodeBarang = view.findViewById(R.id.et_kode_barang)
        btnScanQr = view.findViewById(R.id.btn_scan_qr)

        etTanggal = view.findViewById(R.id.et_tanggal)
        tilSupplierPenerima = view.findViewById(R.id.text_input_layout_supplier_penerima)
        etSupplierPenerima = view.findViewById(R.id.et_supplier_penerima)
        tilNomorPoSo = view.findViewById(R.id.text_input_layout_nomor_po_so)
        etNomorPoSo = view.findViewById(R.id.et_nomor_po_so)
        etQuantityValue = view.findViewById(R.id.et_quantity_value)
        spinnerQuantityUnit = view.findViewById(R.id.spinner_quantity_unit)

        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)/*  */

        view.findViewById<View>(R.id.btn_close).setOnClickListener {
            dismiss()
        }
    }

    // QR Scanner setup
    private fun setupQRScanner() {
        btnScanQr.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setPrompt("Scan Barcode")
                .setOrientationLocked(false)
                .initiateScan()
        }
    }

    // Handle QR Scan result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(context, "Scan Dibatalkan", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Hasil Scan: " + result.contents, Toast.LENGTH_LONG).show()
                etKodeBarang.setText(result.contents) // Populate kode barang field
            }
        }
    }


    private fun setupToggle() {
        tvBarangMasuk.setOnClickListener {
            if (!isIncomingMode) {
                isIncomingMode = true
                updateToggleUI()
                updateFormFields()
            }
        }

        tvBarangKeluar.setOnClickListener {
            if (isIncomingMode) {
                isIncomingMode = false
                updateToggleUI()
                updateFormFields()
            }
        }
    }

    private fun updateToggleUI() {
        context?.let {
            val selectedTextColor = ContextCompat.getColor(it, R.color.white)
            val unselectedTextColor = ContextCompat.getColor(it, R.color.red_primary)

            if (isIncomingMode) {
                tvBarangMasuk.setBackgroundResource(R.drawable.toggle_selected)
                tvBarangMasuk.setTextColor(selectedTextColor)

                tvBarangKeluar.setBackgroundResource(R.drawable.toggle_unselected)
                tvBarangKeluar.setTextColor(unselectedTextColor)

            } else {
                tvBarangKeluar.setBackgroundResource(R.drawable.toggle_selected)
                tvBarangKeluar.setTextColor(selectedTextColor)

                tvBarangMasuk.setBackgroundResource(R.drawable.toggle_unselected)
                tvBarangMasuk.setTextColor(unselectedTextColor)
            }
        }
    }

    private fun updateFormFields() {
        if (isIncomingMode) {
            tilSupplierPenerima.hint = getString(R.string.hint_supplier_barang)
            tilNomorPoSo.hint = getString(R.string.hint_nomor_po)
            tilNomorPoSo.visibility = View.VISIBLE
        } else {
            tilSupplierPenerima.hint = getString(R.string.hint_nama_penerima)
            tilNomorPoSo.hint = getString(R.string.hint_nomor_sj)
            tilNomorPoSo.visibility = View.VISIBLE
        }
        clearForm()
        setupItemNameSpinner()
    }

    private fun setupDatePicker() {
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    // Display format in etTanggal
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etTanggal.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuantityUnit.adapter = adapter
    }

    private fun setupItemNameSpinner() {
        pbLoadingItems.visibility = View.VISIBLE
        inventoryRepository.getAvailableItemNames { names ->
            itemNamesList.clear()
            itemNamesList.add("Pilih Barang...")
            itemNamesList.addAll(names.sorted())

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemNamesList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNamaBarang.adapter = adapter
            pbLoadingItems.visibility = View.GONE

            spinnerNamaBarang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedOption = itemNamesList[position]
                    if (selectedOption == "Pilih Barang...") {
                        selectedItemName = null
                    } else {
                        selectedItemName = selectedOption
                        if (!isIncomingMode) {
                            inventoryStockRepository.getInventoryStock(selectedItemName!!) { stock ->
                                if (stock != null) {
                                    Toast.makeText(context, "Stok $selectedItemName: ${stock.stokTersedia} ${stock.unit}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "$selectedItemName tidak ada dalam stok.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedItemName = null
                }
            }
        }
    }

    private fun setupButtons() {
        btnSave.setOnClickListener {
            if (validateForm()) {
                saveData()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }


    private fun validateForm(): Boolean {
        var isValid = true

        etTanggal.error = null
        etQuantityValue.error = null
        etSupplierPenerima.error = null
        etNomorPoSo.error = null
        etKodeBarang.error = null

        val spinnerSelectedItem = spinnerNamaBarang.selectedItem?.toString()

        if (spinnerSelectedItem == "Pilih Barang...") {
            Toast.makeText(requireContext(), getString(R.string.error_select_item), Toast.LENGTH_SHORT).show()
            isValid = false
        } else {
            selectedItemName = spinnerSelectedItem
        }

        if (selectedItemName.isNullOrBlank()) {
            isValid = false
        }

        if (etTanggal.text.isNullOrBlank()) {
            etTanggal.error = getString(R.string.error_tanggal_empty)
            isValid = false
        }

        if (etKodeBarang.text.isNullOrBlank()) {
            etKodeBarang.error = "Kode Barang tidak boleh kosong"
            isValid = false
        }

        val quantityVal = etQuantityValue.text.toString().trim().toIntOrNull()
        if (quantityVal == null || quantityVal <= 0) {
            etQuantityValue.error = getString(R.string.error_quantity_empty)
            isValid = false
        }

        if (etSupplierPenerima.text.isNullOrBlank()) {
            etSupplierPenerima.error = getString(
                if (isIncomingMode) R.string.error_supplier_empty
                else R.string.error_receiver_empty
            )
            isValid = false
        }

        if (!isIncomingMode && etNomorPoSo.text.toString().trim().isEmpty()) {
            etNomorPoSo.error = getString(R.string.error_so_empty)
            isValid = false
        }

        if (!isIncomingMode && selectedItemName != null && selectedItemName != "Pilih Barang...") {
            if (!itemNamesList.contains(selectedItemName)) {
                Toast.makeText(context, getString(R.string.error_select_item), Toast.LENGTH_LONG).show()
                isValid = false
            }
        }

        return isValid
    }

    private fun saveData() {
        val namaBarang = selectedItemName!!
        val kodeBarang = etKodeBarang.text.toString().trim()
        val tanggalSelectedString = etTanggal.text.toString().trim() // e.g., "20/06/2025"
        val supplierPenerima = etSupplierPenerima.text.toString().trim()
        val quantityValue = etQuantityValue.text.toString().trim().toIntOrNull() ?: 0
        val quantityUnit = spinnerQuantityUnit.selectedItem.toString()
        val nomorPoSo = etNomorPoSo.text.toString().trim()

        // FIX: Parse the dd/MM/yyyy string from the picker into a Date object,
        // then format that Date object into the desired "dd Mon yyyy" string.
        val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Desired format

        val parsedDate: Date? = try {
            inputDateFormat.parse(tanggalSelectedString)
        } catch (e: Exception) {
            Log.e("AddInventoryDialog", "Error parsing selected date string: $tanggalSelectedString", e)
            Toast.makeText(context, "Format tanggal tidak valid. Silakan pilih tanggal lagi.", Toast.LENGTH_LONG).show()
            return
        }

        if (parsedDate == null) {
            Toast.makeText(context, "Tanggal tidak valid.", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDateForModel = outputDateFormat.format(parsedDate) // This is "20 Jun 2025"


        if (!isIncomingMode) {
            inventoryStockRepository.getInventoryStock(namaBarang) { inventoryStock ->
                val currentStock = inventoryStock?.stokTersedia ?: 0
                if (quantityValue > currentStock) {
                    Toast.makeText(context, "Stok tidak cukup untuk $namaBarang. Tersedia: $currentStock, Diminta: $quantityValue", Toast.LENGTH_LONG).show()
                    Log.w("AddInventoryDialog", "Insufficient stock detected again before saving.")
                    return@getInventoryStock
                }

                val cargoOut = CargoOut(
                    cargoId = "", // Repository will set this
                    tanggalKeluar = formattedDateForModel, // Use the new formatted date
                    namaBarang = namaBarang,
                    tujuanPengiriman = supplierPenerima,
                    quantity = quantityValue,
                    unit = quantityUnit,
                    nomorSuratJalan = nomorPoSo
                )
                onSaveListener?.onSaveCargoOut(cargoOut)
                onSaveListener?.onCargoOutSavedAndReadyForPrint(cargoOut)
                Log.d("AddInventoryDialog", "CargoOut data passed to listener. Dialog will dismiss.")
                Toast.makeText(requireContext(), getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show()
                dismiss()
            }
        } else { // Incoming mode
            val cargoIn = CargoIn(
                cargoId = "", // Repository will set this
                tanggalMasuk = formattedDateForModel, // Use the new formatted date
                namaBarang = namaBarang,
                namaSupplier = supplierPenerima,
                quantity = quantityValue,
                unit = quantityUnit,
                nomorPO = nomorPoSo
            )
            onSaveListener?.onSaveCargoIn(cargoIn)
            Log.d("AddInventoryDialog", "CargoIn data passed to listener. Dialog will dismiss.")
            Toast.makeText(requireContext(), getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun clearForm() {
        spinnerNamaBarang.setSelection(0, false)
        etTanggal.setText("")
        etSupplierPenerima.setText("")
        etQuantityValue.setText("")
        spinnerQuantityUnit.setSelection(0)
        etNomorPoSo.setText("")
        etKodeBarang.setText("")

        etTanggal.error = null
        etSupplierPenerima.error = null
        etQuantityValue.error = null
        etNomorPoSo.error = null
        etKodeBarang.error = null
    }
}
