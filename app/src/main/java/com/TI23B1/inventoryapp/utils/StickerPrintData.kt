package com.TI23B1.inventoryapp.utils

import android.graphics.Bitmap

data class StickerPrintData(
    val itemName: String,
    val itemCode: String,
    val quantity: String, // Use String for "200(PCS)"
    val destination: String,
    val qrCodeBitmap: Bitmap?
)

