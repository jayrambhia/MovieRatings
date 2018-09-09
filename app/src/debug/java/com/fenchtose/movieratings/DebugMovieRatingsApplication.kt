package com.fenchtose.movieratings

import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.fenchtose.movieratings.util.add
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class DebugMovieRatingsApplication: MovieRatingsApplication() {
    override fun onCreate() {
        super.onCreate()
        /*val builder = Stetho.newInitializerBuilder(this)

        builder.enableWebKitInspector {
            Stetho.DefaultInspectorModulesBuilder(this).runtimeRepl{

            }.finish()
        }*/
        Stetho.initializeWithDefaults(this)
    }

    override fun getOkHttpClient(cache: Cache?, interceptors: List<Interceptor>,
                                 networkInterceptors: List<Interceptor>): OkHttpClient {
        return super.getOkHttpClient(cache,
                interceptors.add(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)),
                networkInterceptors.add(StethoInterceptor()))
    }
}