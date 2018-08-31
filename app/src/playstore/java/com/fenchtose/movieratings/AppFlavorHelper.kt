package com.fenchtose.movieratings

//import com.crashlytics.android.Crashlytics
import com.fenchtose.movieratings.analytics.AnswersEventDispatcher
import com.fenchtose.movieratings.analytics.EventDispatcher
import com.fenchtose.movieratings.analytics.GaEventDispatcher
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

//import io.fabric.sdk.android.Fabric

class AppFlavorHelper {

    private lateinit var tracker: Tracker

    fun getAnswersDispatcher(): EventDispatcher = AnswersEventDispatcher()

    fun getGaDispatcher(): EventDispatcher = GaEventDispatcher(tracker)

    fun onAppCreated(app: MovieRatingsApplication) {
        val ga = GoogleAnalytics.getInstance(app)
        ga.setDryRun(BuildConfig.DEBUG)
        tracker = ga.newTracker(R.xml.global_tracker)
    }
}