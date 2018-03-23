package com.fenchtose.movieratings.features.baselistpage

import android.support.annotation.CallSuper
import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

abstract class BaseMovieListPresenter<V :BaseMovieListPage>(
        private var likeStore: LikeStore): Presenter<V>() {

    protected val data: ArrayList<Movie> = ArrayList()

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
                            data.clear()
                            it.printStackTrace()
                        }
                )

        subscribe(d)
    }

    open protected fun updateData(movies: ArrayList<Movie>) {
        this.data.clear()
        this.data.addAll(movies)
        getView()?.setData(this.data)
    }

    open fun toggleLike(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        MovieRatingsApplication.router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    abstract fun load(): Observable<List<Movie>>
}