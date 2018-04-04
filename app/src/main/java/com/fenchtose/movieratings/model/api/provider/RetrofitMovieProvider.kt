package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.analytics.AnalyticsDispatcher
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.model.Episode
import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.api.MovieApi
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.util.Constants
import io.reactivex.Observable
import retrofit2.Retrofit

class RetrofitMovieProvider(retrofit: Retrofit, private val dao: MovieDao,
                            private val analytics: AnalyticsDispatcher?) : MovieProvider {

    private val api: MovieApi = retrofit.create(MovieApi::class.java)
    private val preferenceAppliers = ArrayList<UserPreferneceApplier>()

    override fun getMovieWithImdb(imdbId: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDbWithImdb(imdbId) },
                { api.getMovieInfoWithImdb(BuildConfig.OMDB_API_KEY, imdbId) },
                { analytics?.sendEvent(Event("get_movie_online").putAttribute("imdb", imdbId)) }
        )
    }

    override fun getMovie(title: String, year: String): Observable<Movie> {
        return getMovie(
                { this.getMovieFromDb(title, year) },
                { api.getMovieInfo(BuildConfig.OMDB_API_KEY, title, year) },
                { analytics?.sendEvent(Event("get_movie_online").putAttribute("title", title)) },
                { api.getMovieInfo(BuildConfig.OMDB_API_KEY, title)}
        )
    }

    private fun getMovie(dbCall: () -> Observable<Movie>,
                         apiCall: () -> Observable<Movie>,
                         analyticsCall: () -> Unit,
                         fallbackApiCall: (() -> Observable<Movie>)? = null): Observable<Movie> {

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
                                    .flatMap {
                                        if (it.imdbId.isNullOrEmpty() && fallbackApiCall != null) {
                                            fallbackApiCall.invoke()
                                        } else {
                                            Observable.just(it)
                                        }
                                    }
                                    .filter { it.isComplete(Movie.Check.BASE) }
                                    .doOnNext {
                                        dao.insert(it)
                                    }
                                }

                        }
                }
                .filter { it.isComplete(Movie.Check.BASE) }
                .doOnNext {
                    for (preferenceApplier in preferenceAppliers) {
                        preferenceApplier.apply(it)
                    }
                }

    }

    private fun getMovieFromDb(title: String, year: String): Observable<Movie> {
        return Observable.defer {
            val movie = if (year.isNotEmpty()) dao.getMovie(title, year) else dao.getMovie(title)
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

    override fun getEpisodes(series: Movie, season: Int): Observable<EpisodesList> {
        if (series.type != Constants.TitleType.SERIES.type) {
            return Observable.error(Throwable("invalid title type ${series.type}"))
        }

        return getEpisodes(
                {
                    Observable.defer {
                        val episodes = EpisodesList()
                        episodes.success = false

                        if (series.totalSeasons > 0) {
                            episodes.episodes.addAll(dao.getEpisodesForSeason(series.imdbId, season))
                            episodes.success = episodes.episodes.size > 0
                            episodes.title = series.title
                            episodes.season = season
                            episodes.totalSeasons = series.totalSeasons
                        }

                        Observable.just(episodes)
                    }

                },
                {
                    api.getEpisodesList(BuildConfig.OMDB_API_KEY, series.imdbId, season)
                            .doOnNext {
                                it.episodes.map {
                                    ep -> run {
                                    ep.seriesId = series.imdbId
                                    ep.season = it.season
                                    dao.insert(ep)
                                }
                                }
                            }
                }
        )
    }

    override fun getEpisode(episode: Episode): Observable<Movie> {
        return getEpisode(
                {
                    getMovieFromDbWithImdb(episode.imdbId)
                },
                {
                    api.getEpisode(BuildConfig.OMDB_API_KEY, episode.seriesId, episode.season, episode.episode)
                            .doOnNext {
                                dao.insert(it)
                            }
                }
        ).map {
            for (preferenceApplier in preferenceAppliers) {
                preferenceApplier.apply(it)
            }
            it
        }
    }

    private fun getEpisodes(dbCall: () -> Observable<EpisodesList>,
                            apiCall: () -> Observable<EpisodesList>): Observable<EpisodesList> {

        return dbCall()
                .flatMap {
                    if (it.success) {
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
                    if (it.id != -1) {
                        Observable.just(it)
                    } else {
                        apiCall()
                    }
                }

    }

    override fun addPreferenceApplier(applier: UserPreferneceApplier) {
        preferenceAppliers.add(applier)
    }

}