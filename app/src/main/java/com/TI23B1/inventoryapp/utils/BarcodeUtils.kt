// In BarcodeUtils.kt (or similar utility file)
package com.TI23B1.inventoryapp.utils // Adjust package name

import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

object BarcodeUtils { // Using an object for utility functions

    fun generateQrCodeBitmap(dataToEncode: String, width: Int = 400, height: Int = 400): Bitmap? {
        try {
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.encodeBitmap(
                dataToEncode,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
        } catch (e: Exception) {
            Log.e("BarcodeUtils", "Error generating QR code: ${e.message}", e)
            return null
        }
    }
}