package com.fenchtose.movieratings

import android.annotation.SuppressLint
import com.fenchtose.movieratings.analytics.EventDispatcher
import com.fenchtose.movieratings.analytics.FirebaseEventDispatcher
import com.google.firebase.analytics.FirebaseAnalytics

class AppFlavorHelper {

    private lateinit var fa: FirebaseAnalytics

    fun getGaDispatcher(): EventDispatcher = FirebaseEventDispatcher(fa)

    @SuppressLint("MissingPermission")
    fun onAppCreated(app: MovieRatingsApplication) {
        fa = FirebaseAnalytics.getInstance(app)
    }
}