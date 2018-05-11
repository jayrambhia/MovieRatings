package com.fenchtose.movieratings.model.db.movie

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.google.gson.JsonArray
import io.reactivex.Observable

class DbMovieStore private constructor(private val dao: MovieDao): MovieStore {

    companion object {
        private var instance: DbMovieStore? = null

        fun getInstance(dao: MovieDao): MovieStore {
            if (instance == null) {
                instance = DbMovieStore(dao)
            }

            return instance!!
        }
    }

    override fun export(): Observable<JsonArray> {
        return Observable.defer {
            Observable.fromCallable {
                dao.getAll()
            }.map {
                MovieRatingsApplication.gson.toJsonTree(it).asJsonArray
            }
        }
    }
}