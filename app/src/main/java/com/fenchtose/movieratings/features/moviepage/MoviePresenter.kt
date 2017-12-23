package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MoviePresenter(private val provider: MovieProvider, private val likeStore: LikeStore,
                     private val imdbId: String?, private val movie: Movie?): Presenter<MoviePage>() {

    private var loadedMovie: Movie? = null

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: MoviePage) {
        super.attachView(view)
        imdbId?.let {
            loadMovie(it)
        }
    }

    private fun loadMovie(imdbId: String) {
        movie?.let {
            @Suppress("UselessCallOnNotNull")
            if (it.isComplete()) {
                showMovie(movie)
                return
            } else if (!it.poster.isNullOrEmpty()) {
                getView()?.loadImage(it.poster)
            }
        }

        val d = provider
                .getMovieWithImdb(imdbId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showMovie(it)
                }, {
                    loadedMovie = null
                    it.printStackTrace()
                    getView()?.showError()
                })

        subscribe(d)
    }

    private fun showMovie(movie: Movie) {
        loadedMovie = movie
        getView()?.showMovie(movie)
    }

    fun likeToggle(): Boolean {
        loadedMovie?.let {
            it.liked = !it.liked
            likeStore.setLiked(it.imdbId, it.liked)
            return it.liked
        }

        return false
    }
}