package com.alexandresamson.freelancereceipt.di

import androidx.room.Room
import com.alexandresamson.freelancereceipt.data.billing.BillingManager
import com.alexandresamson.freelancereceipt.data.local.AppDatabase
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import com.alexandresamson.freelancereceipt.data.repository.ExportRepository
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.ui.addreceipt.AddReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.auth.AuthViewModel
import com.alexandresamson.freelancereceipt.ui.dashboard.ReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.detail.DetailReceiptViewModel
import com.alexandresamson.freelancereceipt.ui.export.ExportViewModel
import com.alexandresamson.freelancereceipt.ui.paywall.PaywallViewModel
import com.alexandresamson.freelancereceipt.ui.settings.SettingsViewModel
import com.alexandresamson.freelancereceipt.ui.stats.StatsViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "freelance_receipt_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().receiptDao() }

    single { ReceiptRepository(get()) }
    single { ExportRepository(androidApplication()) }
    single { PreferencesManager(androidContext()) }
    single { BillingManager(androidContext(), get()) }

    single { FirebaseAuth.getInstance() }

    viewModel { ReceiptViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { AddReceiptViewModel(get(), get(), androidApplication()) }
    viewModel { ExportViewModel(get(), get()) }
    viewModel { DetailReceiptViewModel(get(), androidApplication()) }
    viewModel { StatsViewModel(get()) }
    viewModel { PaywallViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
