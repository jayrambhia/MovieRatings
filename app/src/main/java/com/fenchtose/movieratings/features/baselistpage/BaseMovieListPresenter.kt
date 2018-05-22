package com.fenchtose.movieratings.features.baselistpage

import android.support.annotation.CallSuper
import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy

abstract class BaseMovieListPresenter<V :BaseMovieListPage>(
        private val rxHooks: RxHooks,
        private val likeStore: LikeStore): Presenter<V>() {

    protected var data: ArrayList<Movie>? = null

    @CallSuper
    override fun attachView(view: V) {
        super.attachView(view)
        loadData()
    }

    open protected fun loadData() {
        val d = load()
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

    open fun toggleLike(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
        movie.liked = !movie.liked
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        MovieRatingsApplication.router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    abstract fun load(): Observable<List<Movie>>
}