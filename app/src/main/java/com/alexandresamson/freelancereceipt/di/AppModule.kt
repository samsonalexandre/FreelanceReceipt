package com.alexandresamson.freelancereceipt.di

import androidx.room.Room
import com.alexandresamson.freelancereceipt.data.local.AppDatabase
import com.alexandresamson.freelancereceipt.data.repository.ExportRepository
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.ui.addreceipt.AddReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.auth.AuthViewModel
import com.alexandresamson.freelancereceipt.ui.dashboard.ReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.detail.DetailReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.export.ExportViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.alexandresamson.freelancereceipt.ui.stats.StatsViewModel

val appModule = module {

    // Room-Datenbank
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "freelance_receipt_db"
        ).build()
    }

    // DAO
    single { get<AppDatabase>().receiptDao() }

    // Repositories
    single { ReceiptRepository(get()) }
    single { ExportRepository(androidApplication()) } // androidApplication() statt androidContext()

    // Firebase
    single { FirebaseAuth.getInstance() }

    // ViewModels
    viewModel { ReceiptViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { AddReceiptViewModel(get(), get()) }
    viewModel { ExportViewModel(get(), get()) }
    viewModel { DetailReceiptViewModel(get()) }
    viewModel { StatsViewModel(get()) }
}