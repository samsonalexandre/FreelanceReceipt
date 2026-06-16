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

    // Brutto-Muster — sucht nach "Brutto" gefolgt von einem Betrag
    // auf derselben ODER der nächsten Zeile
    private val BRUTTO_PATTERNS = listOf(
        // "Brutto 20,03" oder "Brutto: 20,03" auf einer Zeile
        Regex("""brutto[:\s]*(\d{1,4}[.,]\d{2})""", RegexOption.IGNORE_CASE),
        // "Gesantoetrag" (OCR-Fehler für "Gesamtbetrag") gefolgt von Betrag
        Regex("""gesant[^\d]*(\d{1,4}[.,]\d{2})""", RegexOption.IGNORE_CASE),
        // Standard Summen-Keywords
        Regex("""(?:summe|total|gesamt|endbetrag|zahlung|zu.zahlen)[^\d]*(\d{1,4}[.,]\d{2})""",
            RegexOption.IGNORE_CASE),
    )

    // Wenn Brutto-Label auf einer Zeile steht und der Betrag auf der nächsten
    private val BRUTTO_LABEL_PATTERN = Regex(
        """^brutto$""", RegexOption.IGNORE_CASE
    )

    private val TAX_PATTERNS = listOf(
        // "A:19,00%" Format wie auf diesem Bon
        Regex("""[A-Z]:\s*(\d{1,2})[.,]\d{2}\s*%"""),
        Regex("""(\d{1,2})\s*%\s*(?:mwst|ust|vat|tax)""", RegexOption.IGNORE_CASE),
        Regex("""(?:mwst|ust|vat|tax)[^\d]*(\d{1,2})\s*%""", RegexOption.IGNORE_CASE)
    )

    private val DATE_PATTERNS = listOf(
        Regex("""(\d{2})[./\-](\d{2})[./\-](\d{4})"""),  // 15.06.2026
        Regex("""(\d{2})[./\-](\d{2})[./\-](\d{2})"""),  // 15.06.26
        Regex("""(\d{4})[./\-](\d{2})[./\-](\d{2})""")   // 2026-06-15
    )

    private val CATEGORY_KEYWORDS = mapOf(
        "Fahrtkosten" to listOf(
            "shell", "aral", "bp", "esso", "tankstelle", "station", "sb station",
            "superbenzin", "benzin", "diesel", "kraftstoff", "tanken",
            "bahn", "db ", "taxi", "uber", "parking", "zapfsäule", "zp "
        ),
        "Lebensmittel" to listOf(
            "rewe", "edeka", "aldi", "lidl", "penny", "netto",
            "bäcker", "metzger", "markt", "supermarkt"
        ),
        "Büro" to listOf(
            "staples", "bürobedarf", "schreibwaren", "canon", "epson", "drucker"
        ),
        "Restaurant" to listOf(
            "restaurant", "café", "cafe", "bistro", "pizza", "burger",
            "mcdonald", "subway", "döner"
        ),
        "Software" to listOf(
            "amazon", "google", "apple", "microsoft", "adobe", "github", "digitalocean"
        )
    )

    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lowerText = rawText.lowercase()

        val merchant  = extractMerchant(lines)
        val amount    = extractAmount(lines, lowerText)
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

    private fun extractMerchant(lines: List<String>): String =
        lines.firstOrNull {
            it.length > 2
                    && !it.all { c -> c.isDigit() || c in ".,:-/" }
                    && !it.startsWith("Beleg")
                    && !it.startsWith("Tel")
                    && !it.startsWith("Ubj")
        }?.take(40) ?: "Unbekannt"

    private fun extractAmount(lines: List<String>, lowerText: String): Long {
        // Strategie 1: Brutto-Keyword auf gleicher Zeile
        for (pattern in BRUTTO_PATTERNS) {
            val match = pattern.find(lowerText) ?: continue
            val raw = match.groupValues[1].replace(",", ".")
            val value = raw.toDoubleOrNull() ?: continue
            if (value > 0) return (value * 100).toLong()
        }

        // Strategie 2: "Brutto" auf einer Zeile, Betrag auf der nächsten
        for (i in lines.indices) {
            if (BRUTTO_LABEL_PATTERN.matches(lines[i])) {
                // Nächste Zeile die einen Betrag enthält
                for (j in (i + 1)..minOf(i + 3, lines.lastIndex)) {
                    val amountMatch = Regex("""^(\d{1,4}[.,]\d{2})$""").find(lines[j].trim())
                    if (amountMatch != null) {
                        val value = amountMatch.groupValues[1].replace(",", ".").toDoubleOrNull()
                        if (value != null && value > 0) return (value * 100).toLong()
                    }
                }
            }
        }

        // Strategie 3: "20,03 EUR" Format (Betrag gefolgt von EUR)
        val eurPattern = Regex("""(\d{1,4}[.,]\d{2})\s*EUR""", RegexOption.IGNORE_CASE)
        val eurMatches = eurPattern.findAll(lowerText)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .filter { it > 0 }
            .toList()
        if (eurMatches.isNotEmpty()) {
            // Nimm den größten EUR-Betrag (wahrscheinlich Brutto)
            return (eurMatches.max() * 100).toLong()
        }

        // Strategie 4: Fallback — größter Betrag im Text
        val allAmounts = Regex("""(\d{1,4}[.,]\d{2})""").findAll(lowerText)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .filter { it > 0.5 } // Cent-Beträge rausfiltern
            .toList()
        return ((allAmounts.maxOrNull() ?: 0.0) * 100).toLong()
    }

    private fun extractTaxRate(lowerText: String): Double {
        for (pattern in TAX_PATTERNS) {
            val match = pattern.find(lowerText) ?: continue
            val rate = match.groupValues[1].toDoubleOrNull() ?: continue
            if (rate in 1.0..30.0) return rate // Plausibilitätsprüfung
        }
        return 19.0
    }

    private fun extractTimestamp(text: String): Long {
        for (pattern in DATE_PATTERNS) {
            val match = pattern.find(text) ?: continue
            val groups = match.groupValues
            return try {
                val cal = Calendar.getInstance()
                when {
                    // YYYY-MM-DD
                    groups[1].length == 4 -> cal.set(
                        groups[1].toInt(),
                        groups[2].toInt() - 1,
                        groups[3].toInt()
                    )
                    // DD.MM.YYYY
                    groups[3].length == 4 -> cal.set(
                        groups[3].toInt(),
                        groups[2].toInt() - 1,
                        groups[1].toInt()
                    )
                    // DD.MM.YY
                    else -> cal.set(
                        groups[3].toInt() + 2000,
                        groups[2].toInt() - 1,
                        groups[1].toInt()
                    )
                }
                cal.timeInMillis
            } catch (e: Exception) {
                continue // nächstes Pattern versuchen
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