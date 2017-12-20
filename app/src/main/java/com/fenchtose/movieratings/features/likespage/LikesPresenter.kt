package com.fenchtose.movieratings.features.likespage

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.FavoriteMovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LikesPresenter(private val provider: FavoriteMovieProvider, private val likeStore: LikeStore) : Presenter<LikesPage>() {

    var data: ArrayList<Movie>? = null

    override fun attachView(view: LikesPage) {
        super.attachView(view)
        loadData()
    }

    private fun loadData() {
        val d = provider.getMovies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            data = it
                            getView()?.setData(it)
                        },
                        onError = {
                            data = null
                            it.printStackTrace()
                        }
                )

        subscribe(d)
    }

    fun unlike(movie: Movie) {
        data?.let {
            val index = it.indexOf(movie)
            if (index >= 0) {
                it.removeAt(index)
                likeStore.setLiked(movie.imdbId, false)
                getView()?.showRemoved(movie, index)
            }
        }
    }

    fun undoUnlike(movie: Movie, index: Int) {
        likeStore.setLiked(movie.imdbId, true)
        data?.let {
            val addedIndex = when {
                (index >= 0 && index < it.size) -> {
                    it.add(index, movie)
                    index
                }
                else -> {
                    it.add(movie)
                    it.size - 1
                }
            }
            getView()?.showAdded(movie, addedIndex)
        }
    }
}