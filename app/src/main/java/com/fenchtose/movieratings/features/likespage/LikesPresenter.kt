package com.fenchtose.movieratings.features.likespage

import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.FavoriteMovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class LikesPresenter(private val provider: FavoriteMovieProvider, private val likeStore: LikeStore,
                     private val userPreferences: UserPreferences) : Presenter<LikesPage>() {

    private var data: ArrayList<Movie>? = null
    private var currentSort: Sort = userPreferences.getLatestLikeSort()

        set(value) {
        field = value
        userPreferences.setLatestLikeSort(value)
    }

    override fun attachView(view: LikesPage) {
        super.attachView(view)
        loadData()
    }

    private fun loadData() {
        val d = provider.getMovies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    getSorted(currentSort, it)
                }
                .subscribeBy(
                        onNext = {
                            updateData(ArrayList(it))
                        },
                        onError = {
                            data = null
                            it.printStackTrace()
                        }
                )

        subscribe(d)
    }

    private fun updateData(data: ArrayList<Movie>) {
        this.data = data
        getView()?.setData(data)
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

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        MovieRatingsApplication.router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    fun sort(type: Sort) {

        if (type == currentSort) {
            return
        }

        data?.let {
            updateData(ArrayList(getSorted(type, it)))
            currentSort = type
        }
    }

    private fun getSorted(type: Sort, data: ArrayList<Movie>): List<Movie> = when(type) {
        Sort.YEAR -> data.sortedWith(compareByDescending { it.year })
        Sort.ALPHABETICAL -> data.sortedBy { it.title }
        else -> data
    }
}