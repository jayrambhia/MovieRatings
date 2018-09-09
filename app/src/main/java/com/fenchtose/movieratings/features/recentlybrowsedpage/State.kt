package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.*
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageAction
import com.fenchtose.movieratings.features.baselistpage.Progress
import com.fenchtose.movieratings.model.api.provider.DbRecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.api.provider.RecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.recentlyBrowsed.MovieRegsitered
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.remove
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks
import com.fenchtose.movieratings.util.add

data class RecentlyBrowsedState(
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default
) {
    val active: Boolean = progress != Progress.Default
}

object ClearRecentlyBrowsedState: Action
object LoadRecentlyBrowsedMovies: Action

const val RECENTLY_BROWSED_PAGE = "rbp"

fun AppState.recentlyBrowsedPageReducer(action: Action): AppState {
    return reduceChild(recentlyBrowsedPage, action, {reduce(action)}, {copy(recentlyBrowsedPage = it)})
}

private fun RecentlyBrowsedState.reduce(action: Action): RecentlyBrowsedState {
    return when(action) {
        is ClearRecentlyBrowsedState -> RecentlyBrowsedState()
        is BaseMovieListPageAction -> {
            if (action.page == RECENTLY_BROWSED_PAGE) {
                when(action) {
                    is BaseMovieListPageAction.Loading -> copy(progress = Progress.Loading)
                    is BaseMovieListPageAction.Loaded -> copy(progress = Progress.Success, movies = action.movies)
                    is BaseMovieListPageAction.Error -> copy(progress = Progress.Error)
                }
            } else {
                this
            }
        }
        is MovieRegsitered -> {
            if (active) {
                val movies = movies.remove(action.movie).add(action.movie, 0)
                copy(movies = movies.remove(action.movie).add(action.movie, 0))
            } else {
                this
            }
        }

        else -> this
    }
}

class RecentlyBrowsedPageMiddleware(private val provider: RecentlyBrowsedMovieProvider,
                                    private val rxHooks: RxHooks,
                                    likeStore: LikeStore) {
    init {
        provider.addPreferenceApplier(likeStore)
    }

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action === LoadRecentlyBrowsedMovies && state.recentlyBrowsedPage.movies.isEmpty()) {
            load(dispatch)
            return BaseMovieListPageAction.Loading(RECENTLY_BROWSED_PAGE)
        }

        return next(state, action, dispatch)
    }

    private fun load(dispatch: Dispatch) {
        provider.getMovies()
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(BaseMovieListPageAction.Loaded(RECENTLY_BROWSED_PAGE, it))
                },{
                    it.printStackTrace()
                    dispatch(BaseMovieListPageAction.Error(RECENTLY_BROWSED_PAGE))
                })
    }

    companion object {
        fun newInstance(): RecentlyBrowsedPageMiddleware {
            return RecentlyBrowsedPageMiddleware(
                    DbRecentlyBrowsedMovieProvider(MovieRatingsApplication.database.movieDao()),
                    AppRxHooks(),
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao())
            )
        }
    }
}