package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.net.Uri
import android.util.Log
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.util.FileUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CollectionListPresenter(
        private val provider: MovieCollectionProvider,
        private val collectionStore: MovieCollectionStore,
        private val exporter: DataExporter<Uri>) : Presenter<CollectionListPage>() {

    override fun attachView(view: CollectionListPage) {
        super.attachView(view)
        loadCollections()
        subscribe(exporter.observe()
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    when(it) {
                        is DataExporter.Progress.Started -> CollectionListPage.ShareState.Start()
                        is DataExporter.Progress.Error -> CollectionListPage.ShareState.Error()
                        is DataExporter.Progress.Success -> CollectionListPage.ShareState.Success(it.data)
                    }
                }
                .subscribe({
                    updateState(it)
                }, {
                    updateState(CollectionListPage.ShareState.Error())
                }))
    }

    override fun detachView(view: CollectionListPage) {
        super.detachView(view)
        exporter.release()
    }

    private fun loadCollections() {
        provider.getCollections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isEmpty()) {
                        updateState(CollectionListPage.State.Empty())
                    } else {
                        updateState(CollectionListPage.State.Success(ArrayList(it)))
                    }
                }, {
                    it.printStackTrace()
                    updateState(CollectionListPage.State.Error())
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("create collection: ", "${it.name}, ${it.id}")
                    updateState(CollectionListPage.OpState.Created(it.name))
                    loadCollections()
                }, {
                    it.printStackTrace()
                    updateState(CollectionListPage.OpState.CreateError(name))
                })
    }

    fun deleteCollection(collection: MovieCollection) {
        collectionStore.deleteCollection(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateState(CollectionListPage.OpState.Deleted(collection.name))
                    loadCollections()
                }, {
                    it.printStackTrace()
                    updateState(CollectionListPage.OpState.DeleteError(collection.name))
                })
    }

    fun share() {
        val uri = FileUtils.createCacheFile(MovieRatingsApplication.instance!!, FileUtils.createCacheFilename())
        exporter.export(DataExporter.Config(uri, false, true, false))
    }
}