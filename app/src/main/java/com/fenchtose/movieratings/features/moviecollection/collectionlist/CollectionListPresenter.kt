package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.content.Context
import android.net.Uri
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.base.PresenterState
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.util.FileUtils
import com.fenchtose.movieratings.util.RxHooks

class CollectionListPresenter(
        private val context: Context,
        private val rxHooks: RxHooks,
        private val fileUtils: FileUtils,
        private val provider: MovieCollectionProvider,
        private val collectionStore: MovieCollectionStore,
        private val exporter: DataExporter<Uri>) : Presenter<CollectionListPage>() {

    private val collections: ArrayList<MovieCollection> = ArrayList()

    override fun attachView(view: CollectionListPage) {
        super.attachView(view)
        loadCollections()
        subscribe(exporter.observe()
                .observeOn(rxHooks.mainThread())
                .map {
                    when(it) {
                        is DataExporter.Progress.Started -> CollectionListPage.ShareState.Started
                        is DataExporter.Progress.Error -> CollectionListPage.ShareState.Error
                        is DataExporter.Progress.Success -> CollectionListPage.ShareState.Success(it.output)
                    }
                }
                .subscribe({
                    updateState(it)
                }, {
                    updateState(CollectionListPage.ShareState.Error)
                }))
    }

    override fun detachView(view: CollectionListPage) {
        super.detachView(view)
        exporter.release()
    }

    private fun loadCollections() {
        if (collections.isNotEmpty()) {
            updateState(CollectionListPage.State.Success(collections))
        }

        provider.getCollections(withMovies = true)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    collections.clear()
                    collections.addAll(it)
                    if (it.isEmpty()) {
                        updateState(CollectionListPage.State.Empty)
                    } else {
                        updateState(CollectionListPage.State.Success(collections))
                    }
                }, {
                    it.printStackTrace()
                    collections.clear()
                    updateState(CollectionListPage.State.Error)
                })
    }

    private fun updateState(state: CollectionListPage.State) {
        getView()?.updateState(state)
    }

    private fun updateState(state: CollectionListPage.OpState) {
        getView()?.updateState(state)
    }

    private fun updateState(state: CollectionListPage.ShareState) {
        getView()?.updateState(state)
    }

    fun createCollection(name: String) {
        collectionStore.createCollection(name)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    updateState(CollectionListPage.OpState.Created(it.name))
                    loadCollections()
                }, {
                    it.printStackTrace()
                    updateState(CollectionListPage.OpState.CreateError(name))
                })
    }

    fun deleteCollection(collection: MovieCollection) {
        collectionStore.deleteCollection(collection)
                .subscribeOn(rxHooks.ioThread())
                .observeOn(rxHooks.mainThread())
                .subscribe({
                    if (it) {
                        updateState(CollectionListPage.OpState.Deleted(collection.name))
                        loadCollections()
                    } else {
                        updateState(CollectionListPage.OpState.DeleteError(collection.name))
                    }
                }, {
                    it.printStackTrace()
                    updateState(CollectionListPage.OpState.DeleteError(collection.name))
                })
    }

    fun share() {
        val uri = fileUtils.createCacheFile(context, "flutter_collections.txt")
        exporter.export(uri, DataExporter.Config(false, true, false))
    }

    override fun saveState(): PresenterState? {
        return if (collections.isNotEmpty()) CollectionListState(collections) else null
    }

    override fun restoreState(state: PresenterState?) {
        if (state != null && state is CollectionListState) {
            collections.clear()
            collections.addAll(state.collections)
        }
    }

    fun getCollectionCount(): Int {
        return collections.size
    }
}

data class CollectionListState(val collections: List<MovieCollection>): PresenterState