package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.net.Uri
import android.support.annotation.VisibleForTesting
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.router.Navigation
import com.fenchtose.movieratings.analytics.ga.GaEvents
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPresenter
import com.fenchtose.movieratings.features.searchpage.SearchPageFragment
import com.fenchtose.movieratings.model.entity.Sort
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.FileUtils
import com.fenchtose.movieratings.util.RxHooks
import io.reactivex.Observable

class CollectionPagePresenter(likeStore: LikeStore,
                              rxHooks: RxHooks,
                              private val fileUtils: FileUtils,
                              private val provider: MovieCollectionProvider,
                              private val collectionStore: MovieCollectionStore,
                              private val userPreferences: UserPreferences,
                              private val exporter: DataExporter<Uri>,
                              private val collection: MovieCollection?,
                              router: Router?
                              ) : BaseMovieListPresenter<CollectionPage>(rxHooks, likeStore, router) {

    private var currentSort: Sort = userPreferences.getLatestCollectionSort(collection?.id)
        set(value) {
            field = value
            userPreferences.setLatestCollectionSort(collection?.id, value)
        }

    init {
        provider.addPreferenceApplier(likeStore)
    }

    override fun attachView(view: CollectionPage) {
        super.attachView(view)
        subscribe(exporter.observe()
                .map {
                    when(it) {
                        is DataExporter.Progress.Started -> CollectionPage.ShareState.Started()
                        is DataExporter.Progress.Error -> CollectionPage.ShareState.Error()
                        is DataExporter.Progress.Success -> CollectionPage.ShareState.Success(it.output)
                    }
                }
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    getView()?.updateState(it)
                },{
                    getView()?.updateState(CollectionPage.ShareState.Error())
                }))
    }

    override fun load(reload: Boolean): Observable<List<Movie>> {
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
                    .subscribeOn(rxHooks.ioThread())
                    .observeOn(rxHooks.mainThread())
                    .subscribe({
                        if (it) {
                            data?.let {
                                val index = it.indexOf(movie)
                                if (index >= 0) {
                                    val removed = it.removeAt(index)
                                    getView()?.updateState(CollectionPage.OpState.Removed(removed, index))
                                    if (it.isEmpty()) {
                                        getView()?.updateState(BaseMovieListPage.State.Empty())
                                    }
                                    return@subscribe
                                }
                            }
                        }
                        getView()?.updateState(CollectionPage.OpState.RemoveError(movie))
                    }, {
                        it.printStackTrace()
                        getView()?.updateState(CollectionPage.OpState.RemoveError(movie))
                    })

            subscribe(d)
        }
    }

    fun undoRemove(movie: Movie, index: Int) {
        collection?.let {
            collectionStore.addMovieToCollection(it, movie)
                    .subscribeOn(rxHooks.ioThread())
                    .observeOn(rxHooks.mainThread())
                    .subscribe({
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
                            getView()?.updateState(CollectionPage.OpState.Added(movie, addedIndex))
                        }

                    }, {
                        it.printStackTrace()
                        getView()?.updateState(CollectionPage.OpState.AddError(movie))
                    })
        }
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

    fun searchToAddToCollection() {
        GaEvents.TAP_SEARCH_FOR_COLLECTION.track()
        collection?.let {
            router?.let {
                getView()?.getDispatcher()?.invoke(Navigation(it, SearchPageFragment.SearchPath.AddToCollection(collection)))
            }
        }
    }

    fun share() {
        collection?.let {
            val uri = fileUtils.createCacheFile(MovieRatingsApplication.instance, "collection_${it.name}.txt")
            exporter.exportCollection(uri, it.id)
        }
    }

    fun canShare(): Boolean {
        return collection != null && data != null && data!!.size > 0
    }

    private fun getSorted(type: Sort, data: List<Movie>): List<Movie> = when(type) {
        Sort.YEAR -> data.sortedWith(compareByDescending { it.year })
        Sort.ALPHABETICAL -> data.sortedBy { it.title }
        else -> data
    }

    @VisibleForTesting
    fun getSort() = currentSort
}