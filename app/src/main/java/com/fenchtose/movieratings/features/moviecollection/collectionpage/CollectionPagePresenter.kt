package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CollectionPagePresenter(likeStore: LikeStore,
                              private val provider: MovieCollectionProvider,
                              private val collectionStore: MovieCollectionStore,
                              private val collection: MovieCollection?) : BaseMovieListPresenter<CollectionPage>(likeStore) {

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun load(): Observable<List<Movie>> {
        collection?.let {
            return provider.getMoviesForCollection(it)
        }

        return Observable.just(ArrayList())
    }

    fun removeMovie(movie: Movie) {
        collection?.let {
            val d = collectionStore.removeMovieFromCollection(it, movie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it) {
                            val index = data.indexOf(movie)
                            if (index >= 0) {
                                val removed = data.removeAt(index)
                                getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVED, removed))
                                getView()?.onRemoved(removed, index)
                                return@subscribe
                            }
                        }
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVE_ERROR, movie))
                    }, {
                        it.printStackTrace()
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVE_ERROR, movie))
                    })

            subscribe(d)
        }
    }
}