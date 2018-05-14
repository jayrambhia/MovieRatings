package com.fenchtose.movieratings.model.db.like

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Fav
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import io.reactivex.Observable
import io.reactivex.Single

interface LikeStore: UserPreferenceApplier {
    @WorkerThread
    fun isLiked(imdbId: String): Boolean
    fun setLiked(imdbId: String, liked: Boolean)
    fun deleteAll(): Observable<Int>
    fun export(): Single<List<Fav>>
    @WorkerThread
    fun import(favs: List<Fav>): Int
}