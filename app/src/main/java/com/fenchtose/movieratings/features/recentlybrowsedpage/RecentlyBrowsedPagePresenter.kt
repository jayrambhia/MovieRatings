package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.RecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.Observable

class RecentlyBrowsedPagePresenter(
        private val movieProvider: RecentlyBrowsedMovieProvider,
        likeStore: LikeStore): BaseMovieListPresenter<BaseMovieListPage>(likeStore) {

    init {
        movieProvider.addPreferenceApplier(likeStore)
    }

    override fun load(): Observable<List<Movie>> {
        return movieProvider.getMovies()
                .map {
                    it.map {
                        it.movies!![0]
                    }
                }
    }
}