package com.fenchtose.movieratings

import com.fenchtose.movieratings.analytics.EventDispatcher
import com.fenchtose.movieratings.analytics.GaEventDispatcher
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

class AppFlavorHelper {

    private lateinit var tracker: Tracker

    fun getGaDispatcher(): EventDispatcher = GaEventDispatcher(tracker)

    fun onAppCreated(app: MovieRatingsApplication) {
        val ga = GoogleAnalytics.getInstance(app)
        ga.setDryRun(BuildConfig.DEBUG)
        tracker = ga.newTracker(R.xml.global_tracker)
    }
}