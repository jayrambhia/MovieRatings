package com.fenchtose.movieratings.features.searchpage

import android.view.View
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.features.moviepage.MoviePageFragment
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.SearchResult
import com.fenchtose.movieratings.model.api.provider.MovieProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

sealed class SearchPresenter(private val provider: MovieProvider,
                      private val likeStore: LikeStore) : Presenter<SearchPage>() {

    private var currentQuery = ""
    private val data: ArrayList<Movie> = ArrayList()
    private var pageNum: Int = 1
    private var queryDisposables: CompositeDisposable = CompositeDisposable()

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: SearchPage) {
        super.attachView(view)
        if (data.isEmpty()) {
            updateState(SearchPage.State.Default)
        } else {
            showAlreadyLoadedData(data)
        }
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
        updateState(SearchPage.State.Loading)
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
            updateState(SearchPage.State.LoadingMore)
            makeApiCall(it, pageNum, queryDisposables)
        }
    }

    fun retrySearch() {
        if (currentQuery.isNotEmpty()) {
            if (pageNum == 1) {
                onSearchRequested(currentQuery)
            } else {
                loadMore()
            }
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
            result != null && result.success -> {
                if (pageNum == 1) {
                    data.clear()
                    data.addAll(result.results)
                    SearchPage.State.Loaded.Success(data)
                } else {
                    data.addAll(result.results)
                    SearchPage.State.Loaded.PaginationSuccess(data)
                }
            }
            result != null && !result.success -> {
                SearchPage.State.NoResult
            }
            else -> {
                if (pageNum == 1) {
                    data.clear()
                    SearchPage.State.Error
                } else {
                    pageNum--
                    SearchPage.State.PaginationError
                }
            }
        }

        updateState(state)
    }

    private fun showAlreadyLoadedData(data: ArrayList<Movie>) {
        updateState(SearchPage.State.Loaded.Restored(data))
    }

    protected fun updateState(state: SearchPage.State) {
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
        MovieRatingsApplication.router?.go(MoviePageFragment.MoviePath(movie, sharedElement))
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

    class DefaultPresenter(provider: MovieProvider, likeStore: LikeStore): SearchPresenter(provider, likeStore)

    class AddToCollectionPresenter(provider: MovieProvider,
                                   likeStore: LikeStore,
                                   private val collectionStore: MovieCollectionStore,
                                   private val collection: MovieCollection): SearchPresenter(provider, likeStore) {

        fun addToCollection(movie: Movie) {
            val d = collectionStore.isMovieAddedToCollection(collection, movie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        if (it) {
                            updateState(SearchPage.CollectionState.Exists(collection))
                        }
                    }.filter {
                        !it
                    }
                    .observeOn(Schedulers.io())
                    .flatMap {
                        collectionStore.addMovieToCollection(collection, movie)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        updateState(SearchPage.CollectionState.Added(collection))
                    }, {
                        it.printStackTrace()
                        updateState(SearchPage.CollectionState.Error(collection))
                    })
            subscribe(d)
        }

        fun updateState(state: SearchPage.CollectionState) {
            getView()?.updateState(state)
        }

    }
}