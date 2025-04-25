package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class ScanActivity : AppCompatActivity() {

    private lateinit var scanButton: Button
    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)

        barcodeLauncher =registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null){
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show()
            } else {
                val scannedTagId = result.contents
                Toast.makeText(this, "Scanned: $scannedTagId", Toast.LENGTH_LONG).show()
            }
        }
        scanButton.setOnClickListener { startQRScan() }
    }

    private fun startQRScan(){
        val options = ScanOptions()
        options.setPrompt("Scan a QR Code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        barcodeLauncher.launch(options)
    }

}