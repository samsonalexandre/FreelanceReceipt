package com.alexandresamson.freelancereceipt.data.repository

import com.alexandresamson.freelancereceipt.data.local.dao.ReceiptDao
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

class ReceiptRepository(private val dao: ReceiptDao) {

    // Der ViewModel bekommt immer einen aktuellen Flow – nie veraltete Daten
    fun getAllReceipts(): Flow<List<ReceiptEntity>> = dao.getAllReceipts()

    suspend fun saveReceipt(receipt: ReceiptEntity) = dao.insertReceipt(receipt)

    suspend fun deleteReceipt(id: Long) = dao.deleteReceiptById(id)
}