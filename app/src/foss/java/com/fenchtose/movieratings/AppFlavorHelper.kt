package com.fenchtose.movieratings

import com.fenchtose.movieratings.analytics.EventDispatcher
import com.fenchtose.movieratings.analytics.FakeDispatcher

class AppFlavorHelper {

    fun getAnswersDispatcher(): EventDispatcher = FakeDispatcher()
    fun getGaDispatcher(): EventDispatch = FakeDispatcher()

    fun onAppCreated(@Suppress("UNUSED_PARAMETER") app: MovieRatingsApplication) {

    }
}