package com.alexandresamson.freelancereceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    // Holt alle Belege sortiert nach dem neuesten Datum.
    @Query("SELECT * FROM receipts ORDER BY timestamp DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    // Einzelnen Beleg per ID laden
    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    suspend fun getReceiptById(receiptId: Long): ReceiptEntity?

    // Löscht einen Beleg anhand seiner ID
    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: Long): Int
}