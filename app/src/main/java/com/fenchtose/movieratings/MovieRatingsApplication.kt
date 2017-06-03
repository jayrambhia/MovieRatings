package com.fenchtose.movieratings

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.AnswersEventDispatcher
import com.fenchtose.movieratings.model.db.MovieDb
import io.fabric.sdk.android.Fabric

class MovieRatingsApplication : Application() {
    companion object {
        var instance: MovieRatingsApplication? = null
        var dispatcher: AnalyticsDispatcher? = null

        fun getDatabase() : MovieDb {
            return MovieDb.getInstance(instance!!.applicationContext)
        }

        fun getAnalyticsDispatcher(): AnalyticsDispatcher {
            if (dispatcher == null) {
                val dispatcher = AnalyticsDispatcher()
                dispatcher.attachDispatcher("answers", AnswersEventDispatcher())
                this.dispatcher = dispatcher
            }

            return dispatcher!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fabric.with(this, Crashlytics())
    }
}