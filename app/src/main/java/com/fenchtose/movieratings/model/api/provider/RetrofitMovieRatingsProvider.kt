package com.fenchtose.movieratings.model.api.provider

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.api.MovieRatingApi
import com.fenchtose.movieratings.model.db.dao.MovieRatingDao
import com.fenchtose.movieratings.model.entity.MovieRating
import io.reactivex.Observable
import retrofit2.Retrofit

class RetrofitMovieRatingsProvider(flutterRetrofit: Retrofit?,
                                   omdbRetrofit: Retrofit?,
                                   val dao: MovieRatingDao): MovieRatingsProvider {

    private val flutterApi: MovieRatingApi? = flutterRetrofit?.create(MovieRatingApi::class.java)
    private val omdbApi: MovieApi? = omdbRetrofit?.create(MovieApi::class.java)

    private var useFlutterApi = true

    override fun useFlutterApi(status: Boolean) {
        useFlutterApi = status
    }

    override fun getMovieRating(title: String, year: String?): Observable<MovieRating> {
        val yearInt = convertYear(year)

        return getMovieRating(
                {
                    Observable.defer {
                        Observable.just(getRatingsFromDb(title, yearInt))
                    }
                },
                {
                    if (useFlutterApi && flutterApi != null) {
                        flutterApi.getMovieRating(title, year)
                    } else if (omdbApi != null) {

                        omdbApi.getMovieInfo(BuildConfig.OMDB_API_KEY, title, year ?: "")
                                .map {
                                    MovieRating.fromMovie(it)
                                }
                    } else {
                        Observable.error(Throwable("No API attached"))
                    }
                },
                {}
        )
    }

    private fun getMovieRating(dbCall: () -> Observable<MovieRating>,
                              apiCall: () -> Observable<MovieRating>,
                              analyticsCall: () -> Unit): Observable<MovieRating> {

        return dbCall().flatMap {
            if (it.imdbId.isNotEmpty()) {
                Observable.just(it)
            } else {
                apiCall()
                        .doOnNext {
                            analyticsCall.invoke()
                        }.doOnNext {
                            dao.insert(it)
                        }
            }
        }

    }

    private fun convertYear(year: String?): Int {
        year?.let {
            return try {
                year.toInt()
            } catch (e: Exception) {
                -1
            }
        }

        return -1
    }

    @WorkerThread
    private fun getRatingsFromDb(title: String, year: Int): MovieRating {
        var ratings = dao.getRatingsForTitle(title)
                .filter {
                    it.fitsYear(year)
                }

        if (ratings.isEmpty()) {
           ratings = dao.getRatingsForTranslatedTitle(title)
                   .filter {
                       it.fitsYear(year)
                   }
        }

        if (ratings.isEmpty()) {
            return MovieRating.empty()
        }

        return ratings.sortedWith(compareByDescending { it.votes }).first()
    }

}