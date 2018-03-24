package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable
import retrofit2.Retrofit

class RetrofitMovieProvider(retrofit: Retrofit, val dao: MovieDao) : MovieProvider {

    val api: MovieApi = retrofit.create(MovieApi::class.java)
    val analytics = MovieRatingsApplication.analyticsDispatcher
    private val preferenceAppliers = ArrayList<UserPreferneceApplier>()

    override fun getMovieWithImdb(imdbId: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDbWithImdb(imdbId) },
                { api.getMovieInfoWithImdb(BuildConfig.OMDB_API_KEY, imdbId) },
                { analytics.sendEvent(Event("get_movie_online").putAttribute("imdb", imdbId)) }
        )
    }

    override fun getMovie(title: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDb(title) },
                { api.getMovieInfo(BuildConfig.OMDB_API_KEY, title) },
                { analytics.sendEvent(Event("get_movie_online").putAttribute("title", title)) }
        )
    }

    private fun getMovie(dbCall: () -> Observable<Movie>,
                         apiCall: () -> Observable<Movie>,
                         analyticsCall: () -> Unit): Observable<Movie> {

        return dbCall()
                .flatMap {
                    if (it.id != -1) {
                        Observable.just(it)
                    } else {
                        Observable.just(true)
                                .doOnNext {
                                    analyticsCall()
                                }.flatMap {
                                    apiCall()
                                    .doOnNext {
                                        dao.insert(it)
                                    }
                                }

                        }
                }
                .doOnNext {
                    for (preferenceApplier in preferenceAppliers) {
                        preferenceApplier.apply(it)
                    }
                }

    }

    private fun getMovieFromDb(title: String): Observable<Movie> {
        return Observable.defer {
            val movie = dao.getMovie(title)
            if (movie != null && movie.isComplete(Movie.Check.BASE)) {
                Observable.just(movie)
            } else {
                Observable.just(Movie.empty())
            }
        }
    }

    private fun getMovieFromDbWithImdb(imdbId: String): Observable<Movie> {
        return Observable.defer {
            val movie = dao.getMovieWithImdbId(imdbId)
            if (movie != null && movie.isComplete(Movie.Check.EXTRA)) {
                Observable.just(movie)
            } else {
                Observable.just(Movie.empty())
            }
        }
    }

    override fun search(title: String, page: Int): Observable<SearchResult> {
        return api.search(BuildConfig.OMDB_API_KEY, title, page)
                .doOnNext {
                    it.results.map {
                        for (preferenceApplier in preferenceAppliers) {
                            preferenceApplier.apply(it)
                        }
                    }
                }
                .doOnNext {
                    it.results.map {
                        dao.insertSearch(it)
                    }
                }
    }

    override fun addPreferenceApplier(applier: UserPreferneceApplier) {
        preferenceAppliers.add(applier)
    }

}