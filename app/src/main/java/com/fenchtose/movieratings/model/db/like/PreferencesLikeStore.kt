package com.fenchtose.movieratings.model.db.like

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Fav
import com.fenchtose.movieratings.model.Movie
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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

    override fun export(): Observable<JsonArray> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun import(favs: List<Fav>): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}