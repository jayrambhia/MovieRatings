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

            val titles: List<AccessibilityNodeInfoCompat> = when(it.packageName) {
                BuildConfig.APPLICATION_ID -> it.findAccessibilityNodeInfosByViewId(BuildConfig.APPLICATION_ID + ":id/flutter_test_title")
                Constants.PACKAGE_NETFLIX -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_title")
                Constants.PACKAGE_PRIMEVIDEO -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/TitleText")
                else -> ArrayList()
            }.distinctBy { it.text }

            val years: List<AccessibilityNodeInfoCompat> = when(it.packageName) {
                Constants.PACKAGE_NETFLIX -> it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_basic_info")
                Constants.PACKAGE_PRIMEVIDEO -> {
                    it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/ItemMetadataView")
                            // get children of that node
                            .flatMap {
                                val children = ArrayList<AccessibilityNodeInfoCompat>()
                                (0 until it.childCount).map {
                                    i -> it.getChild(i)
                                }.toCollection(children)
                                children
                            }
                            // filter node which has text containing 4 digits
                            .filter {
                                it.text?.let {
                                    return@filter !FixTitleUtils.fixPrimeVideoYear(it.toString()).isNullOrEmpty()
                                }
                                false
                            }
                }
                else -> ArrayList()
            }.distinctBy { it.text }

            if (titles.isNotEmpty()) {
                titles.first { it.text != null }
                        .let {
                            setMovieTitle(
                                    fixTitle(it.packageName, it.text.toString()),
                                    years.takeIf { it.isNotEmpty() }?.first()?.let {
                                        fixYear(it.packageName, it.text?.toString())
                                    }
                            )
                        }

            }
        }
    }

    @Suppress("unused")
    private fun checkNodeRecursively(info: AccessibilityNodeInfoCompat?) {
        info?.let {
            Log.d(TAG, "info: text:" + it.text + ", id:" + it.viewIdResourceName + ", class:" + it.className + ", parent:" + it.parent?.viewIdResourceName)
            if (info.childCount > 0) {
                Log.d(TAG, "--- <children> ---")
                (0 until info.childCount)
                        .forEach { index ->
                            checkNodeRecursively(it.getChild(index))
                        }

                Log.d(TAG, "--- </children> ---")
            }
        }
    }

    override fun onInterrupt() {
    }

    private fun setMovieTitle(text: String, year: String?) {
        if (title == null || title != text) {
            title = text
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Movie :- title: $text, year: $year")
            }
            getMovieInfo(text, year ?: "")
        }
    }

    private fun fixTitle(packageName: CharSequence, text: String): String {
        return when(packageName) {
            Constants.PACKAGE_PRIMEVIDEO -> FixTitleUtils.fixPrimeVideoTitle(text)
            else -> text
        }
    }

    private fun fixYear(packageName: CharSequence, text: String?): String {
        text?.let {
            val fixed = when(packageName) {
                Constants.PACKAGE_NETFLIX -> FixTitleUtils.fixNetflixYear(it)
                Constants.PACKAGE_PRIMEVIDEO -> FixTitleUtils.fixPrimeVideoYear(it)
                else -> ""
            }

            fixed?.let {
                return it
            }
        }

        return ""
    }

    private fun getMovieInfo(title: String, year: String) {
        analytics?.sendEvent(Event("get_movie"))

        provider?.let {
            it.getMovie(title, year)
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
