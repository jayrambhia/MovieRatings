package com.fenchtose.movieratings.features.searchpage

import android.view.View
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SearchPresenter(private val provider: MovieProvider, private val likeStore: LikeStore,
                      private val router: Router?) : Presenter<SearchPage>() {

    private var currentQuery = ""
    private val data: ArrayList<Movie> = ArrayList()
    private var pageNum: Int = 1
    private var queryDisposables: CompositeDisposable = CompositeDisposable()

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: SearchPage) {
        super.attachView(view)
        data.takeIf { data.isNotEmpty() }?.let { showAlreadyLoadedData(it) }
    }

    override fun detachView(view: SearchPage) {
        super.detachView(view)
        queryDisposables.dispose()
    }

    fun onSearchRequested(query: String) {

        if (currentQuery == query && data.isNotEmpty()) {
            showAlreadyLoadedData(data)
            return
        }

        queryDisposables.dispose()

        pageNum = 1
        updateState(SearchPage.Ui.LOADING)
        currentQuery = query
        queryDisposables = CompositeDisposable()
        makeApiCall(query, pageNum, queryDisposables)
    }

    fun loadMore() {
        currentQuery.takeIf { it.isNotEmpty() }?.let {
            pageNum++
            if (queryDisposables.isDisposed) {
                queryDisposables = CompositeDisposable()
            }
            updateState(SearchPage.Ui.LOADING_MORE)
            makeApiCall(it, pageNum, queryDisposables)
        }
    }

    private fun makeApiCall(query: String, page: Int, disposable: CompositeDisposable) {
        val d = provider.search(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            updateData(it)
                        },
                        onError = {
                            it.printStackTrace()
                            updateData(null)
                        }
                )

        disposable.add(d)
    }

    private fun updateData(result: SearchResult?) {

        val state = when {
            result == null -> {
                if (pageNum == 1) {
                    data.clear()
                    SearchPage.Ui.ERROR
                } else {
                    SearchPage.Ui.LOAD_MORE_ERROR
                }
            }
            result.success -> {
                if (pageNum == 1) {
                    data.clear()
                    data.addAll(result.results)
                    SearchPage.Ui.DATA_LOADED
                } else {
                    data.addAll(result.results)
                    SearchPage.Ui.MORE_DATA_LOADED
                }
            }
            else -> {
                if (pageNum == 1) {
                    data.clear()
                    SearchPage.Ui.ERROR
                } else {
                    SearchPage.Ui.LOAD_MORE_ERROR
                }
            }
        }

        updateState(SearchPage.State(state, data))
    }

    private fun showAlreadyLoadedData(data: ArrayList<Movie>) {
        updateState(SearchPage.State(SearchPage.Ui.DATA_RESTORED, data))
    }

    private fun updateState(state: SearchPage.Ui) {
        updateState(SearchPage.State(state))
    }

    private fun updateState(state: SearchPage.State) {
        getView()?.updateState(state)
    }

    fun onQueryCleared() {
        currentQuery = ""
        data.clear()
    }

    fun setLiked(movie: Movie) {
        likeStore.setLiked(movie.imdbId, !movie.liked)
        movie.liked = !movie.liked
    }

    fun openMovie(movie: Movie, sharedElement: Pair<View, String>?) {
        router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
    }

    override fun saveState() = SearchState(currentQuery, data.map { it -> it } as ArrayList<Movie>, pageNum)

    override fun restoreState(state: PresenterState?) {
        state?.takeIf { it is SearchState }?.let {
            it as SearchState
        }?.apply {
            currentQuery = query
            data.clear()
            data.addAll(movies)
            pageNum = page
        }
    }
}