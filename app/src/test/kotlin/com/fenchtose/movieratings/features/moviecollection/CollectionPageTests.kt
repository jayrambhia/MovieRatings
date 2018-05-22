package com.fenchtose.movieratings.features.moviecollection

import android.content.Context
import android.net.Uri
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPage
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPagePresenter
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.Sort
import com.fenchtose.movieratings.model.api.provider.MovieCollectionProvider
import com.fenchtose.movieratings.model.db.like.LikeStore
import com.fenchtose.movieratings.model.db.movieCollection.MovieCollectionStore
import com.fenchtose.movieratings.model.offline.export.DataExporter
import com.fenchtose.movieratings.model.preferences.UserPreferences
import com.fenchtose.movieratings.util.TestFileUtils
import com.fenchtose.movieratings.util.TestRxHooks
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("IllegalIdentifier")
class CollectionPageTests {
    private val context: Context = mock()
    private val exporterPublisher: PublishSubject<DataExporter.Progress<Uri>> = PublishSubject.create()

    private val view: CollectionPage = mock()
    private val likeStore: LikeStore = mock()

    private val movies = ArrayList<Movie>()

    private val provider: MovieCollectionProvider = mock {
        on { getMoviesForCollection(any()) }.thenReturn(Observable.just(movies  as List<Movie>))
    }
    private val store: MovieCollectionStore = mock()
    private val exporter: DataExporter<Uri> = mock {
        on { observe() }.thenReturn(exporterPublisher)
    }
    private val userPreferences: UserPreferences = mock {
        on { getLatestCollectionSort(any()) }.doReturn(Sort.ALPHABETICAL)
    }
    private val collection = MovieCollection.create("cool collection")

    private val rxHooks = TestRxHooks()
    private val fileUtils = TestFileUtils()

    private var presenter = CollectionPagePresenter(likeStore, rxHooks, fileUtils, provider, store, userPreferences, exporter, collection)

    init {
        val m1: Movie = mock()
        movies.add(m1)
        movies.add(mock())
    }

    @Test
    fun `attach presenter`() {
        presenter.attachView(view)
        assertNotNull(presenter.getView())
        verify(provider).getMoviesForCollection(eq(collection))
    }

    @Test
    fun `detach presenter`() {
        presenter.attachView(view)
        presenter.detachView(view)
        assertNull(presenter.getView(), "view should be null because detached")
    }

    @Test
    fun `presenter not attached`() {
        verifyZeroInteractions(view)
        verifyZeroInteractions(exporter)
        verifyZeroInteractions(store)
        verifyZeroInteractions(likeStore)
        verify(provider).addPreferenceApplier(any())
        verifyNoMoreInteractions(provider)
    }

}