package com.fenchtose.movieratings.model.inAppAnalytics

import android.content.Context
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.displayedRatings.DbDisplayedRatingsStore
import com.fenchtose.movieratings.model.db.displayedRatings.DisplayedRatingsStore
import com.fenchtose.movieratings.model.entity.DisplayedRating
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.Observable

class DbHistoryKeeper(private val userHistory: UserHistory,
                      private val ratingsStore: DisplayedRatingsStore,
                      private val preferences: UserPreferences): HistoryKeeper {

    private val APP_OPENED_THRESHOLD = 7 * 24 * 3600 // 7 days
    private val SUPPORT_APP_THRESHOLD = 10 * 24 * 3600 // 10 days
    private val RATE_APP_THRESHOLD = 14 * 24 * 3600 // 14 days
    private val RATINGS_SHOWN_THRESHOLD = 1/*0*/

    companion object {
        fun newInstance(context: Context): DbHistoryKeeper {
            return DbHistoryKeeper(PreferenceUserHistory(context),
                    DbDisplayedRatingsStore.getInstance(MovieRatingsApplication.database.displayedRatingsDao()),
                    SettingsPreferences(context))
        }
    }

    override fun appOpened() {
        userHistory.updateAppOpened()
    }

    override fun ratingDisplayed(movieId: String, packageName: String) {
        ratingsStore.update(DisplayedRating(0, movieId, System.currentTimeMillis(), packageName))
    }

    override fun paidInAppPurchase() {
        userHistory.purchasedInAppProduct()
    }

    override fun ratedAppOnPlaystore() {
        userHistory.repliedPositiveToReviewPrompt()
    }

    override fun shouldShowSupportAppPrompt(): Observable<Boolean> {

        return shouldShowPrompt(userHistory.hasPurchasedInAppProduct(),
                !preferences.isAppEnabled(UserPreferences.SHOW_SUPPORT_APP_PROMPT),
                userHistory.getSupportAppPromptShownLatest(),
                SUPPORT_APP_THRESHOLD)
                .flatMap {
                    if (it) {
                        ratingsStore.getUniqueRatingsCount().map { it >= RATINGS_SHOWN_THRESHOLD }
                    } else {
                        Observable.just(it)
                    }

                }
    }

    override fun shouldShowRateAppPrompt(): Observable<Boolean> {
        return shouldShowPrompt(userHistory.hasRepliedToReviewPrompt(),
                !preferences.isAppEnabled(UserPreferences.SHOW_RATE_APP_PROMPT),
                userHistory.getReviewPromptShownLatest(),
                RATE_APP_THRESHOLD)
                .flatMap {
                    if (it) {
                        ratingsStore.getUniqueRatingsCount().map { it >= RATINGS_SHOWN_THRESHOLD }
                    } else {
                        Observable.just(it)
                    }

                }
    }

    private fun shouldShowPrompt(hasReplied: Boolean, blocked: Boolean,
                                 lastShown: Long, threshold: Int): Observable<Boolean> {
        var show = false
        if (hasReplied || blocked) {
            show = false
        }

        val firstOpened = userHistory.getAppOpenedFirst()
        val now = System.currentTimeMillis()/1000

        if (firstOpened != -1L && now - firstOpened > APP_OPENED_THRESHOLD) {
            if (now - lastShown > threshold) {
                show = true
            }
        }

        return Observable.just(show)
    }

    override fun inAppPurchasePromptShown() {
        userHistory.updateSupportAppPromptShown()
    }

    override fun rateAppPromptShown() {
        userHistory.updateReviewPromptShown()
    }
}