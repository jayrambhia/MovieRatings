package com.fenchtose.movieratings.features.baselistpage

import android.support.annotation.CallSuper
import android.support.annotation.VisibleForTesting
import android.view.View
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

abstract class BaseMovieListPresenter<V :BaseMovieListPage>(
        protected val rxHooks: RxHooks,
        private val likeStore: LikeStore,
        protected val router: Router?,
        private val reload: Boolean = true): Presenter<V>() {

    protected var data: ArrayList<Movie>? = null

    @CallSuper
    override fun attachView(view: V) {
        super.attachView(view)
        val temp = data
        var load = true

        if (temp != null) {
            updateData(temp)
            load = reload
        }

        if (load) {
            loadData()
        }
    }

    open protected fun loadData(reload: Boolean = false) {
        val d = load(reload)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribeBy(
                        onNext = {
                            updateData(ArrayList(it))
                        },
                        onError = {
                            data = null
                            it.printStackTrace()
                            getView()?.updateState(BaseMovieListPage.State.Error())
                        }
                )

        subscribe(d)
    }

    open protected fun updateData(movies: ArrayList<Movie>) {
        this.data = movies

        val state = if (movies.isEmpty()) {
            BaseMovieListPage.State.Empty()
        } else {
            BaseMovieListPage.State.Success(movies)
        }

        getView()?.updateState(state)
    }

    open protected fun clearData() {
        data = null
        getView()?.updateState(BaseMovieListPage.State.Cleared())
    }

    open fun toggleLike(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
        // TODO like movie
//        movie.liked = !movie.liked
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    abstract fun load(reload: Boolean = false): Observable<List<Movie>>

    @VisibleForTesting
    fun getDataForTest(): List<Movie>? = data

    override fun saveState(): PresenterState? {
        data?.let {
            return BaseMovieListState(it)
        }

        return null
    }

    override fun restoreState(state: PresenterState?) {
        if (state != null && state is BaseMovieListState) {
            data = ArrayList(state.movies)
        }
    }


    fun callWithDelay(fn: () -> Unit, delay: Long) {
        Observable.just(fn)
                .delay(delay, TimeUnit.MILLISECONDS)
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    it()
                })
    }
}

data class BaseMovieListState(val movies: List<Movie>): PresenterState