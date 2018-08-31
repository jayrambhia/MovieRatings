package com.fenchtose.movieratings

import android.app.Application
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.model.api.provider.MovieProviderModule
import com.fenchtose.movieratings.model.api.provider.MovieRatingProviderModule
import com.fenchtose.movieratings.model.db.MovieDb
import com.fenchtose.movieratings.model.gsonadapters.IntAdapter
import com.fenchtose.movieratings.util.registerNotificationChannel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.leakcanary.RefWatcher
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.cache.CacheInterceptor

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
            AnalyticsDispatcher()
                    .attachDispatcher("answers", flavorHelper.getAnswersDispatcher())
                    .attachDispatcher("ga", flavorHelper.getGaDispatcher())
        }

        var refWatcher: RefWatcher? = null

        val gson: Gson by lazy {
            GsonBuilder()
                    .setDateFormat("dd MM yyyy")
                    .registerTypeAdapter(Int::class.java, IntAdapter())
                    .setPrettyPrinting()
                    .create()
        }

        val movieProviderModule by lazy {
            MovieProviderModule(instance!!, gson)
        }

        val ratingProviderModule by lazy {
            MovieRatingProviderModule(instance!!, gson)
        }
    }

    open fun getOkHttpClient(cache: Cache? = null, interceptors: List<Interceptor> = listOf(),
                             networkInterceptors: List<Interceptor> = listOf()): OkHttpClient {
        val builder = OkHttpClient.Builder()
        interceptors.forEach { builder.addInterceptor(it) }
        networkInterceptors.forEach { builder.addNetworkInterceptor(it) }
        cache?.apply { builder.cache(this) }
        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()
/*        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)*/
        flavorHelper.onAppCreated(this)
        registerNotificationChannel(this)
    }
}