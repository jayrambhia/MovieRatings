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
import com.fenchtose.movieratings.model.db.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.util.AppRxHooks
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

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

data class CreateCollection(val name: String): Action
data class DeleteCollection(val collection: MovieCollection): Action

data class UpdateCollectionOp(val op: CollectionOp): Action

sealed class MovieCollectionResponse(val collection: MovieCollection, val movie: Movie): Action {
    class MovieExists(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
    class MovieAdded(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
    class AddError(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
}

fun AppState.reduceCollections(action: Action): AppState {
    if (action is MovieCollectionResponse) {
        return updateCollectionListPage(action)
                .updateMoviePage(action)
    }

    return this
}

private fun AppState.updateMoviePage(action: MovieCollectionResponse): AppState {
    if (action is MovieCollectionResponse.MovieAdded) {
        if (moviePage.movie.imdbId == action.movie.imdbId
                && moviePage.movie.collections.hasCollection(action.collection.name) == -1) {
            return copy(moviePage = moviePage.copy(movie = moviePage.movie.addCollection(action.collection)))
        }
    }

    return this
}

private fun AppState.updateCollectionListPage(action: MovieCollectionResponse): AppState {
    if (action is MovieCollectionResponse.MovieAdded) {
        if (collectionListPage.active && collectionListPage.collections.hasCollection(action.collection.name) != -1) {
            return copy(collectionListPage = collectionListPage.copy(collections = collectionListPage.collections.update(action.collection)))
        }
    }

    return this
}

class CollectionMiddleware(private val collectionStore: MovieCollectionStore,
                           private val rxHooks: RxHooks) {
    fun collectionMiddleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        when(action) {
            is AddToCollection -> addToCollection(action, dispatch)
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
                        dispatch(MovieCollectionResponse.MovieExists(action.collection, action.movie))
                    }
                }.filter {
                    !it
                }.observeOn(Schedulers.io())
                .flatMap {
                    collectionStore.addMovieToCollection(action.collection, action.movie)
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    dispatch(MovieCollectionResponse.MovieAdded(action.collection.addMovie(action.movie), action.movie))
                }, {
                    dispatch(MovieCollectionResponse.AddError(action.collection, action.movie))
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