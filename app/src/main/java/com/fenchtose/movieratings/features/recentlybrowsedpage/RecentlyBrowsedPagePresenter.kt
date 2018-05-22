package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.RecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable

class RecentlyBrowsedPagePresenter(
        rxHooks: RxHooks,
        private val movieProvider: RecentlyBrowsedMovieProvider,
        likeStore: LikeStore): BaseMovieListPresenter<BaseMovieListPage>(rxHooks, likeStore) {

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