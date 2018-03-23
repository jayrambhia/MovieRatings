package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.Observable

class CollectionPagePresenter(likeStore: LikeStore, val provider: MovieCollectionProvider, val collection: MovieCollection?) : BaseMovieListPresenter<CollectionPage>(likeStore) {

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun load(): Observable<List<Movie>> {
        collection?.let {
            return provider.getMoviesForCollection(it)
        }

        return Observable.just(ArrayList())
    }
}