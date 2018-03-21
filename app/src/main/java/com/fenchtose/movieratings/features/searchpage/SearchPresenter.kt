package com.fenchtose.movieratings.features.searchpage

import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SearchPresenter(private val provider: MovieProvider, private val likeStore: LikeStore) : Presenter<SearchPage>() {

    private var currentQuery = ""
    private var data: ArrayList<Movie>? = null

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: SearchPage) {
        super.attachView(view)
        data?.let {
            updateData(it)
        }
    }

    fun onSearchRequested(query: String) {

        if (currentQuery == query && data != null) {
            updateData(data!!)
            return
        }

        updateState(SearchPage.Ui.LOADING)

        currentQuery = query

        val d = provider.search(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            data = if (it.success) {
                                updateData(it.results)
                                it.results
                            } else {
                                null
                            }
                        },
                        onError = {
                            it.printStackTrace()
                            data = null
                            updateState(SearchPage.Ui.ERROR)
                        }
                )

        subscribe(d)
    }

    private fun updateData(movies: ArrayList<Movie>) {
        updateState(SearchPage.State(SearchPage.Ui.DATA_LOADED, movies))
    }

    private fun updateState(state: SearchPage.Ui) {
        updateState(SearchPage.State(state))
    }

    private fun updateState(state: SearchPage.State) {
        getView()?.updateState(state)
    }

    fun onQueryCleared() {
        currentQuery = ""
        data = null
    }

    fun setLiked(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        MovieRatingsApplication.router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }
}