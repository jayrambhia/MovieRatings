package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import androidx.annotation.MainThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.*
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.DbMovieCollectionStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.RegisterMovie
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.Season
import com.fenchtose.movieratings.util.*
import java.lang.ref.WeakReference
import kotlin.math.max

data class MoviePageState(
    val movieId: String = "",
    val progress: Progress = Progress.Default,
    val movie: Movie = Movie.invalid(),
    val currentSeason: Int = -1,
    val seasonProgress: SeasonProgress = SeasonProgress.Default,
    val season: Season? = null
)

sealed class Progress {
    object Default: Progress()
    object Loading: Progress()
    object Error: Progress()
    object Loaded: Progress()
}

sealed class SeasonProgress {
    object Default: SeasonProgress()
    object Loading: SeasonProgress()
    object Error: SeasonProgress()
    object Loaded: SeasonProgress()
}

data class InitMoviePage(val imdbId: String, val movie: Movie?): Action
object ClearMoviePage: Action
class OpenImdbPage(val contextRef: WeakReference<Context>): Action

data class LoadMovie(val imdbId: String): Action
data class UpdateProgress(val progress: Progress): Action
data class MovieLoaded(val movie: Movie): Action
data class LoadSeason(val series: Movie, val season: Int): Action
data class SeasonLoaded(val season: Season): Action
data class UpdateSeasonProgress(val progress: SeasonProgress): Action

fun AppState.reduceMoviePage(action: Action) = reduceChild(moviePages, action, {reduce(it)}, {copy(moviePages = it)})

private fun List<MoviePageState>.reduce(action: Action): List<MoviePageState> {
    if (action is InitMoviePage) {
        return push(MoviePageState(movieId = action.imdbId, movie = action.movie?: Movie.invalid()))
    }

    if (isEmpty()) {
        return this
    }

    if (action == ClearMoviePage) {
        return pop()
    }

    return swapLastIfUpdated(last().reduce(action))
}

private fun MoviePageState.reduce(action: Action): MoviePageState {
    return when(action) {
        is ClearMoviePage -> MoviePageState()
        is MovieLoaded -> if (movieId == action.movie.imdbId) copy(movie=action.movie, progress = Progress.Loaded) else this
        is SeasonLoaded -> if (movieId == action.season.seriesId) copy(season = action.season, seasonProgress = SeasonProgress.Loaded) else this
        else -> this
    }
}

class MoviePageMiddleware(private val provider: MovieProvider,
                          likeStore: LikeStore,
                          collectionStore: MovieCollectionStore,
                          private val rxHooks: RxHooks) {

    init {
        provider.addPreferenceApplier(likeStore)
        provider.addPreferenceApplier(collectionStore)
    }

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (state.moviePages.isEmpty()) {
            return next(state, action, dispatch)
        }

        val moviePage = state.moviePages.last()
        if (action is LoadMovie) {
            if (moviePage.movieId == action.imdbId) {
                if (moviePage.progress == Progress.Default) {
                    getMovie(action.imdbId, moviePage.currentSeason, dispatch)
                    return UpdateProgress(Progress.Loading)
                }
            }
        } else if (action is LoadSeason) {
            if (moviePage.movieId == action.series.imdbId && moviePage.season?.season != action.season) {
                loadSeason(action.series, action.season, dispatch)
                return UpdateSeasonProgress(SeasonProgress.Loading)
            }
        } else if (action is OpenImdbPage) {
            if (moviePage.movieId.isNotEmpty()) {
                action.contextRef.get()?.let {
                    IntentUtils.openImdb(it, moviePage.movieId, false)
                    return NoAction
                }
            }
        }

        return next(state, action, dispatch)
    }

    @MainThread
    private fun getMovie(imdbId: String, currentSeason: Int, dispatch: Dispatch) {
        provider.getMovieWithImdb(imdbId)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .doAfterNext {
                    if (it.type == Constants.TitleType.SERIES.type) {
                        dispatch(LoadSeason(it, max(currentSeason, 1)))
                    }
                }.subscribe({
                    dispatch(MovieLoaded(it))
                    dispatch(RegisterMovie(it))
                },{
                    it.printStackTrace()
                    dispatch(UpdateProgress(Progress.Error))
                })
    }

    @MainThread
    private fun loadSeason(series: Movie, season: Int, dispatch: Dispatch) {
        provider.getEpisodes(series, season)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(SeasonLoaded(it))
                }, {
                    dispatch(UpdateSeasonProgress(SeasonProgress.Error))
                })

    }

    companion object {
        fun newInstance(): MoviePageMiddleware {
            return MoviePageMiddleware(MovieRatingsApplication.movieProviderModule.movieProvider,
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                    DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                    AppRxHooks())
        }
    }
}