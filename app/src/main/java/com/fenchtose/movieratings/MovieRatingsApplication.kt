package com.fenchtose.movieratings

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import okhttp3.OkHttpClient

open class MovieRatingsApplication : Application() {

    companion object {
        var refWatcher: RefWatcher? = null
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
        AppFlavorHelper().onAppCreated(this)
    }
}