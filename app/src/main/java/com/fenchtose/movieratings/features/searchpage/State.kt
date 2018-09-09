package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.analytics.ga.GaCategory
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.*
import com.fenchtose.movieratings.features.moviecollection.collectionpage.MovieCollectionOp
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.like.MovieLiked
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.hasMovie
import com.fenchtose.movieratings.model.entity.updateMovie
import com.fenchtose.movieratings.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Collections

data class SearchPageState(
        val query: String = "",
        val fixScroll: Boolean = false,
        val progress: Progress = Progress.Default,
        val movies: List<Movie> = Collections.emptyList(),
        val page: Int = 0) {
    override fun toString(): String {
        return "SearchPageState(query='$query', progress=$progress, movies=${movies.size}, page=$page)"
    }
}

data class CollectionSearchPageState(
        val searchPageState: SearchPageState = SearchPageState(),
        val collection: MovieCollection = MovieCollection.invalid(),
        val collectionOp: MovieCollectionOp? = null
        )

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

sealed class SearchAction(val addToCollection: Boolean): Action {
    class ClearSearch(addToCollection: Boolean): SearchAction(addToCollection)
    class Search(val query: String, addToCollection: Boolean): SearchAction(addToCollection) {
        override fun toString(): String {
            return "Search(query='$query', collection=$addToCollection)"
        }
    }
    class LoadMore(addToCollection: Boolean): SearchAction(addToCollection)
    class Result(val progress: Progress, addToCollection: Boolean): SearchAction(addToCollection)
    class Reload(val query: String, addToCollection: Boolean): SearchAction(addToCollection)
    class ScrollFixed(addToCollection: Boolean): SearchAction(addToCollection)
}

data class InitCollectionSearchPage(val collection: MovieCollection): Action
object ClearCollectionSearchPage: Action
object ClearCollectionOp: Action


fun AppState.searchPageReducer(action: Action): AppState {
    return when(action) {
        is SearchAction ->
            if (!action.addToCollection) {
                reduceChild(searchPage, action, {reduce(it)}, {copy(searchPage=it)})
            } else {
                reduceChild(collectionSearchPages, action, {reduce(it)}, {copy(collectionSearchPages=it)})
            }
        is InitCollectionSearchPage, is ClearCollectionSearchPage, is ClearCollectionOp ->
            reduceChild(collectionSearchPages, action, {reduce(it)}, {copy(collectionSearchPages=it)})
        else -> this
    }
}

private fun SearchPageState.reduce(action: Action): SearchPageState {
    return when(action) {
        is SearchAction -> {
            when(action) {
                is SearchAction.Search -> this // NO-OP
                is SearchAction.Result -> {
                    val progress = action.progress
                    when(progress) {
                        is Progress.Loading -> copy(query = progress.query, progress = progress)
                        is Progress.Success.Loaded -> copy(progress = progress, movies = progress.movies, page = progress.page, fixScroll = true)
                        is Progress.Success.Pagination -> copy(progress = progress, movies = movies.addAll(progress.movies), page = progress.page)
                        else -> copy(progress = progress)
                    }
                }
                is SearchAction.ClearSearch -> SearchPageState()
                is SearchAction.LoadMore -> this // NO-OP
                is SearchAction.Reload -> this // NO-OP
                is SearchAction.ScrollFixed -> copy(fixScroll = false)
            }
        }
        is MovieLiked -> {
            if (movies.hasMovie(action.movie) != -1) {
                   copy(movies = movies.updateMovie(action.movie))
            } else {
                this
            }
        }
        else -> this
    }
}

private fun List<CollectionSearchPageState>.reduce(action: Action): List<CollectionSearchPageState> {
    if (action is InitCollectionSearchPage) {
        return push(CollectionSearchPageState(collection = action.collection))
    }

    if (isEmpty()) {
        return this
    }

    if (action === ClearCollectionSearchPage) {
        return pop()
    }

    val updated = last().reduce(action)
    return swapLastIfUpdated(updated)
}

private fun CollectionSearchPageState.reduce(action: Action): CollectionSearchPageState {
    return when(action) {
        is SearchAction -> reduceChild(searchPageState, action, {reduce(it)}, {copy(searchPageState = it)})
        is InitCollectionSearchPage -> copy(searchPageState = SearchPageState(), collection = action.collection)
        is ClearCollectionSearchPage -> CollectionSearchPageState()
        is ClearCollectionOp -> copy(collectionOp = null)
        else -> this
    }
}

class SearchMiddleWare(private val provider: MovieProvider,
                       likeStore: LikeStore) {

    init {
        provider.addPreferenceApplier(likeStore)
    }

    fun searchMiddleware(appState: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        return when(action) {
            is SearchAction.Search -> {
                if (action.addToCollection) {
                    if (!appState.collectionSearchPages.isEmpty()) {
                        val state = appState.collectionSearchPages.last()
                        if (state.searchPageState.query == action.query && state.searchPageState.movies.isNotEmpty()) {
                            NoAction
                        } else {
                            GaEvents.SEARCH.withCategory(GaCategory.COLLECTION_SEARCH).withLabel(action.query).track()
                            makeApiCall(action.query, 1, true, dispatch)
                            SearchAction.Result(Progress.Loading(action.query), true)
                        }
                    } else {
                        NoAction
                    }

                } else {
                    if (appState.searchPage.query == action.query && appState.searchPage.movies.isNotEmpty()) {
                        NoAction
                    } else {
                        GaEvents.SEARCH.withCategory(GaCategory.SEARCH).withLabel(action.query).track()
                        makeApiCall(action.query, 1, false, dispatch)
                        SearchAction.Result(Progress.Loading(action.query), false)
                    }
                }
            }

            is SearchAction.LoadMore -> {
                if (action.addToCollection) {
                    if (!appState.collectionSearchPages.isEmpty()) {
                        val state = appState.collectionSearchPages.last()
                        GaEvents.SEARCH_MORE.withCategory(GaCategory.COLLECTION_SEARCH).withLabelArg(state.searchPageState.page + 1).track()
                        makeApiCall(state.searchPageState.query, state.searchPageState.page + 1, true, dispatch)
                        SearchAction.Result(Progress.Paginating, true)
                    } else {
                        NoAction
                    }
                } else {
                    GaEvents.SEARCH_MORE.withCategory(GaCategory.SEARCH).withLabelArg(appState.searchPage.page + 1).track()
                    makeApiCall(appState.searchPage.query, appState.searchPage.page + 1, action.addToCollection, dispatch)
                    SearchAction.Result(Progress.Paginating, false)
                }
            }

            is SearchAction.Reload -> {
                if (action.query.isBlank()) {
                    NoAction
                } else {
                    makeApiCall(action.query, 1, action.addToCollection, dispatch)
                    SearchAction.Result(Progress.Loading(action.query), action.addToCollection)
                }
            }

            else -> next(appState, action, dispatch)
        }
    }

    private fun makeApiCall(query: String, page: Int, addToCollection: Boolean, dispatch: Dispatch) {
        provider.search(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    if (it.error != null) {
                        dispatch(SearchAction.Result(if (page == 1) Progress.Error else Progress.PaginationError, addToCollection))
                    } else {
                        dispatch(SearchAction.Result(if (page == 1) Progress.Success.Loaded(it.movies, page) else Progress.Success.Pagination(it.movies, page), addToCollection))
                    }
                }, {
                    it.printStackTrace()
                    dispatch(SearchAction.Result(if (page == 1) Progress.Error else Progress.Paginating, addToCollection))
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

