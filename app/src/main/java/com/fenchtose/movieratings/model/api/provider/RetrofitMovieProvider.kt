package com.fenchtose.movieratings.model.api.provider

import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable
import retrofit2.Retrofit

class RetrofitMovieProvider(retrofit: Retrofit, val dao: MovieDao) : MovieProvider {

    val api: MovieApi = retrofit.create(MovieApi::class.java)
    val TAG = "MovieProvider"

    override fun getMovie(title: String): Observable<Movie> {
        Log.d(TAG, "call movie info: " + title)
        return getMovieFromDb(title)
                .flatMap {
                    if (it.id != -1) {
                        Observable.just(it)
                    } else {
                        api.getMovieInfo(title, BuildConfig.OMDB_API_KEY)
                                .doOnNext {
                                    dao.insert(it)
                                }
                    }
                }
    }

    fun getMovieFromDb(title: String): Observable<Movie> {
        return Observable.defer {
            val movie = dao.getMovie(title)
            if (movie != null) {
                Observable.just(movie)
            } else {
                Observable.just(Movie.empty())
            }
        }
    }

    override fun search(title: String): Observable<SearchResult> {
        return api.search(title, BuildConfig.OMDB_API_KEY)
    }

}