package com.fenchtose.movieratings.model.inAppAnalytics

import io.reactivex.Observable

interface HistoryKeeper {
    fun appOpened()
    fun ratingDisplayed(movieId: String, packageName: String)
    fun paidInAppPurchase()
    fun ratedAppOnPlaystore()

    fun inAppPurchasePromptShown()
    fun rateAppPromptShown()

    fun shouldShowSupportAppPrompt(): Observable<Boolean>
    fun shouldShowRateAppPrompt(): Observable<Boolean>
}