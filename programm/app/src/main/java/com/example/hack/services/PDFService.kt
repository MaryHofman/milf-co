package com.example.hack.services

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint


import java.io.File



fun createPdfFromResponse(context: Context, responseBody: String): File? {
    return try {
        val pdfFile = File(context.getExternalFilesDir(null), "report_${System.currentTimeMillis()}.pdf")
        val document = android.graphics.pdf.PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val lines = responseBody.split("\n")
        var y = 40
        for (line in lines) {
            if (y > 800) {
                document.finishPage(page)
                y = 40
            }
            canvas.drawText(line, 40f, y.toFloat(), paint)
            y += 20
        }

        document.finishPage(page)
        pdfFile.outputStream().use {
            document.writeTo(it)
        }
        document.close()
        pdfFile
    } catch (e: Exception) {
        null
    }
}




