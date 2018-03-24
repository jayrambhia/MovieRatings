package com.fenchtose.movieratings

import android.accessibilityservice.AccessibilityService
import android.support.v4.view.accessibility.AccessibilityEventCompat
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.display.RatingDisplayer
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils


class NetflixReaderService : AccessibilityService() {

    private var title: String? = null
    private val TAG: String = "NetflixReaderService"

    private var provider: MovieProvider? = null

    private var preferences: UserPreferences? = null

    // For Samsung S6 edge, we are getting TYPE_WINDOW_STATE_CHANGED for adding floating window which triggers removeView()
    private val supportedPackages: Array<String> = arrayOf(Constants.PACKAGE_NETFLIX, Constants.PACKAGE_PRIMEVIDEO/*, BuildConfig.APPLICATION_ID*/)

    private var lastWindowStateChangeEventTime: Long = 0
    private val WINDOW_STATE_CHANGE_THRESHOLD = 2000

    private var analytics: AnalyticsDispatcher? = null

    private var displayer: RatingDisplayer? = null

    override fun onCreate() {
        super.onCreate()

        preferences = SettingsPreferences(this)
        provider = MovieRatingsApplication.movieProviderModule.movieProvider
        analytics = MovieRatingsApplication.analyticsDispatcher
        displayer = RatingDisplayer(this, analytics!!, preferences!!)

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "eventt: " + AccessibilityEvent.eventTypeToString(event.eventType) + ", " + event.packageName)
        }

        if (!supportedPackages.contains(event.packageName)) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && displayer != null && displayer!!.isShowingView
                    && event.packageName != BuildConfig.APPLICATION_ID) {
                if (System.currentTimeMillis() - lastWindowStateChangeEventTime > WINDOW_STATE_CHANGE_THRESHOLD) {
                    // User has moved to some other app
                    displayer?.removeView()
                    title = null
                }
            }

            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            displayer?.removeView()
            lastWindowStateChangeEventTime = System.currentTimeMillis()
            title = null
        }

        val record = AccessibilityEventCompat.asRecord(event)
        val info = record.source
        info?.let {

            val isAppEnabled = when (it.packageName) {
                BuildConfig.APPLICATION_ID -> true
                Constants.PACKAGE_NETFLIX -> preferences?.isAppEnabled(UserPreferences.NETFLIX)
                Constants.PACKAGE_PRIMEVIDEO -> preferences?.isAppEnabled(UserPreferences.PRIMEVIDEO)
                else -> false
            }

            if (isAppEnabled == null || !isAppEnabled) {
                return
            }

            val titles: List<AccessibilityNodeInfoCompat> = when {
                it.packageName == BuildConfig.APPLICATION_ID -> it.findAccessibilityNodeInfosByViewId(BuildConfig.APPLICATION_ID + ":id/flutter_test_title")
                it.packageName == Constants.PACKAGE_NETFLIX -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_title")
                it.packageName == Constants.PACKAGE_PRIMEVIDEO -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/TitleText")
                else -> ArrayList()
            }

            if (titles.isNotEmpty()) {
                titles.filter { it.text != null }
                        .forEach {
                            setMovieTitle(fixTitle(it.packageName, it.text.toString()))
                        }
            }
        }
    }

    @Suppress("unused")
    fun checkNodeRecursively(info: AccessibilityNodeInfoCompat?) {
        info?.let {
            Log.d(TAG, "info: " + it.text + ", " + it.viewIdResourceName + ", " + it.className + ", " + it.parent)
            Log.d(TAG, "--- children ---")
            (0 until info.childCount)
                    .forEach {
                        index -> checkNodeRecursively(it.getChild(index))
                    }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "on interrupt")
    }


    private fun setMovieTitle(text: String) {
        if (title == null || title != text) {
            title = text
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Movie Title: " + title!!)
            }
            getMovieInfo(text)
        }
    }

    private fun fixTitle(packageName: CharSequence, text: String): String {
        return when(packageName) {
            Constants.PACKAGE_PRIMEVIDEO -> FixTitleUtils.fixPrimeVideoTitle(text)
            else -> text
        }
    }

    private fun getMovieInfo(title: String) {
        analytics?.sendEvent(Event("get_movie").putAttribute("title", title))

        provider?.let {
            it.getMovie(title)
                    .subscribeOn(Schedulers.io())
                    .filter { it.ratings.size > 0 }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onNext = {
                        showRating(it)
                    }, onError = {
                        it.printStackTrace()
                    })
        }
    }

    private fun showRating(movie: Movie) {
        displayer?.showRatingWindow(movie)
    }
}
