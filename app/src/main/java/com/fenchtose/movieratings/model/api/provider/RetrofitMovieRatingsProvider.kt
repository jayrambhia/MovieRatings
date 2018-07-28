package com.fenchtose.movieratings.model.api.provider

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.api.MovieRatingApi
import com.fenchtose.movieratings.model.db.dao.MovieRatingDao
import com.fenchtose.movieratings.model.db.movieRatings.MovieRatingStore
import com.fenchtose.movieratings.model.entity.MovieRating
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit

class RetrofitMovieRatingsProvider(flutterRetrofit: Retrofit?,
                                   omdbRetrofit: Retrofit?,
                                   private val dao: MovieRatingDao,
                                   private val store: MovieRatingStore): MovieRatingsProvider {

    private val flutterApi: MovieRatingApi? = flutterRetrofit?.create(MovieRatingApi::class.java)
    private val omdbApi: MovieApi? = omdbRetrofit?.create(MovieApi::class.java)

    private val threshold = 60

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
                    Observable.defer {
                        Observable.just(store.was404(title, year, System.currentTimeMillis()/1000 - threshold))
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
                               check404Call: () -> Observable<Boolean>,
                               apiCall: () -> Observable<MovieRating>,
                               analyticsCall: () -> Unit): Observable<MovieRating> {

        return dbCall().flatMap {
            if (it.imdbId.isNotEmpty()) {
                Observable.just(it)
            } else {
                check404Call().flatMap {
                    if (it) {
                        Observable.error(Throwable("already 404"))
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

    override fun report404(title: String, year: String?) {
        Observable.defer {
            Observable.just(store.update404(title, year))
        }
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {

                })

    }
}