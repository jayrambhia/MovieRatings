package com.fenchtose.movieratings.features.trending

import com.fenchtose.movieratings.base.PresenterState
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

    private var current: String = "day"

    override fun load(reload: Boolean): Observable<List<Movie>> {

        if (!reload) {
            data?.let {
                return Observable.just(it)
            }
        }

        callWithDelay(::showProgress, 200)

        return provider.getTrending(current)
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

    private fun showProgress() {
        if (data == null) {
            getView()?.updateState(BaseMovieListPage.State.Loading())
        }
    }

    fun updatePeriod(period: String) {
        if (current != period) {
            current = period
            loadData(true)
        }
    }

    fun currentPeriod(): String {
        return current
    }

    override fun saveState(): PresenterState? {
        data?.let {
            return TrendingState(current, it)
        }

        return null
    }

    override fun restoreState(state: PresenterState?) {
        if (state != null && state is TrendingState) {
            current = state.period
            data = ArrayList(state.movies)
        }
    }
}

data class TrendingState(val period: String, val movies: List<Movie>): PresenterState
