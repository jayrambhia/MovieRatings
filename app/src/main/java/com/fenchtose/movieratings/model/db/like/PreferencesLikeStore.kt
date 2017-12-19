package com.fenchtose.movieratings.model.db.like

import android.content.Context
import android.content.SharedPreferences
import com.fenchtose.movieratings.model.Movie

class PreferencesLikeStore(val context: Context) : LikeStore {

    private val preferences: SharedPreferences = context.getSharedPreferences("like_store", Context.MODE_PRIVATE)

    override fun isLiked(imdbId: String): Boolean = preferences.getBoolean(imdbId, false)

    override fun setLiked(imdbId: String, liked: Boolean) {
        preferences.edit().putBoolean(imdbId, liked).apply()
    }

    override fun apply(movie: Movie) {
        movie.liked = isLiked(movie.imdbId)
    }
}