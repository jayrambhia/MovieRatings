package com.fenchtose.movieratings.model.db.movie

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Movie
import com.google.gson.JsonArray
import io.reactivex.Observable

interface MovieStore {
    fun export(): Observable<JsonArray>
    @WorkerThread
    fun import(movies: List<Movie>): Int
}