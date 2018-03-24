package com.fenchtose.movieratings.model.db.like

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Movie
import io.reactivex.Observable

class PreferencesLikeStore(val context: Context) : LikeStore {

    private val preferences: SharedPreferences = context.getSharedPreferences("like_store", Context.MODE_PRIVATE)

    @WorkerThread
    override fun isLiked(imdbId: String): Boolean = preferences.getBoolean(imdbId, false)

    override fun setLiked(imdbId: String, liked: Boolean) {
        preferences.edit().putBoolean(imdbId, liked).apply()
    }

    override fun deleteAll(): Observable<Int> {
        preferences.edit().clear().apply()
        return Observable.just(1)
    }

    @WorkerThread
    override fun apply(movie: Movie) {
        movie.liked = isLiked(movie.imdbId)
    }
}