package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.content.Context
import android.net.Uri
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.base.redux.reduceChild
import com.fenchtose.movieratings.model.api.provider.DbMovieCollectionProvider
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.UpdateCollectionOp
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.offline.export.ExportProgress
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.IntentUtils
import com.fenchtose.movieratings.util.RxHooks
import com.fenchtose.movieratings.util.add

data class CollectionListPageState(
    val collections: List<MovieCollection> = listOf(),
    val progress: Progress = Progress.Default,
    val collectionOp: CollectionOp? = null,
    val shareSuccess: Boolean? = null) {

    val active: Boolean = progress != Progress.Default
}

object InitAction: Action
object ClearAction: Action
object LoadCollections: Action
object ClearCollectionOp: Action
object ClearShareError: Action

const val COLLECTION_LIST = "collection_list"

sealed class Progress {
    object Default: Progress()
    object Loading: Progress()
    object Loaded: Progress()
    object Error: Progress()
}

sealed class CollectionOp(val name: String) {
    class Created(name: String, val collection: MovieCollection): CollectionOp(name)
    class CreateError(name: String): CollectionOp(name)
    class Deleted(name: String): CollectionOp(name)
    class DeleteError(name: String): CollectionOp(name)
}

data class UpdateProgress(val progress: Progress): Action
data class CollectionsLoaded(val collections: List<MovieCollection>): Action

fun AppState.reduceCollectionListPage(action: Action): AppState {
    return reduceChild(collectionListPage, action, {reduce(it)}, {copy(collectionListPage = it)})
}

fun CollectionListPageState.reduce(action: Action): CollectionListPageState {
    if (action === InitAction || action === ClearAction) {
        return CollectionListPageState()
    }

    if (action is UpdateProgress) {
        return copy(progress = action.progress)
    }

    if (action is CollectionsLoaded) {
        return copy(collections = action.collections, progress = Progress.Loaded)
    }

    if (action is UpdateCollectionOp && active) {
        return when(action.op) {
            is CollectionOp.Created -> copy(collections = collections.add(action.op.collection), collectionOp = action.op)
            is CollectionOp.Deleted -> copy(collections = collections.remove(action.op.name), collectionOp = action.op)
            else -> copy(collectionOp = action.op)
        }
    }

    if (action === ClearCollectionOp && collectionOp != null) {
        return copy(collectionOp = null)
    }

    if (action is ExportProgress && action.progress.key == COLLECTION_LIST) {
        return copy(shareSuccess =  when(action.progress) {
            is DataExporter.Progress.Error -> false
            is DataExporter.Progress.Success -> true
            is DataExporter.Progress.Started -> null
        })
    }

    if (action === ClearShareError) {
        return copy(shareSuccess = null)
    }

    return this
}

fun List<MovieCollection>.remove(name: String): List<MovieCollection> {
    val index = hasCollection(name)
    if (index != -1) {
        return toMutableList().apply { removeAt(index) }
    }

    return this
}

fun List<MovieCollection>.hasCollection(name: String): Int {
    forEachIndexed {
        i, collection ->
        if (collection.name == name) {
            return i
        }
    }

    return -1
}

fun List<MovieCollection>.update(collection: MovieCollection): List<MovieCollection> {
    val index = hasCollection(collection.name)
    if (index != -1) {
        return toMutableList().apply {
            removeAt(index)
            add(kotlin.math.min(index, 0), collection)
        }
    }

    return this
}

class CollectionListPageMiddleware(
        private val context: Context,
        private val provider: MovieCollectionProvider,
        private val rxHooks: RxHooks) {

    fun middleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action === LoadCollections) {
            if (state.collectionListPage.collections.isEmpty()) {
                load(dispatch)
                return UpdateProgress(Progress.Loading)
            }
        } else if (action is ExportProgress && action.key == COLLECTION_LIST && action.progress is DataExporter.Progress.Success<*>) {
            val uri = action.progress.output as Uri?
            uri?.let {
                IntentUtils.openShareFileIntent(context, it)
            }
        }

        return next(state, action, dispatch)
    }

    private fun load(dispatch: Dispatch) {
        provider.getCollections(withMovies = true)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(CollectionsLoaded(it))
                }, {
                    it.printStackTrace()
                    dispatch(UpdateProgress(Progress.Error))
                })
    }

    companion object {
        fun newInstance(context: Context): CollectionListPageMiddleware {
            return CollectionListPageMiddleware(
                    context,
                    DbMovieCollectionProvider(MovieRatingsApplication.database.movieCollectionDao()),
                    AppRxHooks()
            )
        }
    }
}