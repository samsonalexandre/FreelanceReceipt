package com.alexandresamson.freelancereceipt.domain

data class ReceiptStats(
    val totalBruttoInCents: Long,
    val totalNettoInCents: Long,
    val totalTaxInCents: Long,
    val receiptCount: Int,
    val byCategory: Map<String, Long>,
    val byMonth: Map<String, Long>
)
