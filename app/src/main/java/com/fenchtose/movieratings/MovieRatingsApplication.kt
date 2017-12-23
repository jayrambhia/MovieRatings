package com.fenchtose.movieratings

import android.app.Application
import com.facebook.stetho.Stetho
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.api.provider.MovieProviderModule
import com.fenchtose.movieratings.model.db.MovieDb

class MovieRatingsApplication : Application() {

    init {
        instance = this
    }

    companion object {
        var instance: MovieRatingsApplication? = null

        val flavorHelper: AppFlavorHelper = AppFlavorHelper()

        val database by lazy {
            MovieDb.instance
        }

        val analyticsDispatcher: AnalyticsDispatcher by lazy {
            AnalyticsDispatcher().attachDispatcher("answers", flavorHelper.getAnswersDispatcher())
        }

        var router: Router? = null

        val movieProviderModule by lazy {
            MovieProviderModule(instance!!)
        }
    }

    override fun onCreate() {
        super.onCreate()
        flavorHelper.onAppCreated(this)
        Stetho.initializeWithDefaults(this)
    }
}