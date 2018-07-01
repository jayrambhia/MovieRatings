package com.fenchtose.movieratings.model.inAppAnalytics

interface UserHistory {
    fun updateAppOpened()
    fun getAppOpenedFirst(): Long
    fun getAppOpenedLatest(): Long
    fun getAppOpenedCount(): Int

    fun updateReviewPromptShown()
    fun getReviewPromptShownLatest(): Long
    fun getReviewPromptShownCount(): Int
    fun repliedPositiveToReviewPrompt()
    fun hasRepliedToReviewPrompt(): Boolean

    fun updateSupportAppPromptShown()
    fun getSupportAppPromptShownLatest(): Long
    fun getSupportAppPromptShownCount(): Int
    fun purchasedInAppProduct()
    fun hasPurchasedInAppProduct(): Boolean
}