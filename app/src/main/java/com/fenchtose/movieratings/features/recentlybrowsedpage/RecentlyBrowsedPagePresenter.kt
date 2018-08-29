package com.fenchtose.movieratings.features.recentlybrowsedpage

import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.api.provider.RecentlyBrowsedMovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable

class RecentlyBrowsedPagePresenter(
        rxHooks: RxHooks,
        private val movieProvider: RecentlyBrowsedMovieProvider,
        likeStore: LikeStore,
        router: Router?): BaseMovieListPresenter<BaseMovieListPage>(rxHooks, likeStore, router) {

    init {
        movieProvider.addPreferenceApplier(likeStore)
    }

    override fun load(reload: Boolean): Observable<List<Movie>> {
        return movieProvider.getMovies()
                .map {
                    it.map {
                        it.movies!![0]
                    }
                }
    }
}