package com.fenchtose.movieratings.model.inAppAnalytics

import android.content.Context
import android.content.SharedPreferences

class PreferenceUserHistory(context: Context): UserHistory {

    private val PREF_NAME = "user_history"

    private val APP_OPENED_FIRST = "app_opened_first"
    private val APP_OPENED_LATEST = "app_opened_latest"
    private val APP_OPENED_COUNT = "app_opened_count"

    private val REVIEW_APP_SHOWN_FIRST = "review_app_shown_first"
    private val REVIEW_APP_SHOWN_LATEST = "review_app_shown_latest"
    private val REVIEW_APP_SHOWN_COUNT = "review_app_shown_count"
    private val REVIEW_APP_POSITIVE_REPLY = "review_app_positive_reply"

    private val SUPPORT_APP_SHOWN_FIRST = "support_app_shown_first"
    private val SUPPORT_APP_SHOWN_LATEST = "support_app_shown_latest"
    private val SUPPORT_APP_SHOWN_COUNT = "support_app_shown_count"
    private val IN_APP_PURCHASED = "in_app_purchased"

    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun updateAppOpened() {
        if (getAppOpenedFirst() == -1L) {
            updateTime(APP_OPENED_FIRST)
        }

        updateTime(APP_OPENED_LATEST)
        increaseCount(APP_OPENED_COUNT)
    }

    override fun getAppOpenedFirst(): Long {
        return preferences.getLong(APP_OPENED_FIRST, -1)
    }

    override fun getAppOpenedLatest(): Long {
        return preferences.getLong(APP_OPENED_LATEST, -1)
    }

    override fun getAppOpenedCount(): Int {
        return preferences.getInt(APP_OPENED_COUNT, 0)
    }

    override fun updateReviewPromptShown() {
        if (getReviewPromptShownFirst() == -1L) {
            updateTime(REVIEW_APP_SHOWN_FIRST)
        }

        updateTime(REVIEW_APP_SHOWN_LATEST)
        increaseCount(REVIEW_APP_SHOWN_COUNT)
    }

    override fun getReviewPromptShownLatest(): Long {
        return preferences.getLong(REVIEW_APP_SHOWN_LATEST, -1)
    }

    private fun getReviewPromptShownFirst(): Long {
        return preferences.getLong(REVIEW_APP_SHOWN_FIRST, -1)
    }

    override fun getReviewPromptShownCount(): Int {
        return preferences.getInt(REVIEW_APP_SHOWN_COUNT, 0)
    }

    override fun repliedPositiveToReviewPrompt() {
        preferences.edit().putBoolean(REVIEW_APP_POSITIVE_REPLY, true).apply()
    }

    override fun hasRepliedToReviewPrompt(): Boolean {
        return preferences.getBoolean(REVIEW_APP_POSITIVE_REPLY, false)
    }

    override fun updateSupportAppPromptShown() {
        if (getSupportAppPromptShownFirst() == -1L) {
            updateTime(SUPPORT_APP_SHOWN_FIRST)
        }

        updateTime(SUPPORT_APP_SHOWN_LATEST)
        increaseCount(SUPPORT_APP_SHOWN_COUNT)
    }

    override fun getSupportAppPromptShownLatest(): Long {
        return preferences.getLong(SUPPORT_APP_SHOWN_LATEST, -1)
    }

    private fun getSupportAppPromptShownFirst(): Long {
        return preferences.getLong(SUPPORT_APP_SHOWN_FIRST, -1)
    }

    override fun getSupportAppPromptShownCount(): Int {
        return preferences.getInt(SUPPORT_APP_SHOWN_COUNT, 0)
    }

    override fun hasPurchasedInAppProduct(): Boolean {
        return preferences.getBoolean(IN_APP_PURCHASED, false)
    }

    override fun purchasedInAppProduct() {
        preferences.edit().putBoolean(IN_APP_PURCHASED, true).apply()
    }

    private fun increaseCount(key: String) {
        preferences.edit().putInt(key, preferences.getInt(key, 0) + 1).apply()
    }

    private fun updateTime(key: String) {
        preferences.edit().putLong(key, System.currentTimeMillis()/1000).apply()
    }

}