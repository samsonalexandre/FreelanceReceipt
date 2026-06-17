package com.alexandresamson.freelancereceipt.data.repository

import com.alexandresamson.freelancereceipt.data.local.dao.ReceiptDao
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.alexandresamson.freelancereceipt.domain.ReceiptStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptRepository(private val dao: ReceiptDao) {

    fun getAllReceipts(): Flow<List<ReceiptEntity>> = dao.getAllReceipts()

    suspend fun getReceiptById(id: Long): ReceiptEntity? = dao.getReceiptById(id)

    suspend fun saveReceipt(receipt: ReceiptEntity) = dao.insertReceipt(receipt)

    suspend fun deleteReceipt(id: Long) = dao.deleteReceiptById(id)

    suspend fun getStats(): ReceiptStats {
            // getAllReceipts() gibt einen Flow zurück — wir brauchen hier einen Snapshot.
            // first() nimmt genau einen Wert aus dem Flow und beendet die Subscription.
            val receipts = dao.getAllReceipts().first()

            if (receipts.isEmpty()) {
                return ReceiptStats(0L, 0L, 0L, 0, emptyMap(), emptyMap())
            }

            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

            var totalBrutto = 0L
            val byCategory  = mutableMapOf<String, Long>()
            val byMonth     = mutableMapOf<String, Long>()

            for (receipt in receipts) {
                val brutto = receipt.amountInCents

                // Gesamtsumme
                totalBrutto += brutto

                // Nach Kategorie gruppieren
                byCategory[receipt.category] =
                    (byCategory[receipt.category] ?: 0L) + brutto

                // Nach Monat gruppieren
                val monthKey = monthFormat.format(Date(receipt.timestamp))
                byMonth[monthKey] = (byMonth[monthKey] ?: 0L) + brutto
            }

            // Netto und MwSt werden aus den Einzelbelegen akkumuliert
            // (nicht aus dem Brutto berechnet, da verschiedene MwSt-Sätze möglich)
            val totalNetto = receipts.sumOf { r ->
                (r.amountInCents / (1 + r.taxRate / 100.0)).toLong()
            }
            val totalTax = totalBrutto - totalNetto

            return ReceiptStats(
                totalBruttoInCents = totalBrutto,
                totalNettoInCents  = totalNetto,
                totalTaxInCents    = totalTax,
                receiptCount       = receipts.size,
                // Kategorien nach Betrag absteigend sortieren → für das Tortendiagramm
                byCategory = byCategory.entries
                    .sortedByDescending { it.value }
                    .associate { it.key to it.value },
                // Monate chronologisch sortieren → für das Balkendiagramm
                byMonth = byMonth.entries
                    .sortedBy { it.key }
                    .associate { it.key to it.value }
            )
        }
    }
