package com.fenchtose.movieratings.features.moviecollection

import android.content.Context
import android.net.Uri
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPage
import com.fenchtose.movieratings.features.moviecollection.collectionlist.CollectionListPresenter
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.util.TestFileUtils
import com.fenchtose.movieratings.util.TestRxHooks
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@Suppress("IllegalIdentifier")
class CollectionListTests {

    private val context: Context = mock()
    private val view: CollectionListPage = mock()
    private val provider: MovieCollectionProvider = mock()
    private val store: MovieCollectionStore = mock()
    private val exporter: DataExporter<Uri> = mock()

    private val exporterPublisher: PublishSubject<DataExporter.Progress<Uri>> = PublishSubject.create()
    private val rxHooks = TestRxHooks()
    private val fileUtils = TestFileUtils()

    private var presenter = CollectionListPresenter(context, rxHooks, fileUtils, provider, store, exporter)

    private val collectionToBeDeleted = MovieCollection.create("collection to be deleted")
    private val collectionToBeDeletedError = MovieCollection.create("collection to be deleted error")
    private val collectionToBeDeletedButUnableToDelete = MovieCollection.create("collection to be deleted but unable to delete")

    @Before
    fun setupPresenter() {
        whenever(exporter.observe()).thenReturn(exporterPublisher)
        whenever(provider.getCollections()).thenReturn(Observable.just(ArrayList()))
        whenever(store.createCollection("cool collection")).thenReturn(Observable.just(MovieCollection.create("cool collection")))
        whenever(store.createCollection("uncool collection")).thenReturn(Observable.error(Throwable("unable to create collection")))
        whenever(store.deleteCollection(collectionToBeDeleted)).thenReturn(Observable.just(true))
        whenever(store.deleteCollection(collectionToBeDeletedError)).thenReturn(Observable.error(Throwable("unable to delete this collection")))
        whenever(store.deleteCollection(collectionToBeDeletedButUnableToDelete)).thenReturn(Observable.just(false))
    }

    @Test
    fun `attach presenter`() {
        presenter.attachView(view)
        assertNotNull(presenter.getView())
        verify(exporter).observe()
        verify(provider).getCollections()
        verify(view).updateState(any<CollectionListPage.State>())
    }

    @Test
    fun `detach presenter`() {
        presenter.attachView(view)
        presenter.detachView(view)
        assertNull(presenter.getView())
        verify(exporter).release()
    }

    @Test
    fun `unattached presenter`() {
        verifyZeroInteractions(view)
        verifyZeroInteractions(exporter)
        verifyZeroInteractions(provider)
        verifyZeroInteractions(store)
    }

    @Test
    fun `collections available`() {
        val collections = ArrayList<MovieCollection>()
        collections.add(MovieCollection.create("collection 1"))
        collections.add(MovieCollection.create("collection 2"))
        whenever(provider.getCollections()).thenReturn(Observable.just(collections))

        presenter.attachView(view)
        argumentCaptor<CollectionListPage.State.Success>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(2, firstValue.collections.size)
            assertEquals("collection 1", firstValue.collections[0].name)
            assertEquals("collection 2", firstValue.collections[1].name)
        }
    }

    @Test
    fun `empty collections`() {
        presenter.attachView(view)
        verify(view).updateState(CollectionListPage.State.Empty)
    }

    @Test
    fun `error loading collections`() {
        whenever(provider.getCollections()).thenReturn(Observable.error(Throwable("Error loading collections")))
        presenter.attachView(view)
        verify(view).updateState(CollectionListPage.State.Error)
    }

    @Test
    fun `create new collection`() {
        presenter.attachView(view)
        presenter.createCollection("cool collection")
        argumentCaptor<CollectionListPage.OpState.Created>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals("cool collection", firstValue.data)
        }
    }

    @Test
    fun `create new collection error`() {
        presenter.attachView(view)
        presenter.createCollection("uncool collection")
        argumentCaptor<CollectionListPage.OpState.CreateError>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals("uncool collection", firstValue.data)
        }
    }

    @Test
    fun `delete collection`() {
        presenter.attachView(view)
        presenter.deleteCollection(collectionToBeDeleted)
        argumentCaptor<CollectionListPage.OpState.Deleted>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(collectionToBeDeleted.name, firstValue.data)
        }
    }

    @Test
    fun `delete collection error`() {
        presenter.attachView(view)
        presenter.deleteCollection(collectionToBeDeletedError)
        argumentCaptor<CollectionListPage.OpState.DeleteError>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(collectionToBeDeletedError.name, firstValue.data)
        }
    }

    @Test
    fun `delete collection but unable to deleted`() {
        presenter.attachView(view)
        presenter.deleteCollection(collectionToBeDeletedButUnableToDelete)
        argumentCaptor<CollectionListPage.OpState.DeleteError>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(collectionToBeDeletedButUnableToDelete.name, firstValue.data)
        }
    }

    @Test
    fun `share collections`() {

        val uri: Uri = mock()

        whenever(exporter.export(any(), any())).then {
            exporterPublisher.onNext(DataExporter.Progress.Started())
            exporterPublisher.onNext(DataExporter.Progress.Success(uri))
        }

        presenter.attachView(view)
        presenter.share()

        verify(exporter).export(any(), eq(DataExporter.Config(false, true, false)))
        argumentCaptor<CollectionListPage.ShareState>().apply {
            verify(view, times(2)).updateState(capture())
            assertEquals(2, allValues.size)
            assertEquals(CollectionListPage.ShareState.Started, firstValue)
            assertTrue(secondValue is CollectionListPage.ShareState.Success)
        }
    }

    @Test
    fun `share collections error`() {

        whenever(exporter.export(any(), any())).then {
            exporterPublisher.onNext(DataExporter.Progress.Started())
            exporterPublisher.onNext(DataExporter.Progress.Error())
        }

        presenter.attachView(view)
        presenter.share()

        verify(exporter).export(any(), eq(DataExporter.Config(false, true, false)))
        argumentCaptor<CollectionListPage.ShareState>().apply {
            verify(view, times(2)).updateState(capture())
            assertEquals(2, allValues.size)
            assertEquals(CollectionListPage.ShareState.Started, firstValue)
            assertEquals(CollectionListPage.ShareState.Error, secondValue)
        }
    }

    @Test
    fun `share collections crash error`() {

        whenever(exporter.export(any(), any())).then {
            exporterPublisher.onError(Throwable("share collections error"))
        }

        presenter.attachView(view)
        presenter.share()

        verify(exporter).export(any(), eq(DataExporter.Config(false, true, false)))
        argumentCaptor<CollectionListPage.ShareState>().apply {
            verify(view, times(1)).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(CollectionListPage.ShareState.Error, firstValue)
        }
    }

}