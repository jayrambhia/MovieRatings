package com.fenchtose.movieratings.features.premium

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.billingclient.api.*
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.AppEvents
import com.fenchtose.movieratings.analytics.ga.AppScreens
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.inAppAnalytics.DbHistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.HistoryKeeper
import com.fenchtose.movieratings.model.inAppAnalytics.PreferenceUserHistory
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.show
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DonatePageFragment : BaseFragment(), PurchasesUpdatedListener {

    private var progressContainer: View? = null
    private lateinit var cardsContainer: CardsContainer

    private var billingClient: BillingClient? = null
    private var historyKeeper: HistoryKeeper? = null
    private var isBillingAvailable = false

    private val totalPurchases = 100

    override fun canGoBack() = true

    override fun getScreenTitle() = R.string.donate_page_title

    override fun screenName() = AppScreens.SUPPORT_APP

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.donate_page_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingStore = DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao())
        subscribe(
            ratingStore.getUniqueRatingsCount()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .filter { it >= 10 }.map { it - it % 10 }.subscribe({
                    view.findViewById<TextView>(R.id.persuasion_message).apply {
                        text = requireContext().getString(R.string.donate_page_persuasion_message, it)
                        visibility = View.VISIBLE
                    }
                }, {

                })
        )

        cardsContainer = view.findViewById(R.id.cards_container)
        progressContainer = view.findViewById(R.id.progress_container)
        historyKeeper = DbHistoryKeeper(
            PreferenceUserHistory(requireContext()),
            ratingStore,
            SettingsPreferences(requireContext())
        )

        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this).build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(result: BillingResult) {
                isBillingAvailable = result.responseCode == BillingClient.BillingResponseCode.OK
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

    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient?.acknowledgePurchase(params) { response ->
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null && !purchases.isEmpty()) {
            historyKeeper?.paidInAppPurchase()
            AppEvents.completePurchase(purchases.first().sku).track()

            purchases.forEach { purchase -> acknowledgePurchase(purchase) }

            AlertDialog.Builder(context)
                .setTitle(R.string.donate_dialog_title)
                .setMessage(R.string.donate_dialog_message)
                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()

            queryAvailablePurchases()
        }
    }

    private fun queryPurchaseHistory(skus: List<SkuDetails>) {
        if (isDetached || !isAdded) {
            return
        }

        if (BuildConfig.DEBUG) {
            showDetails(skus, listOf())
            return
        }

        val purchases = billingClient?.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList
        val subsPurchases = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)?.purchasesList
        val purchasedSkus = (purchases ?: listOf()) + (subsPurchases ?: listOf())
        purchasedSkus.forEach { purchased -> acknowledgePurchase(purchased) }
        showDetails(skus, purchasedSkus.map { it.sku })
    }

    private fun queryAvailablePurchases() {
        if (isDetached || !isAdded) {
            return
        }

        if (BuildConfig.DEBUG) {
            val list = arrayListOf(
                SkuDetails("{\"productId\":\"donate_large\",\"type\":\"inapp\",\"price\":\"€5,49\",\"price_amount_micros\":5490000,\"price_currency_code\":\"EUR\",\"title\":\"Generous (Flutter - Movie Ratings)\",\"description\":\"Buy this tiny in-app package to show your support.\"}"),
                SkuDetails("{\"productId\":\"donate_medium\",\"type\":\"inapp\",\"price\":\"€2,99\",\"price_amount_micros\":2990000,\"price_currency_code\":\"EUR\",\"title\":\"Standard (Flutter - Movie Ratings)\",\"description\":\"Buy this in-app package to support the app and get us a coffee.\"}"),
                SkuDetails("{\"productId\":\"donate_small\",\"type\":\"inapp\",\"price\":\"€0,99\",\"price_amount_micros\":990000,\"price_currency_code\":\"EUR\",\"title\":\"Basic (Flutter - Movie Ratings)\",\"description\":\"Show your generous support and help us run the app for 2 more weeks!\"}")
            )
            querySubscriptions(list)
            return
        }

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(arrayListOf("donate_small", "donate_medium", "donate_large"))
        params.setType(BillingClient.SkuType.INAPP)
        billingClient?.querySkuDetailsAsync(params.build()) { response, skuDetails ->
            if (response.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null && skuDetails.isNotEmpty()) {
                querySubscriptions(skuDetails)
            }
        }
    }

    private fun querySubscriptions(inApp: List<SkuDetails>) {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(arrayListOf("sub_small"))
        params.setType(BillingClient.SkuType.SUBS)
        billingClient?.querySkuDetailsAsync(params.build()) { response, skuDetails ->
            if (response.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null && skuDetails.isNotEmpty()) {
                queryPurchaseHistory(inApp + skuDetails)
            }
        }
    }

    private fun showDetails(skus: List<SkuDetails>, purchases: List<String>) {
        if (isDetached || !isAdded) {
            return
        }

        progressContainer?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.persuasion_message2)?.apply {
            text = if (purchases.isNotEmpty()) {
                getString(R.string.donate_page_has_purchased)
            } else {
                getString(R.string.donate_page_content_no_purchase, totalPurchases)
            }
            show(true)
        }

        val regex = Regex("(\\(.+\\))")

        val content = skus.map { details ->
            PurchaseCardContent(
                sku = details.sku,
                skuDetails = details,
                title = details.title.replace(regex, ""),
                description = details.description,
                price = details.price,
                purchased = purchases.count { it == details.sku }
            )
        }

        view?.findViewById<View>(R.id.warning_message)?.show(true)
        cardsContainer.update(
            content,
            onPurchase = { onPurchaseRequested(it) },
            onBrag = {
                IntentUtils.openShareIntent(requireContext(), getString(R.string.donate_brag_content, Constants.APP_SHARE_URL))
            }
        )
    }

    private fun onPurchaseRequested(skuDetails: SkuDetails) {
        AppEvents.startPurchase(skuDetails.sku).track()

        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient?.launchBillingFlow(requireActivity(), flowParams)
    }

    class DonatePath : RouterPath<DonatePageFragment>() {
        override fun createFragmentInstance() = DonatePageFragment()
        override fun category() = GaCategory.SUPPORT_APP

        companion object {

            const val KEY = "DonatePath"

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