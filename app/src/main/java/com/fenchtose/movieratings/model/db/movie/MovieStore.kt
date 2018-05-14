package com.fenchtose.movieratings.model.db.movie

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Movie
import io.reactivex.Single

interface MovieStore {
    fun export(movies: Collection<String>): Single<List<Movie>>
    @WorkerThread
    fun import(movies: List<Movie>): Int
}