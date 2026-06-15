package com.alexandresamson.freelancereceipt.domain

import java.util.Calendar

data class ParsedReceipt(
    val merchant: String,
    val amountInCents: Long,
    val taxRate: Double,
    val timestampMs: Long,
    val category: String
)

object ReceiptParser {

    // Regex-Muster für deutsche/europäische Belege
    private val AMOUNT_PATTERNS = listOf(
        // "SUMME 12,99 €", "Total: 8.50 EUR", "Gesamt 4,20"
        Regex("""(?:summe|total|gesamt|betrag|endbetrag|zahlung|bezahlt)[^\d]*(\d{1,4}[.,]\d{2})""",
            RegexOption.IGNORE_CASE),
        // Fallback: größter Betrag im Text
        Regex("""(\d{1,4}[.,]\d{2})\s*[€eEuUrR]""")
    )

    private val TAX_PATTERNS = listOf(
        Regex("""(\d{1,2})\s*%\s*(?:mwst|ust|vat|tax)""", RegexOption.IGNORE_CASE),
        Regex("""(?:mwst|ust|vat|tax)[^\d]*(\d{1,2})\s*%""", RegexOption.IGNORE_CASE)
    )

    private val DATE_PATTERNS = listOf(
        Regex("""(\d{2})[./\-](\d{2})[./\-](\d{2,4})"""),   // 24.12.2024 oder 24/12/24
        Regex("""(\d{4})[./\-](\d{2})[./\-](\d{2})""")       // 2024-12-24
    )

    // Einfache Kategorie-Erkennung anhand von Keywords
    private val CATEGORY_KEYWORDS = mapOf(
        "Lebensmittel"  to listOf("rewe", "edeka", "aldi", "lidl", "penny", "netto",
            "bäcker", "metzger", "markt"),
        "Fahrtkosten"   to listOf("shell", "aral", "bp", "esso", "tankstelle",
            "bahn", "db ", "taxi", "uber", "parking"),
        "Büro"          to listOf("staples", "bürobedarf", "schreibwaren",
            "canon", "epson", "drucker"),
        "Restaurant"    to listOf("restaurant", "café", "cafe", "bistro",
            "pizza", "burger", "mcdonald", "subway"),
        "Software"      to listOf("amazon", "google", "apple", "microsoft",
            "adobe", "github", "digitalocean")
    )

    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lowerText = rawText.lowercase()

        val merchant  = extractMerchant(lines)
        val amount    = extractAmount(lowerText)
        val taxRate   = extractTaxRate(lowerText)
        val timestamp = extractTimestamp(rawText)
        val category  = detectCategory(lowerText, merchant)

        return ParsedReceipt(
            merchant      = merchant,
            amountInCents = amount,
            taxRate       = taxRate,
            timestampMs   = timestamp,
            category      = category
        )
    }

    // Erste nicht-leere Zeile = meist Händlername
    private fun extractMerchant(lines: List<String>): String =
        lines.firstOrNull { it.length > 2 && !it.all { c -> c.isDigit() || c in ".,:-/" } }
            ?.take(40)
            ?: "Unbekannt"

    private fun extractAmount(text: String): Long {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues[1].replace(",", ".")
            return (raw.toDoubleOrNull() ?: continue).let { (it * 100).toLong() }
        }
        // Fallback: größten Betrag im Text nehmen
        val allAmounts = Regex("""(\d{1,4}[.,]\d{2})""").findAll(text)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .toList()
        return ((allAmounts.maxOrNull() ?: 0.0) * 100).toLong()
    }

    private fun extractTaxRate(text: String): Double {
        for (pattern in TAX_PATTERNS) {
            val match = pattern.find(text) ?: continue
            return match.groupValues[1].toDoubleOrNull() ?: continue
        }
        return 19.0 // Deutscher Standard-MwSt-Satz
    }

    private fun extractTimestamp(text: String): Long {
        for (pattern in DATE_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val groups = match.groupValues
            return try {
                val cal = Calendar.getInstance()
                if (groups[1].length == 4) {
                    // Format YYYY-MM-DD
                    cal.set(groups[1].toInt(), groups[2].toInt() - 1, groups[3].toInt())
                } else {
                    // Format DD.MM.YYYY oder DD.MM.YY
                    val year = groups[3].toInt().let { if (it < 100) it + 2000 else it }
                    cal.set(year, groups[2].toInt() - 1, groups[1].toInt())
                }
                cal.timeInMillis
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
        return System.currentTimeMillis()
    }

    private fun detectCategory(lowerText: String, merchant: String): String {
        val combined = lowerText + merchant.lowercase()
        return CATEGORY_KEYWORDS.entries
            .firstOrNull { (_, keywords) -> keywords.any { combined.contains(it) } }
            ?.key
            ?: "Sonstiges"
    }
}