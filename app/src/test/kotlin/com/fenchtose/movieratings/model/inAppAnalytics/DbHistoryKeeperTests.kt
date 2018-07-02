package com.fenchtose.movieratings.model.inAppAnalytics

import com.fenchtose.movieratings.model.db.displayedRatings.DisplayedRatingsStore
import com.fenchtose.movieratings.model.entity.DisplayedRating
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import kotlin.math.max
import kotlin.test.assertEquals

@Suppress("IllegalIdentifier")
class DbHistoryKeeperTests {

    private val userHistory: UserHistory = mock()
    private val ratingStore: DisplayedRatingsStore = mock()
    private val preferences: UserPreferences = mock()

    private val historyKeeper: DbHistoryKeeper = DbHistoryKeeper(userHistory, ratingStore, preferences)

    @Before
    fun setup() {
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(historyKeeper.RATINGS_SHOWN_THRESHOLD))

        whenever(preferences.isAppEnabled(UserPreferences.SHOW_SUPPORT_APP_PROMPT)).thenReturn(true)
        whenever(userHistory.hasPurchasedInAppProduct()).thenReturn(false)

        val now = System.currentTimeMillis() / 1000
        whenever(userHistory.getAppOpenedFirst()).thenReturn(now - max(historyKeeper.SUPPORT_APP_THRESHOLD, historyKeeper.RATE_APP_THRESHOLD) - 50)
        whenever(userHistory.getSupportAppPromptShownLatest()).thenReturn(now - historyKeeper.SUPPORT_APP_THRESHOLD - 20)

        whenever(preferences.isAppEnabled(UserPreferences.SHOW_RATE_APP_PROMPT)).thenReturn(true)
        whenever(userHistory.hasRepliedToReviewPrompt()).thenReturn(false)

        whenever(userHistory.getReviewPromptShownLatest()).thenReturn(now - historyKeeper.RATE_APP_THRESHOLD - 20)
    }

    @Test
    fun `app opened`() {
        historyKeeper.appOpened()
        verify(userHistory).updateAppOpened()
    }

    @Test
    fun `rating displayed`() {
        historyKeeper.ratingDisplayed("id", "com.test")
        argumentCaptor<DisplayedRating>().apply {
            verify(ratingStore).update(capture())
            assertEquals(1, allValues.size)
            assertEquals("id", firstValue.movieId)
            assertEquals("com.test", firstValue.appPackage)
        }
    }

    @Test
    fun `paid in app purchase`() {
        historyKeeper.paidInAppPurchase()
        verify(userHistory).purchasedInAppProduct()
    }

    @Test
    fun `rated app on playstore`() {
        historyKeeper.ratedAppOnPlaystore()
        verify(userHistory).repliedPositiveToReviewPrompt()
    }

    @Test
    fun `blocked support prompt`() {
        whenever(preferences.isAppEnabled(UserPreferences.SHOW_SUPPORT_APP_PROMPT)).thenReturn(false)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `already replied to support prompt`() {
        whenever(userHistory.hasPurchasedInAppProduct()).thenReturn(true)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `blocked and replied to support prompt`() {
        whenever(preferences.isAppEnabled(UserPreferences.SHOW_SUPPORT_APP_PROMPT)).thenReturn(false)
        whenever(userHistory.hasPurchasedInAppProduct()).thenReturn(true)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `never opened the app - support prompt`() {
        whenever(userHistory.getAppOpenedFirst()).thenReturn(-1)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened under threshold - support prompt`() {
        val now = System.currentTimeMillis() / 1000
        whenever(userHistory.getAppOpenedFirst()).thenReturn(now - historyKeeper.SUPPORT_APP_THRESHOLD/2)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and no ratings displayed - support prompt`() {
        whenever(userHistory.getSupportAppPromptShownLatest()).thenReturn(-1)
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(0))
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and ratings displayed under threshold - support prompt`() {
        whenever(userHistory.getSupportAppPromptShownLatest()).thenReturn(-1)
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(historyKeeper.RATINGS_SHOWN_THRESHOLD - 1))
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and ratings displayed over threshold - support prompt`() {
        whenever(userHistory.getSupportAppPromptShownLatest()).thenReturn(-1)
        assertObserver(true, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before under threshold - support prompt`() {
        val now = System.currentTimeMillis() / 1000
        whenever(userHistory.getSupportAppPromptShownLatest()).thenReturn(now - historyKeeper.SUPPORT_APP_THRESHOLD/2)
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and no ratings displayed - support prompt`() {
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(0))
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and ratings displayed under threshold - support prompt`() {
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(historyKeeper.RATINGS_SHOWN_THRESHOLD - 1))
        assertObserver(false, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and ratings displayed over threshold - support prompt`() {
        assertObserver(true, historyKeeper::shouldShowSupportAppPrompt)
    }

    @Test
    fun `blocked rate prompt`() {
        whenever(preferences.isAppEnabled(UserPreferences.SHOW_RATE_APP_PROMPT)).thenReturn(false)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `already replied to rate prompt`() {
        whenever(userHistory.hasRepliedToReviewPrompt()).thenReturn(true)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `blocked and replied to rate prompt`() {
        whenever(preferences.isAppEnabled(UserPreferences.SHOW_RATE_APP_PROMPT)).thenReturn(false)
        whenever(userHistory.hasRepliedToReviewPrompt()).thenReturn(true)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `never opened the app - rate prompt`() {
        whenever(userHistory.getAppOpenedFirst()).thenReturn(-1)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened under threshold - rate prompt`() {
        val now = System.currentTimeMillis() / 1000
        whenever(userHistory.getAppOpenedFirst()).thenReturn(now - historyKeeper.RATE_APP_THRESHOLD/2)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and no ratings displayed - rate prompt`() {
        whenever(userHistory.getReviewPromptShownLatest()).thenReturn(-1)
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(0))
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and ratings displayed under threshold - rate prompt`() {
        whenever(userHistory.getReviewPromptShownLatest()).thenReturn(-1)
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(historyKeeper.RATINGS_SHOWN_THRESHOLD - 1))
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and never shown before and ratings displayed over threshold - rate prompt`() {
        whenever(userHistory.getReviewPromptShownLatest()).thenReturn(-1)
        assertObserver(true, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before under threshold - rate prompt`() {
        val now = System.currentTimeMillis() / 1000
        whenever(userHistory.getReviewPromptShownLatest()).thenReturn(now - historyKeeper.RATE_APP_THRESHOLD/2)
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and no ratings displayed - rate prompt`() {
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(0))
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and ratings displayed under threshold - rate prompt`() {
        whenever(ratingStore.getUniqueRatingsCount()).thenReturn(Observable.just(historyKeeper.RATINGS_SHOWN_THRESHOLD - 1))
        assertObserver(false, historyKeeper::shouldShowRateAppPrompt)
    }

    @Test
    fun `first opened over threshold and shown before over threshold and ratings displayed over threshold - rate prompt`() {
        assertObserver(true, historyKeeper::shouldShowRateAppPrompt)
    }

    private fun assertObserver(expected: Boolean, method: (() -> Observable<Boolean>)) {
        val observer = TestObserver<Boolean>()
        method.invoke().subscribe(observer)
        observer.assertComplete()
        observer.assertNoErrors()
        observer.assertValueCount(1)
        assertEquals(expected, observer.values()[0])
    }
}