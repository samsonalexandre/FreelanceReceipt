package com.alexandresamson.freelancereceipt

import android.app.Application
import com.alexandresamson.freelancereceipt.data.billing.BillingManager
import com.alexandresamson.freelancereceipt.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FreelanceReceiptApp : Application() {

    private val billingManager: BillingManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FreelanceReceiptApp)
            modules(appModule)
        }
        // Connect billing client on app start so price is ready
        // by the time the user reaches the Paywall.
        billingManager.connect()
    }
}
