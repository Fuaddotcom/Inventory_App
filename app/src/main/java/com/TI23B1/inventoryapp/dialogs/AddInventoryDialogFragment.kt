package com.TI23B1.inventoryapp.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.CargoIn
import com.TI23B1.inventoryapp.CargoOut
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddInventoryDialog : DialogFragment() {

    // Views
    private lateinit var tvBarangMasuk: TextView
    private lateinit var tvBarangKeluar: TextView
    private lateinit var etNamaBarang: TextInputEditText
    private lateinit var etTanggal: TextInputEditText
    private lateinit var etSupplierPenerima: TextInputEditText
    private lateinit var etQuantityValue: TextInputEditText
    private lateinit var spinnerQuantityUnit: Spinner
    private lateinit var etNomorPoSo: TextInputEditText
    private lateinit var tilSupplierPenerima: TextInputLayout // For the Supplier/Penerima field
    private lateinit var tilNomorPoSo: TextInputLayout        // For the PO/SO field
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    // State variables
    private var isIncomingMode = true
    private var onSaveListener: OnSaveListener? = null

    private val unitOptions = arrayOf("pcs", "kg", "liter", "meter", "box", "pack")

    /**
     * Interface for communicating save events back to the hosting Activity/Fragment.
     */
    interface OnSaveListener {
        fun onSaveCargoIn(cargoIn: CargoIn)
        fun onSaveCargoOut(cargoOut: CargoOut)
    }

    /**
     * Factory method for creating a new instance of the dialog.
     */
    companion object {
        fun newInstance(): AddInventoryDialog {
            return AddInventoryDialog()
        }
    }

    /**
     * Sets the listener for save events.
     * @param listener The implementation of OnSaveListener.
     */
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupToggle()
        setupDatePicker()
        setupSpinner()
        setupButtons()
        setupCloseButton(view)

        // Initialize UI based on initial mode
        //updateToggleUI()
        updateFormFields()
    }

    private fun setupViews(view: View) {
        tvBarangMasuk = view.findViewById(R.id.tv_barang_masuk)
        tvBarangKeluar = view.findViewById(R.id.tv_barang_keluar)
        etNamaBarang = view.findViewById(R.id.et_nama_barang)
        etTanggal = view.findViewById(R.id.et_tanggal)

        tilSupplierPenerima = view.findViewById(R.id.text_input_layout_supplier_penerima)
        etSupplierPenerima = view.findViewById(R.id.et_supplier_penerima)

        tilNomorPoSo = view.findViewById(R.id.text_input_layout_nomor_po_so)
        etNomorPoSo = view.findViewById(R.id.et_nomor_po_so)

        etQuantityValue = view.findViewById(R.id.et_quantity_value)
        spinnerQuantityUnit = view.findViewById(R.id.spinner_quantity_unit)
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
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
            tilSupplierPenerima.hint = getString(R.string.hint_supplier_barang) // Set hint on TextInputLayout
            tilNomorPoSo.hint = getString(R.string.hint_nomor_po)
        } else {
            tilSupplierPenerima.hint = getString(R.string.hint_nama_penerima) // Set hint on TextInputLayout
            tilNomorPoSo.hint = getString(R.string.hint_nomor_sj)
        }
        clearForm()
    }

    private fun setupDatePicker() {
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
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

    private fun setupCloseButton(view: View) {
        view.findViewById<View>(R.id.btn_close).setOnClickListener {
            dismiss()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val fields = listOf(
            Pair(etNamaBarang, R.string.error_nama_barang_empty),
            Pair(etTanggal, R.string.error_tanggal_empty),
            Pair(etQuantityValue, R.string.error_quantity_empty),
        )

        for ((editText, errorMessageResId) in fields) {
            if (editText.text.isNullOrBlank()) {
                editText.error = getString(errorMessageResId)
                isValid = false
            }
        }

        // Specific hints based on mode
        if (etSupplierPenerima.text.isNullOrBlank()) {
            etSupplierPenerima.error = getString(
                if (isIncomingMode) R.string.error_supplier_empty
                else R.string.error_receiver_empty
            )
            isValid = false
        }

        if (etNomorPoSo.text.isNullOrBlank()) {
            etNomorPoSo.error = getString(
                if (isIncomingMode) R.string.error_po_empty
                else R.string.error_so_empty
            )
            isValid = false
        }

        return isValid
    }

    private fun saveData() {
        val cargoId = generateCargoId()
        val namaBarang = etNamaBarang.text.toString().trim()
        val tanggal = etTanggal.text.toString().trim()
        val supplierPenerima = etSupplierPenerima.text.toString().trim()
        val quantityValue = etQuantityValue.text.toString().trim().toIntOrNull() ?: 0
        val quantityUnit = spinnerQuantityUnit.selectedItem.toString()
        val nomorPoSo = etNomorPoSo.text.toString().trim()

        if (isIncomingMode) {
            val cargoIn = CargoIn(
                cargoId = cargoId,
                tanggalMasuk = tanggal,
                namaBarang = namaBarang,
                namaSupplier = supplierPenerima,
                quantity = quantityValue,
                unit = quantityUnit,
                nomorPO = nomorPoSo,
            )
            onSaveListener?.onSaveCargoIn(cargoIn)
        } else {
            val cargoOut = CargoOut(
                cargoId = cargoId,
                tanggalKeluar = tanggal,
                namaBarang = namaBarang,
                tujuanPengiriman = supplierPenerima,
                quantity = quantityValue,
                unit = quantityUnit,
                nomorSuratJalan = nomorPoSo
            )
            onSaveListener?.onSaveCargoOut(cargoOut)
        }

        Toast.makeText(requireContext(), getString(R.string.toast_save_success), Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun clearForm() {
        etNamaBarang.setText("")
        etTanggal.setText("")
        etSupplierPenerima.setText("")
        etQuantityValue.setText("")
        spinnerQuantityUnit.setSelection(0)
        etNomorPoSo.setText("")

        etNamaBarang.error = null
        etTanggal.error = null
        etSupplierPenerima.error = null
        etQuantityValue.error = null
        etNomorPoSo.error = null
    }

    private fun generateCargoId(): String {
        val timestamp = System.currentTimeMillis()
        val prefix = if (isIncomingMode) "IN" else "OUT"
        return "$prefix$timestamp"
    }
}