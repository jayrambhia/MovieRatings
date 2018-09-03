package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.content.Context
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPageAction
import com.fenchtose.movieratings.features.baselistpage.Progress
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.DbLikeStore
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.Sort
import com.fenchtose.movieratings.model.entity.sort
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks

data class CollectionPageState(
    val collection: MovieCollection = MovieCollection.invalid(),
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default) {

    val active = collection.id != -1L
}

data class InitCollectionPage(val collection: MovieCollection): Action
object ClearCollectionPage: Action
object LoadCollection: Action

data class CollectionSort(val collectionId: Long, val sort: Sort): Action
const val COLLECTION_PAGE = "collection_page"

fun AppState.reduceCollectionPage(action: Action): AppState {
    return reduceChild(collectionPage, action, {reduce(it)}, {copy(collectionPage = it)})
}

private fun CollectionPageState.reduce(action: Action): CollectionPageState {
    return when {
        action === ClearCollectionPage -> CollectionPageState()
        action is InitCollectionPage -> copy(collection = action.collection)
        action is BaseMovieListPageAction -> when(action) {
            is BaseMovieListPageAction.Loading -> copy(progress = Progress.Loading)
            is BaseMovieListPageAction.Loaded -> copy(progress = Progress.Success, movies = action.movies)
            is BaseMovieListPageAction.Error -> copy(progress = Progress.Error)
        }
        action is CollectionSort -> copy(movies = movies.sort(action.sort))
        else -> this
    }

}

class CollectionPageMiddleware(val provider: MovieCollectionProvider,
                               val rxHooks: RxHooks,
                               likeStore: LikeStore,
                               private val userPreferences: UserPreferences) {
    init {
        provider.addPreferenceApplier(likeStore)
    }

    fun middleware(appState: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        val state = appState.collectionPage
        if (action === LoadCollection) {
            if (state.active && state.movies.isEmpty()) {
                load(state.collection, dispatch)
                return BaseMovieListPageAction.Loading(COLLECTION_PAGE)
            }
        } else if (action is CollectionSort && state.collection.id == action.collectionId) {
            userPreferences.setLatestCollectionSort(action.collectionId, action.sort)
        }

        return next(appState, action, dispatch)
    }

    private fun load(collection: MovieCollection, dispatch: Dispatch) {
        provider.getMoviesForCollection(collection)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .map {
                    it.sort(userPreferences.getLatestCollectionSort(collection.id))
                }.subscribe({
                    dispatch(BaseMovieListPageAction.Loaded(COLLECTION_PAGE, it))
                }, {
                    it.printStackTrace()
                    dispatch(BaseMovieListPageAction.Error(COLLECTION_PAGE))
                })
    }

    companion object {
        fun newInstance(context: Context): CollectionPageMiddleware {
            return CollectionPageMiddleware(
                    DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                    AppRxHooks(),
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                    SettingsPreferences(context)
            )
        }
    }
}