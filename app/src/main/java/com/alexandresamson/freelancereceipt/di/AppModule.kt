package com.alexandresamson.freelancereceipt.di

import androidx.room.Room
import com.alexandresamson.freelancereceipt.data.local.AppDatabase
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.ui.dashboard.ReceiptViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.google.firebase.auth.FirebaseAuth
import com.alexandresamson.freelancereceipt.ui.auth.AuthViewModel
import com.alexandresamson.freelancereceipt.ui.addreceipt.AddReceiptViewModel

val appModule = module {

    // Room-Datenbank als Singleton
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "freelance_receipt_db"
        ).build()
    }

    // DAO aus der DB holen
    single { get<AppDatabase>().receiptDao() }

    // Repository mit DAO erstellen
    single { ReceiptRepository(get()) }

    // ViewModel mit Repository
    viewModel { ReceiptViewModel(get()) }

    // Firebase Auth als Singleton
    single { FirebaseAuth.getInstance() }

    // AuthViewModel
    viewModel { AuthViewModel(get()) }

    viewModel { AddReceiptViewModel(get(), get()) }
}