package com.fenchtose.movieratings.features.season.episode

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.entity.Episode
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks

data class EpisodePageState(
    val imdbId: String = "",
    val entry: Episode,
    val episode: Movie,
    val progress: Progress = Progress.Default
)

sealed class Progress {
    object Default: Progress()
    object Loading: Progress()
    object Loaded: Progress()
    object Error: Progress()
}

sealed class EpisodePageAction(val imdbId: String): Action {
    data class InitEpisodePage(val entry: Episode): EpisodePageAction(entry.imdbId)
    class LoadEpisodePage(imdbId: String): EpisodePageAction(imdbId)
    class ClearEpisodePage(imdbId: String): EpisodePageAction(imdbId)

    class UpdateProgress(imdbId: String, val progress: Progress): EpisodePageAction(imdbId)
    data class EpisodeLoaded(val episode: Movie): EpisodePageAction(episode.imdbId)
}

fun AppState.reduceEpisodes(action: Action): AppState {
    return reduceChild(episodePages, action, {reduce(it)}, {copy(episodePages = it)})
}

private fun Map<String, EpisodePageState>.reduce(action: Action): Map<String, EpisodePageState> {
    if (action is EpisodePageAction.InitEpisodePage) {
        val state = EpisodePageState(action.imdbId, action.entry, Movie.invalid())
        return plus(Pair(action.imdbId, state))
    }

    if (action is EpisodePageAction) {
        val state = get(action.imdbId) ?: return this
        if (action is EpisodePageAction.ClearEpisodePage) {
            return minus(action.imdbId)
        }

        val updated = state.reduce(action)
        if (state != updated) {
            return plus(Pair(updated.imdbId, updated))
        }
    }

    return this
}

private fun EpisodePageState.reduce(action: EpisodePageAction): EpisodePageState {
    return when(action) {
        is EpisodePageAction.UpdateProgress -> copy(progress = action.progress)
        is EpisodePageAction.EpisodeLoaded -> copy(progress = Progress.Loaded, episode = action.episode)
        else -> this
    }
}

class EpisodePageMiddleware(private val provider: MovieProvider,
                        private val rxHooks: RxHooks) {

    fun middleware(appState: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action is EpisodePageAction.LoadEpisodePage) {
            val state = appState.episodePages[action.imdbId]
            state?.let {
                load(it.entry, dispatch)
                return next(appState, EpisodePageAction.UpdateProgress(action.imdbId, Progress.Loading), dispatch)
            }
        }

        return next(appState, action, dispatch)
    }

    private fun load(entry: Episode, dispatch: Dispatch) {
        provider.getEpisode(entry)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(EpisodePageAction.EpisodeLoaded(it))
                }, {
                    it.printStackTrace()
                    dispatch(EpisodePageAction.UpdateProgress(entry.imdbId, Progress.Error))
                })
    }

    companion object {
        fun newInstance(): EpisodePageMiddleware {
            return EpisodePageMiddleware(
                    MovieRatingsApplication.movieProviderModule.movieProvider,
                    AppRxHooks()
            )
        }
    }

}