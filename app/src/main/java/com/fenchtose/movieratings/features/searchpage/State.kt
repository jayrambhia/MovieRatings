package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.*
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.like.MovieLiked
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.hasMovie
import com.fenchtose.movieratings.model.entity.updateMovie
import com.fenchtose.movieratings.util.addAll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Collections

data class SearchPageState(
        val query: String = "",
        val progress: Progress = Progress.Default,
        val movies: List<Movie> = Collections.emptyList(),
        val page: Int = 0
) {
    override fun toString(): String {
        return "SearchPageState(query='$query', progress=$progress, movies=${movies.size}, page=$page)"
    }
}

sealed class Progress {
    object Default: Progress()
    data class Loading(val query: String): Progress()
    object Error: Progress()

    sealed class Success(val movies: List<Movie>, val page: Int): Progress() {
        class Loaded(movies: List<Movie>, page: Int): Success(movies, page)
        class Pagination(movies: List<Movie>, page: Int): Success(movies, page)
    }

    object Paginating: Progress()
    object PaginationError: Progress()
}

sealed class SearchAction: Action {
    object Clear: SearchAction()
    data class Search(val query: String): SearchAction()
    object LoadMore: SearchAction()
    data class Result(val progress: Progress): SearchAction()
}

fun searchPageReducer(state: AppState, action: Action): AppState {
    return when(action) {
        is SearchAction -> reduceChildState(state, state.searchPage, action, ::reduce, {s, c -> s.copy(searchPage = c)})
        else -> state
    }
}

private fun reduce(state: SearchPageState, action: Action): SearchPageState {
    return when(action) {
        is SearchAction -> {
            when(action) {
                is SearchAction.Search -> state // NO-OP
                is SearchAction.Result -> {
                    val progress = action.progress
                    when(progress) {
                        is Progress.Loading -> state.copy(query = progress.query, progress = progress)
                        is Progress.Success.Loaded -> state.copy(progress = progress, movies = progress.movies, page = progress.page)
                        is Progress.Success.Pagination -> state.copy(progress = progress, movies = state.movies.addAll(progress.movies), page = progress.page)
                        else -> state.copy(progress = progress)
                    }
                }
                is SearchAction.Clear -> SearchPageState()
                is SearchAction.LoadMore -> state // NO-OP
            }
        }
        is MovieLiked -> {
            if (state.movies.hasMovie(action.movie) != -1) {
                   state.copy(movies = state.movies.updateMovie(action.movie))
            } else {
                state
            }
        }
        else -> state
    }
}

class SearchMiddleWare(private val provider: MovieProvider,
                       likeStore: LikeStore) {

    init {
        provider.addPreferenceApplier(likeStore)
    }

    fun searchMiddleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        return when(action) {
            is SearchAction.Search -> {
                if (state.searchPage.query == action.query && state.searchPage.movies.isNotEmpty()) {
                    NoAction
                } else {
                    makeApiCall(action.query, 1, dispatch)
                    SearchAction.Result(Progress.Loading(action.query))
                }
            }

            is SearchAction.LoadMore -> {
                makeApiCall(state.searchPage.query, state.searchPage.page + 1, dispatch)
                SearchAction.Result(Progress.Paginating)
            }

            else -> next(state, action, dispatch)
        }
    }

    private fun makeApiCall(query: String, page: Int, dispatch: Dispatch) {
        provider.search(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    if (it.error.isNotEmpty()) {
                        dispatch(SearchAction.Result(if (page == 1) Progress.Error else Progress.PaginationError))
                    } else {
                        dispatch(SearchAction.Result(if (page == 1) Progress.Success.Loaded(it.results, page) else Progress.Success.Pagination(it.results, page)))
                    }
                }, {
                    it.printStackTrace()
                    dispatch(SearchAction.Result(if (page == 1) Progress.Error else Progress.Paginating))
                })
    }

    companion object {
        fun newInstance(): SearchMiddleWare {
            return SearchMiddleWare(
                    MovieRatingsApplication.movieProviderModule.movieProvider,
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()))
        }
    }
}

