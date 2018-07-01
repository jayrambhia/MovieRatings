package com.fenchtose.movieratings.model.db.like

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.entity.Fav
import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable
import io.reactivex.Single

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

    override fun export(): Single<List<Fav>> {
        return Single.defer {
            Single.fromCallable {
                val favs = ArrayList<Fav>()
                preferences.all.filter { it.value != null && it.value is Boolean && it.value == true }
                        .map {
                            val fav = Fav()
                            fav.id = it.key
                            fav.liked = it.value as Boolean
                            fav
                        }.toCollection(favs)
                favs
            }
        }
    }

    @WorkerThread
    override fun import(favs: List<Fav>): Int {
        val editor = preferences.edit()
        var count = 0
        for (fav in favs.filter { it.liked }) {
            editor.putBoolean(fav.id, fav.liked)
            count++
        }
        editor.apply()

        return count
    }
}