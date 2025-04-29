package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

data class CargoInfo(
    @SerializedName("cargoId") val cargoId: String,
    @SerializedName("type") val type: String,
    @SerializedName("location") val location: String,
    @SerializedName("shelf") val shelf: String,
)

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
                val scannedData = result.contents
                try {
                    val gson = Gson()
                    val cargoInfo = gson.fromJson(scannedData, CargoInfo::class.java)
                    val intent = Intent(this, CargoDetailsActivity::class.java)


                    intent.putExtra("cargoId", cargoInfo.cargoId)
                    intent.putExtra("type", cargoInfo.type)
                    intent.putExtra("location", cargoInfo.location)
                    intent.putExtra("shelf", cargoInfo.shelf)



                    startActivity(intent)
                } catch(e: Exception){
                    Toast.makeText(this, "Invalid Cargo QR Code", Toast.LENGTH_LONG).show()
                }

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