package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.router.ResultBus
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPageFragment
import com.fenchtose.movieratings.features.season.SeasonPageFragment
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RecentlyBrowsedStore
import com.fenchtose.movieratings.model.entity.*
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.IntentUtils
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
                updateState(MoviePage.State.LoadImage(it.poster))
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
                        getEpisodes(it, if (currentSeason <= 0) 1 else currentSeason)
                    }
                }
                .subscribe({
                    showMovie(it)
                }, {
                    loadedMovie = null
                    it.printStackTrace()
                    updateState(MoviePage.State.Error())
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
                        updateState(MoviePage.EpisodeState.Success(it))
                    } else {
                        episodes = null
                        updateState(MoviePage.EpisodeState.Error())
                    }
                },{
                    episodes = null
                    updateState(MoviePage.EpisodeState.Error())
                    it.printStackTrace()
                })

        subscribe(d)
        updateState(MoviePage.EpisodeState.Loading())
    }

    private fun showMovie(movie: Movie) {
        loadedMovie = movie
        updateState(MoviePage.State.Success(movie))
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
                        updateState(MoviePage.CollectionState.Exists(collection))
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
                    updateState(MoviePage.CollectionState.Added(collection))
                    getMovie(movie.imdbId)
                }, {
                    it.printStackTrace()
                    updateState(MoviePage.CollectionState.Error(collection))
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

    override fun saveState(): PresenterState? {
        return MovieState(currentSeason)
    }

    override fun restoreState(state: PresenterState?) {
        state?.takeIf { it is MovieState }
                ?.let { it as MovieState }
                ?.let {
                    this.currentSeason = it.currentSeason
                }
    }

    fun openImdb(context: Context) {
        IntentUtils.openImdb(context, loadedMovie?.imdbId)
    }

    data class MovieState(val currentSeason: Int): PresenterState
}