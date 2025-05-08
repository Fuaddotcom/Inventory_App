package com.TI23B1.inventoryapp

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class QRScanner(
    private val activity: AppCompatActivity,
    private val onScanSuccess: (String?) -> Unit // Changed callback to String?
) {
    // QR code scanner launcher
    private val barcodeLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)

            if (scanResult != null && scanResult.contents != null) {
                onScanSuccess(scanResult.contents)
            } else {
                onScanSuccess(null)
            }
        } else {
            onScanSuccess(null)
        }
    }

    // Start QR scanning
    fun startQRScan() {
        val integrator = IntentIntegrator(activity)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setCameraId(0)  // Use default camera
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)

        // Use the ActivityResultLauncher instead of calling directly
        val scanIntent = integrator.createScanIntent()
        barcodeLauncher.launch(scanIntent)
    }
}