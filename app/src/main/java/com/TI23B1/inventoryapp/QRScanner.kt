package com.TI23B1.inventoryapp

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatCallback
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions





class QRScanner(private val activity: AppCompatActivity, private val callback: (CargoInfo) -> Unit) {
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

    init {
        initializeBarcodeLauncher()
    }

    private fun initializeBarcodeLauncher() {
        barcodeLauncher = activity.registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null) {
                Toast.makeText(activity, "Canceled", Toast.LENGTH_LONG).show()
            } else {
                val scannedData = result.contents
                try {
                    val gson = Gson()
                    val cargoInfo = gson.fromJson(scannedData, CargoInfo::class.java)
                    callback(cargoInfo)

                } catch (e: Exception) {
                    Toast.makeText(activity, "Invalid Cargo QR Code", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startQRScan() {
        val options = ScanOptions()
        options.setPrompt("Scan a QR Code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        barcodeLauncher.launch(options)
    }
}