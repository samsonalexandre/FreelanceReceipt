package com.alexandresamson.freelancereceipt.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExportRepository(private val context: Context) {

    private val dateFormat    = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    private val exportDir get() = File(context.cacheDir, "exports").also { it.mkdirs() }

    // ── CSV ──────────────────────────────────────────────────────────────────

    suspend fun exportCsv(receipts: List<ReceiptEntity>): Intent =
        withContext(Dispatchers.IO) {
            val file = File(exportDir, "belege_${timestamp()}.csv")
            FileWriter(file).use { writer ->
                // BOM für korrekte UTF-8-Anzeige in Excel
                writer.write("\uFEFF")
                writer.write("Datum;Händler;Betrag (€);MwSt. (%);Kategorie\n")
                receipts.forEach { r ->
                    writer.write(
                        "${dateFormat.format(Date(r.timestamp))};" +
                                "\"${r.merchant.replace("\"", "\"\"")}\";" +
                                "${"%.2f".format(r.amountInCents / 100.0)};" +
                                "${r.taxRate.toInt()};" +
                                "${r.category}\n"
                    )
                }
                // Summenzeile
                val total = receipts.sumOf { it.amountInCents } / 100.0
                writer.write("\n;Gesamt;${"%.2f".format(total)};;\n")
            }
            buildShareIntent(file, "text/csv")
        }

    // ── PDF ──────────────────────────────────────────────────────────────────

    suspend fun exportPdf(receipts: List<ReceiptEntity>): Intent =
        withContext(Dispatchers.IO) {
            val pageWidth  = 595   // A4 in Points (72 dpi)
            val pageHeight = 842
            val margin     = 48f

            val document = PdfDocument()
            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            var canvas = page.canvas
            var y = margin

            // Farben & Pinsel
            val primaryColor  = Color.rgb(33, 97, 140)   // Dunkelblau
            val headerBg      = Color.rgb(235, 245, 255)
            val rowAltBg      = Color.rgb(248, 250, 252)
            val borderColor   = Color.rgb(189, 210, 230)
            val textDark      = Color.rgb(30, 30, 30)
            val textMuted     = Color.rgb(100, 110, 120)

            val paintFill     = Paint().apply { style = Paint.Style.FILL }
            val paintStroke   = Paint().apply {
                style = Paint.Style.STROKE
                color = borderColor
                strokeWidth = 0.5f
            }
            val paintText     = Paint().apply {
                isAntiAlias = true
                color = textDark
            }

            // ── Titelbereich ──
            paintFill.color = primaryColor
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 80f, paintFill)

            paintText.color = Color.WHITE
            paintText.textSize = 22f
            paintText.isFakeBoldText = true
            canvas.drawText("FreelanceReceipt — Belegliste", margin, 38f, paintText)

            paintText.textSize = 10f
            paintText.isFakeBoldText = false
            val exportDate = "Exportiert am ${dateFormat.format(Date())}"
            canvas.drawText(exportDate, margin, 58f, paintText)
            canvas.drawText("${receipts.size} Belege", margin, 70f, paintText)

            y = 100f

            // ── Tabellen-Header ──
            val colX     = floatArrayOf(margin, 148f, 300f, 390f, 460f)
            val colW     = floatArrayOf(100f,   152f,  90f,  70f, 135f)
            val headers  = arrayOf("Datum", "Händler", "Betrag", "MwSt.", "Kategorie")
            val rowH     = 22f

            paintFill.color = headerBg
            canvas.drawRect(margin, y, pageWidth - margin, y + rowH, paintFill)
            canvas.drawRect(margin, y, pageWidth - margin, y + rowH, paintStroke)

            paintText.color = primaryColor
            paintText.textSize = 9f
            paintText.isFakeBoldText = true
            headers.forEachIndexed { i, h ->
                canvas.drawText(h, colX[i] + 4f, y + 15f, paintText)
            }
            y += rowH

            // ── Tabellenzeilen ──
            paintText.isFakeBoldText = false
            paintText.color = textDark
            paintText.textSize = 8.5f

            receipts.forEachIndexed { index, receipt ->
                // Neue Seite falls nötig
                if (y + rowH > pageHeight - margin - 40f) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = margin
                }

                // Alternierende Zeilenfarbe
                if (index % 2 == 1) {
                    paintFill.color = rowAltBg
                    canvas.drawRect(margin, y, pageWidth - margin, y + rowH, paintFill)
                }
                canvas.drawRect(margin, y, pageWidth - margin, y + rowH, paintStroke)

                val cells = arrayOf(
                    dateFormat.format(Date(receipt.timestamp)),
                    receipt.merchant.take(24),
                    currencyFormat.format(receipt.amountInCents / 100.0),
                    "${receipt.taxRate.toInt()} %",
                    receipt.category
                )
                paintText.color = textDark
                cells.forEachIndexed { i, cell ->
                    // Betrag rechtsbündig
                    if (i == 2) {
                        val tw = paintText.measureText(cell)
                        canvas.drawText(cell, colX[i] + colW[i] - tw - 4f, y + 15f, paintText)
                    } else {
                        canvas.drawText(cell, colX[i] + 4f, y + 15f, paintText)
                    }
                }
                y += rowH
            }

            // ── Summenzeile ──
            y += 8f
            val total = receipts.sumOf { it.amountInCents } / 100.0
            val netTotal = receipts.sumOf { r ->
                r.amountInCents / (1 + r.taxRate / 100.0)
            } / 100.0
            val taxTotal = total - netTotal

            paintFill.color = primaryColor
            canvas.drawRect(margin, y, pageWidth - margin, y + rowH + 4f, paintFill)

            paintText.color = Color.WHITE
            paintText.isFakeBoldText = true
            paintText.textSize = 9f
            canvas.drawText("Gesamt (Brutto)", colX[1] + 4f, y + 16f, paintText)
            val totalStr = currencyFormat.format(total)
            canvas.drawText(totalStr,
                colX[2] + colW[2] - paintText.measureText(totalStr) - 4f,
                y + 16f, paintText)

            y += rowH + 12f

            // Netto / MwSt. Aufschlüsselung
            paintText.color = textMuted
            paintText.isFakeBoldText = false
            paintText.textSize = 8f
            canvas.drawText(
                "Netto: ${currencyFormat.format(netTotal)}   " +
                        "MwSt.: ${currencyFormat.format(taxTotal)}",
                colX[1] + 4f, y, paintText
            )

            // ── Fußzeile ──
            paintText.color = textMuted
            paintText.textSize = 7.5f
            canvas.drawText(
                "Erstellt mit FreelanceReceipt • Seite $pageNumber",
                margin, pageHeight - 20f, paintText
            )

            document.finishPage(page)

            // Datei schreiben
            val file = File(exportDir, "belege_${timestamp()}.pdf")
            file.outputStream().use { document.writeTo(it) }
            document.close()

            buildShareIntent(file, "application/pdf")
        }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    private fun buildShareIntent(file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun timestamp() =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}