package com.fenchtose.movieratings.features.moviecollection.collectionlist

import com.fenchtose.movieratings.base.Presenter
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CollectionListPresenter(
        private val provider: MovieCollectionProvider,
        private val collectionStore: MovieCollectionStore) : Presenter<CollectionListPage>() {

    override fun attachView(view: CollectionListPage) {
        super.attachView(view)
        loadCollections()
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

    fun createCollection(name: String) {
        collectionStore.createCollection(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
}