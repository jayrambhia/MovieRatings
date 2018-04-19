package com.fenchtose.movieratings

//import com.crashlytics.android.Crashlytics
import com.fenchtose.movieratings.analytics.AnswersEventDispatcher
import com.fenchtose.movieratings.analytics.EventDispatcher
//import io.fabric.sdk.android.Fabric

class AppFlavorHelper {

    fun getAnswersDispatcher(): EventDispatcher = AnswersEventDispatcher()

    fun onAppCreated(app: MovieRatingsApplication) {
//        Fabric.with(app, Crashlytics())
    }
}