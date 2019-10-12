package com.fenchtose.movieratings.model.db.movie

import androidx.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.entity.Movie
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Single

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

    override fun export(movies: Collection<String>): Single<List<Movie>> {
        return Single.defer {
            Single.fromCallable {
                dao.exportData(movies.toList())
            }
        }
    }

    @WorkerThread
    override fun import(movies: List<Movie>): Int {
        return dao.importData(movies).filter { it != -1L }.count()
    }
}