package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.preferences.UserPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CollectionPagePresenter(likeStore: LikeStore,
                              private val provider: MovieCollectionProvider,
                              private val collectionStore: MovieCollectionStore,
                              private val userPreferences: UserPreferences,
                              private val collection: MovieCollection?) : BaseMovieListPresenter<CollectionPage>(likeStore) {

    private var currentSort: Sort = userPreferences.getLatestCollectionSort(collection?.id)
        set(value) {
            field = value
            userPreferences.setLatestCollectionSort(collection?.id, value)
        }

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun load(): Observable<List<Movie>> {
        collection?.let {
            return provider.getMoviesForCollection(it)
                    .map {
                        getSorted(currentSort, it)
                    }
        }

        return Observable.just(ArrayList())
    }

    fun removeMovie(movie: Movie) {
        collection?.let {
            val d = collectionStore.removeMovieFromCollection(it, movie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it) {
                            val index = data.indexOf(movie)
                            if (index >= 0) {
                                val removed = data.removeAt(index)
                                getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVED, removed, index))
                                getView()?.onRemoved(removed, index)
                                return@subscribe
                            }
                        }
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVE_ERROR, movie))
                    }, {
                        it.printStackTrace()
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_REMOVE_ERROR, movie))
                    })

            subscribe(d)
        }
    }

    fun undoRemove(movie: Movie, index: Int) {
        collection?.let {
            collectionStore.addMovieToCollection(it, movie)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val addedIndex = when {
                            (index >= 0 && index < data.size) -> {
                                data.add(index, movie)
                                index
                            }
                            else -> {
                                data.add(movie)
                                data.size - 1
                            }
                        }
                        getView()?.showAdded(movie, addedIndex)
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_ADDED, movie, addedIndex))
                    }, {
                        it.printStackTrace()
                        getView()?.updateState(CollectionPage.OpState(CollectionPage.Op.MOVIE_ADD_ERROR, movie))
                    })
        }
    }

    fun sort(type: Sort) {
        if (type == currentSort) {
            return
        }

        updateData(ArrayList(getSorted(type, data)))
        currentSort = type
    }

    private fun getSorted(type: Sort, data: List<Movie>): List<Movie> = when(type) {
        Sort.YEAR -> data.sortedWith(compareByDescending { it.year })
        Sort.ALPHABETICAL -> data.sortedBy { it.title }
        else -> data
    }
}