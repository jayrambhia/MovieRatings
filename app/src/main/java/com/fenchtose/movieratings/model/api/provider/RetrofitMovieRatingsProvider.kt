package com.fenchtose.movieratings.model.api.provider

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.analytics.ga.GaLabels
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.api.MovieRatingApi
import com.fenchtose.movieratings.model.db.dao.MovieRatingDao
import com.fenchtose.movieratings.model.db.movieRatings.MovieRatingStore
import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Trending
import com.fenchtose.movieratings.model.entity.convert
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit

class RetrofitMovieRatingsProvider(flutterRetrofit: Retrofit?,
                                   omdbRetrofit: Retrofit?,
                                   private val dao: MovieRatingDao,
                                   private val store: MovieRatingStore): MovieRatingsProvider {

    private val flutterApi: MovieRatingApi? = flutterRetrofit?.create(MovieRatingApi::class.java)
    private val omdbApi: MovieApi? = omdbRetrofit?.create(MovieApi::class.java)

    private var lastRequest: RatingRequest? = null

    private val threshold404 = 7 * 24 * 3600 // 7 days
    private val thresholdRating = 14 * 24 * 3600 // 14 days

    private var useFlutterApi = true

    override fun useFlutterApi(status: Boolean) {
        useFlutterApi = status
    }

    override fun getMovieRating(request: RatingRequest): Observable<MovieRating> {
        val yearInt = convertYear(request.year)

        return getMovieRating(
                {
                    Observable.defer {
                        Observable.just(getRatingsFromDb(request.title, yearInt, thresholdRating))
                    }
                },
                {
                    Observable.defer {
                        Observable.just(store.was404(request.title, request.year, System.currentTimeMillis()/1000 - threshold404))
                    }
                },
                {
                    if (request == lastRequest) {
                        Observable.error(Throwable("Same request. ignoring $request"))
                    } else {
                        lastRequest = request
                        if (useFlutterApi && flutterApi != null) {
                            flutterApi.getMovieRating(request.title, request.year, order = request.order)
                        } else if (omdbApi != null) {

                            omdbApi.getMovieInfo(BuildConfig.OMDB_API_KEY, request.title, request.year
                                    ?: "")
                                    .map {
                                        MovieRating.fromMovie(it)
                                    }
                        } else {
                            Observable.error(Throwable("No API attached"))
                        }
                    }
                },
                {
                    GaEvents.GET_RATINGS_ONLINE.withLabelArg(if (useFlutterApi) GaLabels.FLUTTER_API else GaLabels.OMDB_API).track()
                }
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
                                    dao.insert(it.convert((System.currentTimeMillis()/1000).toInt()))
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
    private fun getRatingsFromDb(title: String, year: Int, threshold: Int): MovieRating {
        val timestamp = (System.currentTimeMillis()/1000).toInt() - threshold

        var ratings = dao.getRatingsForTitle(title)
                .filter {
                    it.fitsYear(year) && it.timestamp > timestamp
                }

        if (ratings.isEmpty()) {
           ratings = dao.getRatingsForTranslatedTitle(title)
                   .filter {
                       it.fitsYear(year) && it.timestamp > timestamp
                   }
        }

        if (ratings.isEmpty()) {
            return MovieRating.empty()
        }

        return ratings.sortedWith(compareByDescending { it.votes }).first().convert()
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

    override fun getTrending(period: String): Observable<Trending> {
        if (flutterApi == null) {
            return Observable.error(Throwable("No API attached"))
        }

        return flutterApi.getTrending(period)
    }
}