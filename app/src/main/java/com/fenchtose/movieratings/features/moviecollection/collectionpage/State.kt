package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.content.Context
import android.net.Uri
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
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.offline.export.ExportProgress
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.*

data class CollectionPageState(
    val collection: MovieCollection = MovieCollection.invalid(),
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default,
    val collectionOp: MovieCollectionOp? = null,
    val shareError: Boolean? = null,
    val removed: Removed? = null) {

    val active = collection.id != -1L
}

data class Removed(
    val movie: Movie,
    val index: Int,
    val show: Boolean
)

data class InitCollectionPage(val collection: MovieCollection): Action
object ClearCollectionPage: Action
object LoadCollection: Action
object ClearShareError: Action
object ClearCollectionOp: Action

data class CollectionSort(val collectionId: Long, val sort: Sort): Action
const val COLLECTION_PAGE = "collection_page"

sealed class MovieCollectionOp(val collection: MovieCollection, val movie: Movie) {
    class Exists(collection: MovieCollection, movie: Movie): MovieCollectionOp(collection, movie)
    class Added(collection: MovieCollection, movie: Movie): MovieCollectionOp(collection, movie)
    class AddError(collection: MovieCollection, movie: Movie): MovieCollectionOp(collection, movie)
    class Removed(collection: MovieCollection, movie: Movie): MovieCollectionOp(collection, movie)
    class RemoveError(collection: MovieCollection, movie: Movie): MovieCollectionOp(collection, movie)
}

fun AppState.reduceCollectionPage(action: Action): AppState {
    return reduceChild(collectionPages, action, {reduce(it)}, {copy(collectionPages = it)})
}

private fun List<CollectionPageState>.reduce(action: Action): List<CollectionPageState> {
    if (action is InitCollectionPage) {
        return push(CollectionPageState(collection = action.collection))
    }

    if (isEmpty()) {
        return this
    }

    if (action === ClearCollectionPage) {
        return pop()
    }

    return swapLastIfUpdated(last().reduce(action))
}

private fun CollectionPageState.reduce(action: Action): CollectionPageState {
    return when {
        action is BaseMovieListPageAction -> when(action) {
            is BaseMovieListPageAction.Loading -> copy(progress = Progress.Loading)
            is BaseMovieListPageAction.Loaded -> copy(progress = Progress.Success, movies = action.movies)
            is BaseMovieListPageAction.Error -> copy(progress = Progress.Error)
        }
        action is CollectionSort -> copy(movies = movies.sort(action.sort))
        action is DataExporter.Progress.Error<*> -> copy(shareError = true)
        action === ClearShareError -> copy(shareError = null)
        action === ClearCollectionOp -> copy(collectionOp = null)
        else -> this
    }

}

class CollectionPageMiddleware(
        private val context: Context,
        private val provider: MovieCollectionProvider,
        private val rxHooks: RxHooks,
        likeStore: LikeStore,
        private val userPreferences: UserPreferences) {

    init {
        provider.addPreferenceApplier(likeStore)
    }

    fun middleware(appState: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (appState.collectionPages.isEmpty()) {
            return next(appState, action, dispatch)
        }

        val state = appState.collectionPages.last()
        if (action === LoadCollection) {
            if (state.active && state.movies.isEmpty()) {
                load(state.collection, dispatch)
                return BaseMovieListPageAction.Loading(COLLECTION_PAGE)
            }
        } else if (action is CollectionSort && state.collection.id == action.collectionId) {
            userPreferences.setLatestCollectionSort(action.collectionId, action.sort)
        } else if (action is ExportProgress && action.key == COLLECTION_PAGE && action.progress is DataExporter.Progress.Success<*>) {
            val uri = action.progress.output as Uri?
            uri?.let {
                IntentUtils.openShareFileIntent(context, it)
            }
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
                    context,
                    DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                    AppRxHooks(),
                    DbLikeStore.getInstance(MovieRatingsApplication.database.favDao()),
                    SettingsPreferences(context)
            )
        }
    }
}