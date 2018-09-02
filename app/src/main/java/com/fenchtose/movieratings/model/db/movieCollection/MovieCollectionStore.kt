package com.fenchtose.movieratings.model.db.movieCollection

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next
import com.fenchtose.movieratings.model.db.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
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

sealed class MovieCollectionResponse(val collection: MovieCollection, val movie: Movie): Action {
    class MovieExists(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
    class MovieAdded(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
    class AddError(collection: MovieCollection, movie: Movie): MovieCollectionResponse(collection, movie)
}

class CollectionMiddleware(private val collectionStore: MovieCollectionStore) {
    fun collectionMiddleware(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
        if (action is AddToCollection) {
            addToCollection(action, dispatch)
        }

        return next(state, action, dispatch)
    }

    private fun addToCollection(action: AddToCollection, dispatch: Dispatch) {
        collectionStore.isMovieAddedToCollection(action.collection, action.movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                    dispatch(MovieCollectionResponse.MovieAdded(action.collection, action.movie))
                }, {
                    dispatch(MovieCollectionResponse.AddError(action.collection, action.movie))
                })
    }

    companion object {
        fun newInstance(): CollectionMiddleware {
            return CollectionMiddleware(DbMovieCollectionStore.getInstance(MovieRatingsApplication.database.movieCollectionDao()))
        }
    }
}