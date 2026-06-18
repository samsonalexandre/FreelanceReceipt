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

    private val BRUTTO_PATTERNS = listOf(
        Regex("""brutto[:\s]*(\d{1,4}[.,]\d{2})""", RegexOption.IGNORE_CASE),
        Regex("""gesant[^\d]*(\d{1,4}[.,]\d{2})""", RegexOption.IGNORE_CASE),
        Regex("""(?:summe|total|gesamt|endbetrag|zahlung|zu.zahlen)[^\d]*(\d{1,4}[.,]\d{2})""",
            RegexOption.IGNORE_CASE),
    )

    private val BRUTTO_LABEL_PATTERN = Regex(
        """^brutto$""", RegexOption.IGNORE_CASE
    )

    private val TAX_PATTERNS = listOf(
        Regex("""[A-Z]:\s*(\d{1,2})[.,]\d{2}\s*%"""),
        Regex("""(\d{1,2})\s*%\s*(?:mwst|ust|vat|tax)""", RegexOption.IGNORE_CASE),
        Regex("""(?:mwst|ust|vat|tax)[^\d]*(\d{1,2})\s*%""", RegexOption.IGNORE_CASE)
    )

    private val DATE_PATTERNS = listOf(
        Regex("""(\d{2})[./\-](\d{2})[./\-](\d{4})"""),
        Regex("""(\d{2})[./\-](\d{2})[./\-](\d{2})"""),
        Regex("""(\d{4})[./\-](\d{2})[./\-](\d{2})""")
    )

    private val CATEGORY_KEYWORDS = mapOf(
        Category.TRAVEL.dbKey to listOf(
            "shell", "aral", "bp", "esso", "tankstelle", "station", "sb station",
            "superbenzin", "benzin", "diesel", "kraftstoff", "tanken",
            "bahn", "db ", "taxi", "uber", "parking", "zapfsäule", "zp "
        ),
        Category.GROCERIES.dbKey to listOf(
            "rewe", "edeka", "aldi", "lidl", "penny", "netto",
            "bäcker", "metzger", "markt", "supermarkt"
        ),
        Category.OFFICE.dbKey to listOf(
            "staples", "bürobedarf", "schreibwaren", "canon", "epson", "drucker"
        ),
        Category.RESTAURANT.dbKey to listOf(
            "restaurant", "café", "cafe", "bistro", "pizza", "burger",
            "mcdonald", "subway", "döner"
        ),
        Category.SOFTWARE.dbKey to listOf(
            "amazon", "google", "apple", "microsoft", "adobe", "github", "digitalocean"
        )
    )

    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lowerText = rawText.lowercase()

        return ParsedReceipt(
            merchant      = extractMerchant(lines),
            amountInCents = extractAmount(lines, lowerText),
            taxRate       = extractTaxRate(lowerText),
            timestampMs   = extractTimestamp(rawText),
            category      = detectCategory(lowerText, extractMerchant(lines))
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
        for (pattern in BRUTTO_PATTERNS) {
            val match = pattern.find(lowerText) ?: continue
            val raw = match.groupValues[1].replace(",", ".")
            val value = raw.toDoubleOrNull() ?: continue
            if (value > 0) return (value * 100).toLong()
        }

        for (i in lines.indices) {
            if (BRUTTO_LABEL_PATTERN.matches(lines[i])) {
                for (j in (i + 1)..minOf(i + 3, lines.lastIndex)) {
                    val amountMatch = Regex("""^(\d{1,4}[.,]\d{2})$""").find(lines[j].trim())
                    if (amountMatch != null) {
                        val value = amountMatch.groupValues[1].replace(",", ".").toDoubleOrNull()
                        if (value != null && value > 0) return (value * 100).toLong()
                    }
                }
            }
        }

        val eurPattern = Regex("""(\d{1,4}[.,]\d{2})\s*EUR""", RegexOption.IGNORE_CASE)
        val eurMatches = eurPattern.findAll(lowerText)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .filter { it > 0 }
            .toList()
        if (eurMatches.isNotEmpty()) {
            return (eurMatches.max() * 100).toLong()
        }

        val allAmounts = Regex("""(\d{1,4}[.,]\d{2})""").findAll(lowerText)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .filter { it > 0.5 }
            .toList()
        return ((allAmounts.maxOrNull() ?: 0.0) * 100).toLong()
    }

    private fun extractTaxRate(lowerText: String): Double {
        for (pattern in TAX_PATTERNS) {
            val match = pattern.find(lowerText) ?: continue
            val rate = match.groupValues[1].toDoubleOrNull() ?: continue
            if (rate in 1.0..30.0) return rate
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
                    groups[1].length == 4 -> cal.set(
                        groups[1].toInt(), groups[2].toInt() - 1, groups[3].toInt()
                    )
                    groups[3].length == 4 -> cal.set(
                        groups[3].toInt(), groups[2].toInt() - 1, groups[1].toInt()
                    )
                    else -> cal.set(
                        groups[3].toInt() + 2000, groups[2].toInt() - 1, groups[1].toInt()
                    )
                }
                cal.timeInMillis
            } catch (e: Exception) {
                continue
            }
        }
        return System.currentTimeMillis()
    }

    private fun detectCategory(lowerText: String, merchant: String): String {
        val combined = lowerText + merchant.lowercase()
        return CATEGORY_KEYWORDS.entries
            .firstOrNull { (_, keywords) -> keywords.any { combined.contains(it) } }
            ?.key
            ?: Category.OTHER.dbKey
    }
}
