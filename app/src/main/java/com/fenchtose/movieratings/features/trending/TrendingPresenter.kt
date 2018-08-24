package com.fenchtose.movieratings.features.trending

import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.api.provider.MovieRatingsProvider
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable

class TrendingPresenter(rxHooks: RxHooks,
                        private val provider: MovieRatingsProvider,
                        private val movieDao: MovieDao,
                        likeStore: LikeStore,
                        private val preferenceAppliers: Set<UserPreferenceApplier>,
                        router: Router?): BaseMovieListPresenter<TrendingFragment>(rxHooks, likeStore, router, false) {

    override fun load(): Observable<List<Movie>> {
        data?.let {
            return Observable.just(it)
        }

        getView()?.updateState(BaseMovieListPage.State.Loading())

        return provider.getTrending()
                .map { it.movies }
                .flatMapIterable { movies -> movies }
                .map {
                    val fromDb = movieDao.getMovieWithImdbId(it.imdbId)
                    if (fromDb != null) {
                        fromDb
                    } else {
                        val toDb = it.toMovie()
                        movieDao.insert(toDb)
                        toDb
                    }
                }
                .doOnNext {

                    movie -> preferenceAppliers.forEach { it.apply(movie) }

                }.toList().toObservable()
    }

}

