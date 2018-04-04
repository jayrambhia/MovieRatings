package com.fenchtose.movieratings.features.baselistpage

import android.support.annotation.CallSuper
import android.view.View
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

abstract class BaseMovieListPresenter<V :BaseMovieListPage>(
        private val likeStore: LikeStore, private val router: Router?): Presenter<V>() {

    protected var data: ArrayList<Movie>? = null

    @CallSuper
    override fun attachView(view: V) {
        super.attachView(view)
        loadData()
    }

    open protected fun loadData() {
        val d = load()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            updateData(ArrayList(it))
                        },
                        onError = {
                            data = null
                            it.printStackTrace()
                            getView()?.updateState(BaseMovieListPage.State(BaseMovieListPage.Ui.ERROR, data))
                        }
                )

        subscribe(d)
    }

    open protected fun updateData(movies: ArrayList<Movie>) {
        this.data = movies

        val state = if (movies.isEmpty()) {
            BaseMovieListPage.Ui.EMPTY
        } else {
            BaseMovieListPage.Ui.DATA_LOADED
        }

        getView()?.updateState(BaseMovieListPage.State(state, movies))
    }

    open fun toggleLike(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    abstract fun load(): Observable<List<Movie>>
}