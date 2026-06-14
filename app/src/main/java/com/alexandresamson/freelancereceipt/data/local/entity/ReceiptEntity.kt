package com.alexandresamson.freelancereceipt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val merchant: String,
    val amountInCents: Long, // Cent-Beträge, um Rundungsfehler zu vermeiden
    val taxRate: Double,
    val category: String,
    val isPremiumFeature: Boolean = false
)
