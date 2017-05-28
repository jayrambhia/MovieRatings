package com.fenchtose.movieratings

import android.app.Application
import com.fenchtose.movieratings.model.db.MovieDb

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
    }
}