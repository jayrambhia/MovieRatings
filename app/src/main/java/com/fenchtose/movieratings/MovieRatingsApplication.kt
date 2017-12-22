package com.fenchtose.movieratings

import android.app.Application
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.api.provider.MovieProviderModule
import com.fenchtose.movieratings.model.db.MovieDb

class MovieRatingsApplication : Application() {

    companion object {
        var instance: MovieRatingsApplication? = null
        var dispatcher: AnalyticsDispatcher? = null
        var router: Router? = null
        var flavorHelper: AppFlavorHelper = AppFlavorHelper()

        val movieProviderModule by lazy {
            MovieProviderModule(instance!!)
        }

        fun getDatabase() : MovieDb {
            return MovieDb.getInstance(instance!!.applicationContext)
        }

        fun getAnalyticsDispatcher(): AnalyticsDispatcher {
            if (dispatcher == null) {
                val dispatcher = AnalyticsDispatcher()
                dispatcher.attachDispatcher("answers", flavorHelper.getAnswersDispatcher())
                this.dispatcher = dispatcher
            }

            return dispatcher!!
        }

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        flavorHelper.onAppCreated(this)
    }

    fun getDatabase() : MovieDb {
        return MovieDb.getInstance(this)
    }

}