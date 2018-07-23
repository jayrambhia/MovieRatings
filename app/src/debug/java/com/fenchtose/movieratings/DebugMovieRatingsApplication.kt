package com.fenchtose.movieratings

import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class DebugMovieRatingsApplication: MovieRatingsApplication() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

    override fun getOkHttpClient(vararg interceptors: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor())
        if (interceptors.isNotEmpty()) {
            for (interceptor in interceptors) {
                builder.addInterceptor(interceptor)
            }
        }

        return builder.build()
    }
}