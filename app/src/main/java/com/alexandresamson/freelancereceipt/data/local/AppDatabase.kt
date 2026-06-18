package com.alexandresamson.freelancereceipt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexandresamson.freelancereceipt.data.local.dao.ReceiptDao
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity

@Database(
    entities = [ReceiptEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}