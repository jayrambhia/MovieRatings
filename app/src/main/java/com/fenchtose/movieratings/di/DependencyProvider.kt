package com.fenchtose.movieratings.di

import android.support.v7.app.AppCompatActivity
import com.fenchtose.movieratings.AppFlavorHelper
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.api.provider.MovieProviderModule
import com.fenchtose.movieratings.model.db.MovieDb

class DependencyProvider private constructor(context: AppCompatActivity) {

    var database: MovieDb? = MovieDb.instance(context)
    var router: Router? = Router(context)
    var analytics: AnalyticsDispatcher? = AnalyticsDispatcher().attachDispatcher("answers", AppFlavorHelper().getAnswersDispatcher())
    var movieProviderModule: MovieProviderModule? = MovieProviderModule(context, database!!.movieDao(), analytics)


    fun clear() {
        movieProviderModule?.release()
        movieProviderModule = null
        database?.close()
        database = null
        analytics?.removeDispatcher("answers")
        analytics = null
        router?.clear()
        router?.callback = null
        router = null
    }

    companion object {
        private var instance: DependencyProvider? = null

        fun createInstance(activity: AppCompatActivity): DependencyProvider {
            val di = DependencyProvider(activity)
            instance = di
            return di
        }

        fun di(): DependencyProvider? = instance
    }


}