package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.router.ResultBus
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPageFragment
import com.fenchtose.movieratings.features.season.SeasonPageFragment
import com.fenchtose.movieratings.model.*
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MoviePresenter(private val provider: MovieProvider,
                     private val likeStore: LikeStore,
                     private val recentStore: RecentlyBrowsedStore,
                     private val collectionStore: MovieCollectionStore,
                     private val preferences: UserPreferences,
                     private val imdbId: String?,
                     private val passedMovie: Movie?): Presenter<MoviePage>(), SeasonSelector {

    private var loadedMovie: Movie? = null
    private var selectedCollection: MovieCollection? = null
    private var currentSeason: Int = -1
    private var episodes: EpisodesList? = null

    init {
        provider.addPreferenceApplier(likeStore)
        provider.addPreferenceApplier(collectionStore)
    }

    override fun attachView(view: MoviePage) {
        super.attachView(view)
        imdbId?.let {
            loadMovie(it)
        }

        val d = ResultBus.subscribe(CollectionListPageFragment.CollectionListPagePath.SELECTED_COLLECTION)
                .subscribe {
                    ResultBus.clearResult(CollectionListPageFragment.CollectionListPagePath.SELECTED_COLLECTION)
                    it.getResult().takeIf { it is MovieCollection }?.let {
                        selectedCollection = it as MovieCollection
                        loadedMovie?.let {
                            addedToCollection(selectedCollection!!, it)
                        }
                    }
                }

        subscribe(d)
    }

    private fun loadMovie(imdbId: String) {
        passedMovie?.let {
            @Suppress("UselessCallOnNotNull")
            if (it.isComplete(Movie.Check.USER_PREFERENCES)) {
                showMovie(passedMovie)
                return
            } else if (!it.poster.isNullOrEmpty()) {
                updateState(MoviePage.State(MoviePage.Ui.LOAD_IMAGE, it))
            }
        }

        getMovie(imdbId)

    }

    private fun getMovie(imdbId: String) {
        val d = provider
                .getMovieWithImdb(imdbId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterNext {
                    if (it.type == Constants.TitleType.SERIES.type) {
                        getEpisodes(it, 1)
                    }
                }
                .subscribe({
                    showMovie(it)
                }, {
                    loadedMovie = null
                    it.printStackTrace()
                    updateState(MoviePage.State(MoviePage.Ui.ERROR))
                })

        subscribe(d)
    }

    override fun selectSeason(season: Int) {
        season.takeIf { it != currentSeason }?.let {
            loadedMovie?.let {
                it.takeIf { it.type == Constants.TitleType.SERIES.type }?.let {
                    getEpisodes(it, season)
                }
            }
        }
    }

    private fun getEpisodes(series: Movie, season: Int) {
        val d = provider
                .getEpisodes(series, season)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.success) {
                        currentSeason = it.season
                        episodes = it
                        updateState(MoviePage.EpisodeState(MoviePage.EpisodeUi.LOADED, it))
                    } else {
                        episodes = null
                        updateState(MoviePage.EpisodeState(MoviePage.EpisodeUi.ERROR))
                    }
                },{
                    episodes = null
                    updateState(MoviePage.EpisodeState(MoviePage.EpisodeUi.ERROR))
                    it.printStackTrace()
                })

        subscribe(d)
        updateState(MoviePage.EpisodeState(MoviePage.EpisodeUi.LOADING))
    }

    private fun showMovie(movie: Movie) {
        loadedMovie = movie
        updateState(MoviePage.State(MoviePage.Ui.LOADED, movie))
        updateRecent(movie.imdbId)
        selectedCollection?.let {
            addedToCollection(it, movie)
        }
    }

    private fun updateRecent(imdbId: String) {
        if (!preferences.isAppEnabled(UserPreferences.SAVE_HISTORY)) {
            return
        }

        val recent = RecentlyBrowsed()
        recent.id = imdbId
        recent.timestamp = System.currentTimeMillis()
        recentStore.update(recent)
    }

    private fun addedToCollection(collection: MovieCollection, movie: Movie) {
        val d = collectionStore.isMovieAddedToCollection(collection, movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (it) {
                        updateState(MoviePage.CollectionState(MoviePage.CollectionUi.EXISTS, collection))
                    }
                }.filter {
                    !it
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    collectionStore.addMovieToCollection(collection, movie)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateState(MoviePage.CollectionState(MoviePage.CollectionUi.ADDED, collection))
                    getMovie(movie.imdbId)
                }, {
                    it.printStackTrace()
                    updateState(MoviePage.CollectionState(MoviePage.CollectionUi.ERROR, collection))
                })
        subscribe(d)
    }

    private fun updateState(state: MoviePage.State) {
        getView()?.updateState(state)
    }

    private fun updateState(state: MoviePage.CollectionState) {
        getView()?.updateState(state)
        selectedCollection = null
    }

    private fun updateState(state: MoviePage.EpisodeState) {
        getView()?.updateState(state)
    }

    fun likeToggle(): Boolean {
        loadedMovie?.let {
            it.liked = !it.liked
            likeStore.setLiked(it.imdbId, it.liked)
            return it.liked
        }

        return false
    }

    fun addToCollection() {
        MovieRatingsApplication.router?.go(CollectionListPageFragment.CollectionListPagePath(true))
    }

    override fun openEpisode(episode: Episode) {
        loadedMovie?.let {
            series -> run {
                episodes?.let {
                    MovieRatingsApplication.router?.go(SeasonPageFragment.SeasonPath(series, it, episode.episode))
                }
            }
        }
    }
}