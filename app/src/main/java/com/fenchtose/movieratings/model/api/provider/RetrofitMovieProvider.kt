package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.db.apply
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.entity.*
import com.fenchtose.movieratings.util.Constants
import io.reactivex.Observable
import retrofit2.Retrofit

class RetrofitMovieProvider(retrofit: Retrofit, val dao: MovieDao) : MovieProvider {

    val api: MovieApi = retrofit.create(MovieApi::class.java)
    private val preferenceAppliers = HashSet<UserPreferenceApplier>()

    override fun getMovieWithImdb(imdbId: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDbWithImdb(imdbId) },
                { api.getMovieInfoWithImdb(BuildConfig.OMDB_API_KEY, imdbId).map { it.convert() } },
                {  }
        )
    }

    override fun getMovie(title: String, year: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDb(title, year) },
                { api.getMovieInfo(BuildConfig.OMDB_API_KEY, title, year).map { it.convert() } },
                {  },
                { api.getMovieInfo(BuildConfig.OMDB_API_KEY, title).map { it.convert() }}
        )
    }

    private fun getMovie(dbCall: () -> Observable<Movie>,
                         apiCall: () -> Observable<Movie>,
                         analyticsCall: () -> Unit,
                         fallbackApiCall: (() -> Observable<Movie>)? = null): Observable<Movie> {

        return dbCall()
                .flatMap {
                    if (it.imdbId.isNotEmpty()) {
                        Observable.just(it)
                    } else {
                        Observable.just(true)
                                .doOnNext {
                                    analyticsCall()
                                }.flatMap {
                                    apiCall()
                                    .flatMap {
                                        if (it.imdbId.isEmpty() && fallbackApiCall != null) {
                                            fallbackApiCall.invoke()
                                        } else {
                                            Observable.just(it)
                                        }
                                    }
                                    .filter { it.isComplete(Movie.Check.BASE) }
                                    .doOnNext {
                                        dao.insert(it.convert())
                                    }
                                }

                        }
                }
                .filter { it.isComplete(Movie.Check.BASE) }
                .map {
                    preferenceAppliers.apply(it)
                }

    }

    /**
     * checks for ratings also.
     */
    private fun getMovieFromDb(title: String, year: String): Observable<Movie> {
        return Observable.defer {
            val dbMovie = if (year.isNotEmpty()) dao.getMovie(title, year) else dao.getMovie(title)
            dbMovie?.let {
                val movie = dbMovie.convert()
                if (movie.isComplete(Movie.Check.BASE) && movie.imdbRating.isNotBlank()) {
                    return@defer Observable.just(movie)
                }
            }

            Observable.just(Movie.invalid())
        }
    }

    private fun getMovieFromDbWithImdb(imdbId: String): Observable<Movie> {
        return Observable.defer {
            val dbMovie = dao.getMovieWithImdbId(imdbId)
            dbMovie?.let {
                val movie = dbMovie.convert()
                if (movie.isComplete(Movie.Check.EXTRA)) {
                    return@defer Observable.just(movie)
                }
            }

            Observable.just(Movie.invalid())
        }
    }

    override fun search(title: String, page: Int): Observable<SearchResult> {
        return api.search(BuildConfig.OMDB_API_KEY, title, page)
                .map {
                    it.convert()
                }.map {
                    it.copy(movies = it.movies.map { preferenceAppliers.apply(it) })
                }.doOnNext {
                    it.movies.map { dao.insertSearch(it.convert()) }
                }
    }

    override fun getEpisodes(series: Movie, season: Int): Observable<Season> {
        if (series.type != Constants.TitleType.SERIES.type) {
            return Observable.error(Throwable("invalid title type ${series.type}"))
        }

        return getEpisodes(
                {
                    Observable.defer {
                        val dbEpisodes = dao.getEpisodesForSeason(series.imdbId, season)
                        Observable.just(dbEpisodes.convert(series, season))
                    }

                },
                {
                    api.getEpisodesList(BuildConfig.OMDB_API_KEY, series.imdbId, season)
                            .map { it.copy(episodes = it.episodes.map { it.update(series.imdbId, season) }) }
                            .doOnNext {
                                it.episodes.map {
                                    it.convert()
                                }.forEach { dao.insert(it) }
                            }
                }
        )
    }

    override fun getEpisode(episode: Episode): Observable<Movie> {
        return getEpisode(
                { getMovieFromDbWithImdb(episode.imdbId) },
                {
                    api.getEpisode(BuildConfig.OMDB_API_KEY, episode.seriesId, episode.season, episode.episode)
                            .map { it.convert() }
                            .doOnNext {
                                dao.insert(it.convert())
                            }
                }
        ).map {
            preferenceAppliers.apply(it)
        }
    }

    private fun getEpisodes(dbCall: () -> Observable<Season>,
                            apiCall: () -> Observable<Season>): Observable<Season> {

        return dbCall()
                .flatMap {
                    if (it.episodes.isNotEmpty()) {
                        Observable.just(it)
                    } else {
                        apiCall()
                    }
                }

    }

    private fun getEpisode(dbCall: () -> Observable<Movie>,
                           apiCall: () -> Observable<Movie>): Observable<Movie> {

        return dbCall()
                .flatMap {
                    if (it.imdbId.isNotEmpty()) {
                        Observable.just(it)
                    } else {
                        apiCall()
                    }
                }

    }

    override fun addPreferenceApplier(applier: UserPreferenceApplier) {
        preferenceAppliers.add(applier)
    }

}