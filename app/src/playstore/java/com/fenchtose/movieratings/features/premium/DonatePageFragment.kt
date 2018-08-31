package com.fenchtose.movieratings.features.premium

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.billingclient.api.*
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.HistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.PreferenceUserHistory
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.google.android.gms.analytics.ecommerce.ProductAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DonatePageFragment: BaseFragment(), PurchasesUpdatedListener, InAppPurchaseCard.Callback {

    private var billingClient: BillingClient? = null
    private var skuContainer: LinearLayout? = null
    private var progressContainer: View? = null
    private val cards: ArrayList<InAppPurchaseCard> = ArrayList()

    private var historyKeeper: HistoryKeeper? = null

//    private var currentPurchase: SkuDetails? = null

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.donate_page_title

    override fun screenName() = GaScreens.SUPPORT_APP

    private var isBillingAvailable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.donate_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingStore = DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao())

        ratingStore.getUniqueRatingsCount()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .filter { it >= 10 }.map { it - it % 10 }.subscribe({
                    view.findViewById<TextView>(R.id.persuasion_message).apply {
                        text = requireContext().getString(R.string.donate_page_persuasion_message, it)
                        visibility = View.VISIBLE
                    }
                }, {

                })

        progressContainer = view.findViewById(R.id.progress_container)
        skuContainer = view.findViewById(R.id.sku_container)
        historyKeeper = DbHistoryKeeper(
                PreferenceUserHistory(requireContext()),
                ratingStore,
                SettingsPreferences(requireContext())
                )

        billingClient = BillingClient.newBuilder(requireContext()).setListener(this).build()
        billingClient?.startConnection(object: BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(@BillingClient.BillingResponse responseCode: Int) {
                isBillingAvailable = responseCode == BillingClient.BillingResponse.OK
                queryAvailablePurchases()
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (billingClient?.isReady == true) {
            billingClient?.endConnection()
        }
    }

    override fun onPurchasesUpdated(@BillingClient.BillingResponse responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null && !purchases.isEmpty()) {
            historyKeeper?.paidInAppPurchase()
            GaEvents.PURCHASED.withLabelArg(purchases.first().sku)

            AlertDialog.Builder(context)
                    .setTitle(R.string.donate_dialog_title)
                    .setMessage(R.string.donate_dialog_message)
                    .setPositiveButton(android.R.string.ok){ dialog, _ ->  dialog.dismiss() }
                    .show()

            queryAvailablePurchases()
        }
    }

    private fun queryPurchaseHistory(skus: List<SkuDetails>) {
        if (isDetached || !isAdded) {
            return
        }

        billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {
            _, purchases -> showDetails(skus, purchases)
        }
    }

    private fun queryAvailablePurchases() {
        if (isDetached || !isAdded) {
            return
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(arrayListOf("donate_small", "donate_medium", "donate_large"))
        params.setType(BillingClient.SkuType.INAPP)
        billingClient?.querySkuDetailsAsync(params.build()) {
            responseCode, skuDetails ->
                if (responseCode == BillingClient.BillingResponse.OK && skuDetails != null && skuDetails.isNotEmpty()) {
                    queryPurchaseHistory(skuDetails)
                }

                if (BuildConfig.DEBUG) {
                    val list = arrayListOf(
                            SkuDetails("{\"productId\":\"donate_large\",\"type\":\"inapp\",\"price\":\"€5,49\",\"price_amount_micros\":5490000,\"price_currency_code\":\"EUR\",\"title\":\"Buy me lunch! (Flutter - Movie Ratings)\",\"description\":\"Buy a small lunch for the developer.\"}"),
                            SkuDetails("{\"productId\":\"donate_medium\",\"type\":\"inapp\",\"price\":\"€2,99\",\"price_amount_micros\":2990000,\"price_currency_code\":\"EUR\",\"title\":\"Buy me beer! (Flutter - Movie Ratings)\",\"description\":\"Buy a beer for the developer.\"}"),
                            SkuDetails("{\"productId\":\"donate_small\",\"type\":\"inapp\",\"price\":\"€0,99\",\"price_amount_micros\":990000,\"price_currency_code\":\"EUR\",\"title\":\"Buy me coffee! (Flutter - Movie Ratings)\",\"description\":\"Buy a cup of coffee for the developer.\"}")
                    )
                    queryPurchaseHistory(list)
                }
        }
    }

    private fun showDetails(skus: List<SkuDetails>, purchases: List<Purchase>?) {

        if (isDetached || !isAdded) {
            return
        }

        progressContainer?.visibility = View.GONE

        val margin = (requireContext().resources.displayMetrics.density * 8).toInt()

        cards.clear()
        skuContainer?.removeAllViews()

        val pSkus = purchases?.map { it.sku } ?: ArrayList()

        if (pSkus.isNotEmpty()) {
            historyKeeper?.paidInAppPurchase()
        }

        for (sku in skus.sortedBy { it.priceAmountMicros }) {
            val view = InAppPurchaseCard(requireContext())
            view.sku = sku
            view.bought = pSkus.contains(sku.sku)
            view.callback = this
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(margin, margin, margin, margin)
            skuContainer?.addView(view, params)
            cards.add(view)
        }
    }

    override fun onPurchaseRequested(sku: SkuDetails) {
        GaEvents.TAP_PURCHASE.withLabelArg(sku.sku).track()

        val flowParams = BillingFlowParams.newBuilder()
                .setSku(sku.sku)
                .setType(BillingClient.SkuType.INAPP)
                .build()

//        currentPurchase = sku
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    class DonatePath: RouterPath<DonatePageFragment>() {
        override fun createFragmentInstance() = DonatePageFragment()
        override fun category() = GaCategory.SUPPORT_APP

        companion object {

            val KEY = "DonatePath"

            fun createExtras(): Bundle {
                val bundle = Bundle()
                bundle.putString(Router.ROUTE_TO_SCREEN, KEY)
                return bundle
            }

            fun createPath(): ((Bundle) -> RouterPath<out BaseFragment>) {
                return ::createPath
            }

            private fun createPath(extras: Bundle): RouterPath<out BaseFragment> {
                return DonatePath()
            }

        }
    }
}