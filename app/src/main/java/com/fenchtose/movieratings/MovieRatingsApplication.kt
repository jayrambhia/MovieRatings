package com.fenchtose.movieratings

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.fenchtose.movieratings.model.db.MovieDb
import io.fabric.sdk.android.Fabric

class MovieRatingsApplication : Application() {
    companion object {
        var instance: MovieRatingsApplication? = null

        fun getDatabase() : MovieDb {
            return MovieDb.getInstance(instance!!.applicationContext)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fabric.with(this, Crashlytics())
    }
}