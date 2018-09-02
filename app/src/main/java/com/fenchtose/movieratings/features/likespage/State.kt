package com.fenchtose.movieratings.features.likespage

import android.content.Context
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageAction
import com.fenchtose.movieratings.features.baselistpage.Progress
import com.fenchtose.movieratings.model.api.provider.DbFavoriteMovieProvider
import com.fenchtose.movieratings.model.api.provider.FavoriteMovieProvider
import com.fenchtose.movieratings.model.db.like.MovieLiked
import com.fenchtose.movieratings.model.entity.*
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks
import com.fenchtose.movieratings.util.add

data class LikesPageState(
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default,
    val unliked: Unliked? = null) {

    val active: Boolean = progress != Progress.Default
}

data class Unliked(
    val movie: Movie,
    val index: Int,
    val shown: Boolean = false
)

object ClearLikedPageState: Action
object LoadLikedMovies: Action
object UndoShown: Action

data class LikeSort(val sort: Sort): Action

const val LIKES_PAGE = "lp"

fun AppState.reduceLikesPage(action: Action): AppState {
    return reduceChild(likesPage, action, {reduce(action)}, {copy(likesPage = it)})
}

private fun LikesPageState.reduce(action: Action): LikesPageState {
    if (action === ClearLikedPageState) {
        return LikesPageState()
    } else if (action is BaseMovieListPageAction) {
        if (action.page == LIKES_PAGE) {
            return when(action) {
                is BaseMovieListPageAction.Loading -> copy(progress = Progress.Loading)
                is BaseMovieListPageAction.Loaded -> copy(progress = Progress.Success, movies = action.movies)
                is BaseMovieListPageAction.Error -> copy(progress = Progress.Error)
            }
        }
    } else if (action == UndoShown) {
        return copy(unliked = unliked?.copy(shown = true))
    } else if (action is LikeSort) {
        return copy(movies = movies.sort(action.sort))
    }

    if (active) {
        if (action is MovieLiked) {
            return if (action.movie.liked) {
                copy(movies = movies.add(action.movie,
                        if (unliked == null || unliked.index < 0 || action.movie.imdbId != unliked.movie.imdbId) movies.size else unliked.index, false),
                        unliked = null)
            } else {
                copy(movies = movies.remove(action.movie), unliked = Unliked(action.movie, movies.hasMovie(action.movie)))
            }
        }
    }

    return this
}

class LikesPageMiddleware(private val provider: FavoriteMovieProvider,
                          private val rxHooks: RxHooks,
                          private val userPreferences: UserPreferences) {

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action === LoadLikedMovies && state.likesPage.movies.isEmpty()) {
            load(dispatch)
            return BaseMovieListPageAction.Loading(LIKES_PAGE)
        } else if (action is LikeSort) {
            userPreferences.setLatestLikeSort(action.sort)
        }

        return next(state, action, dispatch)
    }

    private fun load(dispatch: Dispatch) {
        provider.getMovies()
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    val sort = userPreferences.getLatestLikeSort()
                    dispatch(BaseMovieListPageAction.Loaded(LIKES_PAGE, it.sort(sort)))
                }, {
                    it.printStackTrace()
                    dispatch(BaseMovieListPageAction.Error(LIKES_PAGE))
                })
    }

    companion object {
        fun newInstance(context: Context): LikesPageMiddleware {
            return LikesPageMiddleware(
                    DbFavoriteMovieProvider(MovieRatingsApplication.database.movieDao()),
                    AppRxHooks(),
                    SettingsPreferences(context)
            )
        }
    }

}