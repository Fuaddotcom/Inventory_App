package com.TI23B1.inventoryapp.utils // Adjust package name

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import com.TI23B1.inventoryapp.utils.StickerPrintData
import android.util.TypedValue // Needed for dp to px conversion

import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PageRange
import android.os.ParcelFileDescriptor
import android.os.CancellationSignal
import android.os.Bundle
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

object PrintUtils {

    // Helper to convert dp to pixels
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    // Creates the PDF document for your sticker
    fun createStickerPdf(context: Context, data: StickerPrintData): PdfDocument {
        val document = PdfDocument()

        // Define your sticker size in points (1 point = 1/72 inch)
        // You'll need to know the exact dimensions of your sticker labels.
        // Let's assume a common label size, e.g., 4 inches wide x 2 inches tall
        // 4 inches * 72 points/inch = 288 points
        // 2 inches * 72 points/inch = 144 points
        val stickerWidthPoints = 288 // Example: 4 inches
        val stickerHeightPoints = 144 // Example: 2 inches

        // For A4 size (standard paper):
        // val pageInfo = PageInfo.Builder(PrintAttributes.MediaSize.ISO_A4.widthMils * 72 / 1000,
        //                                PrintAttributes.MediaSize.ISO_A4.heightMils * 72 / 1000, 1).create()

        // For a custom sticker size:
        val pageInfo = PageInfo.Builder(stickerWidthPoints, stickerHeightPoints, 1).create()

        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // --- Drawing parameters ---
        val margin = dpToPx(context, 10).toFloat() // 10dp margin
        var currentY = margin

        // Left side text block
        val textStartX = margin
        val qrCodeStartX = stickerWidthPoints / 2f + margin // Start QR code roughly half-way + margin

        paint.color = android.graphics.Color.BLACK
        paint.typeface = android.graphics.Typeface.DEFAULT // Default font

        // "Nama:" label
        paint.textSize = dpToPx(context, 14).toFloat() // Example text size
        canvas.drawText("Nama:", textStartX, currentY, paint)
        // Item Name value
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD // Make value bold
        canvas.drawText(data.itemName, textStartX + dpToPx(context, 60), currentY, paint) // Adjust X to align
        currentY += dpToPx(context, 20).toFloat() // Move down for next line

        // "Kode:" label
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Kode:", textStartX, currentY, paint)
        // Item Code value
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText(data.itemCode, textStartX + dpToPx(context, 60), currentY, paint)
        currentY += dpToPx(context, 20).toFloat()

        // "Qty:" label
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Qty:", textStartX, currentY, paint)
        // Quantity value
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText(data.quantity, textStartX + dpToPx(context, 60), currentY, paint)
        currentY += dpToPx(context, 20).toFloat()

        // "Tujuan:" label
        paint.typeface = android.graphics.Typeface.DEFAULT
        canvas.drawText("Tujuan:", textStartX, currentY, paint)
        // Destination value
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.drawText(data.destination, textStartX + dpToPx(context, 60), currentY, paint)
        // No need to advance currentY further for text, as QR code is on the right

        // --- Draw QR Code ---
        data.qrCodeBitmap?.let { qrBitmap ->
            // Scale QR code to fit the available space on the right, maintaining aspect ratio
            val maxQrWidth = (stickerWidthPoints / 2f) - (2 * margin) // Half page width minus margins
            val maxQrHeight = stickerHeightPoints - (2 * margin) // Full page height minus margins

            val scaledQrBitmap: Bitmap
            if (qrBitmap.width > maxQrWidth || qrBitmap.height > maxQrHeight) {
                // Scale down if too large
                val scale = Math.min(maxQrWidth / qrBitmap.width, maxQrHeight / qrBitmap.height)
                scaledQrBitmap = Bitmap.createScaledBitmap(qrBitmap, (qrBitmap.width * scale).toInt(), (qrBitmap.height * scale).toInt(), true)
            } else {
                // Use original if small enough
                scaledQrBitmap = qrBitmap
            }

            // Calculate position to center QR code vertically on the right half
            val qrCodeY = (stickerHeightPoints - scaledQrBitmap.height) / 2f
            canvas.drawBitmap(scaledQrBitmap, qrCodeStartX, qrCodeY, paint)
        }


        document.finishPage(page)
        return document
    }

    fun printPdfDocument(context: Context, document: PdfDocument, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(document.pages.size) // Number of pages in your PDF (usually 1 for a sticker)
                    .build()
                callback?.onLayoutFinished(info, newAttributes != oldAttributes)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                try {
                    document.writeTo(FileOutputStream(destination?.fileDescriptor))
                    callback?.onWriteFinished(pages)
                } catch (e: IOException) {
                    Log.e("PrintUtils", "Error writing PDF: ${e.message}", e)
                    callback?.onWriteFailed(e.toString())
                } finally {
                    document.close() // VERY IMPORTANT: Close the document after writing
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            printManager.print(jobName, printAdapter, null)
        } else {
            // Handle older Android versions if necessary
            Log.w("PrintUtils", "Printing not supported on API < 19")
        }
    }
}