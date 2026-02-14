package com.l9rins.trademate

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.l9rins.trademate.data.Job
import java.io.File
import java.io.FileOutputStream

object PDFGenerator {

    fun generateAndShareInvoice(context: Context, job: Job) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Header
        paint.color = android.graphics.Color.rgb(0, 128, 128) // Teal
        paint.textSize = 40f
        paint.isFakeBoldText = true
        canvas.drawText("INVOICE", 40f, 80f, paint)

        // 2. Company Info
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = false
        canvas.drawText("TradeMate Services", 40f, 120f, paint)
        canvas.drawText("Professional Services", 40f, 145f, paint)

        // 3. Divider
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 180f, 555f, 180f, paint)

        // 4. Client
        paint.textSize = 16f
        canvas.drawText("BILL TO:", 40f, 220f, paint)
        paint.isFakeBoldText = true
        canvas.drawText(job.clientName, 40f, 250f, paint)

        // 5. Box
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(40f, 300f, 555f, 400f, paint)

        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = false
        canvas.drawText("Description", 50f, 330f, paint)
        canvas.drawText("Amount", 450f, 330f, paint)

        paint.isFakeBoldText = true
        canvas.drawText(job.title, 50f, 370f, paint)
        canvas.drawText("$${job.price}", 450f, 370f, paint)

        // 6. Total
        paint.textSize = 24f
        paint.color = android.graphics.Color.rgb(0, 200, 83)
        canvas.drawText("TOTAL DUE: $${job.price}", 350f, 500f, paint)

        pdfDocument.finishPage(page)

        // 7. Save & Share
        val file = File(context.cacheDir, "Invoice_${job.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }
}