package com.alexandresamson.freelancereceipt

import android.app.Application
import com.alexandresamson.freelancereceipt.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FreelanceReceiptApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FreelanceReceiptApp)
            modules(appModule)
        }
    }
}