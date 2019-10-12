package com.fenchtose.movieratings.model.db.movie

import androidx.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.entity.Movie
import io.reactivex.Single

interface MovieStore {
    fun export(movies: Collection<String>): Single<List<Movie>>
    @WorkerThread
    fun import(movies: List<Movie>): Int
}