package com.fenchtose.movieratings.model.db.movieCollection

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionOp
import com.fenchtose.movieratings.features.moviecollection.collectionlist.hasCollection
import com.fenchtose.movieratings.features.moviecollection.collectionlist.update
import com.fenchtose.movieratings.features.moviecollection.collectionpage.MovieCollectionOp
import com.fenchtose.movieratings.features.moviecollection.collectionpage.Removed
import com.fenchtose.movieratings.model.db.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.hasMovie
import com.fenchtose.movieratings.model.entity.remove
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks
import com.fenchtose.movieratings.util.add
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.math.acos

interface MovieCollectionStore : UserPreferenceApplier {
    fun createCollection(name: String): Observable<MovieCollection>
    fun deleteCollection(collection: MovieCollection): Observable<Boolean>
    fun addMovieToCollection(collection: MovieCollection, movie: Movie): Observable<MovieCollectionEntry>
    fun isMovieAddedToCollection(collection: MovieCollection, movie: Movie): Observable<Boolean>
    fun removeMovieFromCollection(collection: MovieCollection, movie: Movie): Observable<Boolean>
    fun deleteAllCollectionEntries(): Observable<Int>
    fun deleteAllCollections(): Observable<Int>
    fun export(): Single<List<com.fenchtose.movieratings.model.db.entity.MovieCollection>>
    fun export(collectionId: Long): Single<List<com.fenchtose.movieratings.model.db.entity.MovieCollection>>
    @WorkerThread
    fun import(collections: List<com.fenchtose.movieratings.model.db.entity.MovieCollection>): Int
}

data class AddToCollection(val collection: MovieCollection, val movie: Movie): Action
data class RemoveFromCollection(val collection: MovieCollection, val movie: Movie): Action

data class CreateCollection(val name: String): Action
data class DeleteCollection(val collection: MovieCollection): Action

data class UpdateCollectionOp(val op: CollectionOp): Action
data class UpdateMovieCollectionOp(val op: MovieCollectionOp): Action


fun AppState.reduceCollections(action: Action): AppState {
    if (action is UpdateMovieCollectionOp) {
        return updateCollectionListPage(action.op)
                .updateSearchCollection(action.op)
                .updateCollectionPages(action.op)
                .updateMoviePages(action.op)
    }

    return this
}

private fun AppState.updateMoviePages(op: MovieCollectionOp): AppState {
    if (moviePages.isEmpty()) {
        return this
    }

    val updated = moviePages.map {
        moviePage ->
        var mapped = moviePage
        if (op is MovieCollectionOp.Added) {
            if (moviePage.movie.imdbId == op.movie.imdbId
                    && moviePage.movie.collections.hasCollection(op.collection.name) == -1) {
                mapped = moviePage.copy(movie = moviePage.movie.addCollection(op.collection))
            }
        } else if (op is MovieCollectionOp.Removed) {
            if (moviePage.movie.imdbId == op.movie.imdbId
                    && moviePage.movie.collections.hasCollection(op.collection.name) != -1) {
                mapped = moviePage.copy(movie = moviePage.movie.removeCollection(op.collection))
            }
        }
        mapped
    }

    if (updated != moviePages) {
        return copy(moviePages = updated)
    }

    return this
}

private fun AppState.updateCollectionListPage(op: MovieCollectionOp): AppState {
    if (op is MovieCollectionOp.Added || op is MovieCollectionOp.Removed) {
        if (collectionListPage.active && collectionListPage.collections.hasCollection(op.collection.name) != -1) {
            // TODO should this be updated here? Are we sure the collection has the latest updates?
            return copy(collectionListPage = collectionListPage.copy(collections = collectionListPage.collections.update(op.collection)))
        }
    }
    return this
}

private fun AppState.updateCollectionPages(op: MovieCollectionOp): AppState {
    if (collectionPages.isEmpty()) {
        return this
    }

    val updated = collectionPages.mapIndexed {
        index, collectionPage ->
        var mapped = collectionPage
        if (op is MovieCollectionOp.Added) {
            if (collectionPage.active && collectionPage.collection.id == op.collection.id && collectionPage.movies.hasMovie(op.movie) == -1) {
                // If this was undo remove - add at the previous index
                val removed = collectionPage.removed
                val addIndex = if (removed == null || removed.index < 0 || removed.movie.imdbId != op.movie.imdbId) collectionPage.movies.size else removed.index
                mapped = collectionPage.copy(movies = collectionPage.movies.add(op.movie, addIndex, false), collectionOp = null)
            }
        } else if (op is MovieCollectionOp.Removed) {
            if (collectionPage.active && collectionPage.collection.id == op.collection.id && collectionPage.movies.hasMovie(op.movie) != -1) {
                mapped = if (index == collectionPages.size - 1) {
                    val movieIndex = collectionPage.movies.hasMovie(op.movie)
                    collectionPage.copy(movies = collectionPage.movies.remove(op.movie), collectionOp = op, removed = Removed(op.movie, movieIndex, true))
                } else {
                    collectionPage.copy(movies = collectionPage.movies.remove(op.movie), collectionOp = op)
                }

            }
        }

        mapped
    }

    if (updated != collectionPages) {
        return copy(collectionPages = updated)
    }

    return this
}

private fun AppState.updateSearchCollection(op: MovieCollectionOp): AppState {
    if (collectionSearchPage.collection.id < 0) {
        return this
    }

    return when(op) {
        is MovieCollectionOp.Added, is MovieCollectionOp.AddError, is MovieCollectionOp.Exists  -> copy(collectionSearchPage = collectionSearchPage.copy(collectionOp = op))
        else -> this
    }
}

class CollectionMiddleware(private val collectionStore: MovieCollectionStore,
                           private val rxHooks: RxHooks) {
    fun collectionMiddleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        when(action) {
            is AddToCollection -> addToCollection(action, dispatch)
            is RemoveFromCollection -> removeFromCollection(action.collection, action.movie, dispatch)
            is CreateCollection -> createCollection(action.name, dispatch)
            is DeleteCollection -> deleteCollection(action.collection, dispatch)
        }

        return next(state, action, dispatch)
    }

    private fun addToCollection(action: AddToCollection, dispatch: Dispatch) {
        collectionStore.isMovieAddedToCollection(action.collection, action.movie)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .doOnNext {
                    if (it) {
                        dispatch(UpdateMovieCollectionOp(MovieCollectionOp.Exists(action.collection, action.movie)))
                    }
                }.filter {
                    !it
                }.observeOn(Schedulers.io())
                .flatMap {
                    collectionStore.addMovieToCollection(action.collection, action.movie)
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dispatch(UpdateMovieCollectionOp(MovieCollectionOp.Added(action.collection.addMovie(action.movie), action.movie)))
                }, {
                    dispatch(UpdateMovieCollectionOp(MovieCollectionOp.AddError(action.collection, action.movie)))
                })
    }

    private fun removeFromCollection(collection: MovieCollection, movie: Movie, dispatch: Dispatch) {
        collectionStore.removeMovieFromCollection(collection, movie)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    if (it) {
                        dispatch(UpdateMovieCollectionOp(MovieCollectionOp.Removed(collection.removeMovie(movie), movie)))
                    } else {
                        dispatch(UpdateMovieCollectionOp(MovieCollectionOp.RemoveError(collection, movie)))
                    }
                }, {
                    it.printStackTrace()
                    dispatch(UpdateMovieCollectionOp(MovieCollectionOp.RemoveError(collection, movie)))
                })
    }

    private fun createCollection(name: String, dispatch: Dispatch) {
        collectionStore.createCollection(name)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    dispatch(UpdateCollectionOp(CollectionOp.Created(it.name, it)))
                }, {
                    it.printStackTrace()
                    dispatch(UpdateCollectionOp(CollectionOp.CreateError(name)))
                })
    }

    private fun deleteCollection(collection: MovieCollection, dispatch: Dispatch) {
        collectionStore.deleteCollection(collection)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    if (it) {
                        dispatch(UpdateCollectionOp(CollectionOp.Deleted(collection.name)))
                    } else {
                        dispatch(UpdateCollectionOp(CollectionOp.DeleteError(collection.name)))
                    }
                }, {
                    it.printStackTrace()
                    dispatch(UpdateCollectionOp(CollectionOp.DeleteError(collection.name)))
                })
    }

    companion object {
        fun newInstance(): CollectionMiddleware {
            return CollectionMiddleware(
                    DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()),
                    AppRxHooks())
        }
    }
}