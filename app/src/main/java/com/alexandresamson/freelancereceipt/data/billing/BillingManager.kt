package com.alexandresamson.freelancereceipt.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Play Billing client wrapper.
 *
 *  Product type: INAPP (non-consumable, one-time)
 *  Product id  : "premium_lifetime" (must be configured in Play Console)
 *  Price       : €6.99 (set in Play Console — never hardcoded in code)
 *
 * On startup it connects, queries the product details, and queries
 * existing purchases (so re-installs restore Premium automatically).
 * UI observes [premiumPriceLabel] and [billingError]; PaywallScreen
 * calls [launchPurchase] from a user gesture.
 */
class BillingManager(
    private val context: Context,
    private val prefs: PreferencesManager
) : PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val PRODUCT_ID_PREMIUM = "premium_lifetime"
        private const val TAG = "BillingManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    private val _premiumPriceLabel = MutableStateFlow<String?>(null)
    val premiumPriceLabel: StateFlow<String?> = _premiumPriceLabel.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var productDetails: ProductDetails? = null

    fun connect() {
        if (billingClient.isReady) return
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            _isReady.value = true
            queryProductDetails()
            queryExistingPurchases()
        } else {
            Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
            _billingError.value = "Billing unavailable: ${result.debugMessage}"
        }
    }

    override fun onBillingServiceDisconnected() {
        _isReady.value = false
        // Auto-reconnect handled by next connect() call from user gesture
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID_PREMIUM)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = list.firstOrNull()
                _premiumPriceLabel.value =
                    productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
            } else {
                Log.e(TAG, "queryProductDetails failed: ${result.debugMessage}")
            }
        }
    }

    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (details == null) {
            _billingError.value = "Product not loaded yet — please retry."
            queryProductDetails()
            return
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun restorePurchases() {
        queryExistingPurchases()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.d(TAG, "User canceled purchase")
            else ->
                _billingError.value = "Purchase failed: ${result.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (PRODUCT_ID_PREMIUM !in purchase.products) return

        // Acknowledge non-consumable, non-acknowledged purchase
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    grantPremium()
                }
            }
        } else {
            grantPremium()
        }
    }

    private fun grantPremium() {
        scope.launch { prefs.setPremium(true) }
    }

    fun consumeError() {
        _billingError.value = null
    }
}
