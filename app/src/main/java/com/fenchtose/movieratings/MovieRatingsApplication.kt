package com.fenchtose.movieratings

import android.app.Application
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.model.api.provider.MovieProviderModule
import com.fenchtose.movieratings.model.db.MovieDb
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import okhttp3.OkHttpClient

open class MovieRatingsApplication : Application() {

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
        var refWatcher: RefWatcher? = null

        val movieProviderModule by lazy {
            MovieProviderModule(instance!!)
        }
    }

    open fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)
        flavorHelper.onAppCreated(this)
    }
}