package com.fenchtose.movieratings.features.moviecollection

import android.content.Context
import android.net.Uri
import com.fenchtose.movieratings.base.router.Router
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPage
import com.fenchtose.movieratings.features.moviecollection.collectionpage.CollectionPagePresenter
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.entity.Sort
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
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("IllegalIdentifier")
class CollectionPageTests {
    private val context: Context = mock()
    private val exporterPublisher: PublishSubject<DataExporter.Progress<Uri>> = PublishSubject.create()

    private val view: CollectionPage = mock {
//        on {updateState(any<BaseMovieListPage.State>())}.then {
//            System.out.println("CollectionPageTests - update state ${it.arguments[0]}")
//        }
    }
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

    private val router: Router = mock()

    private val collection = MovieCollection.create("cool collection")

    private val rxHooks = TestRxHooks()
    private val fileUtils = TestFileUtils()

    private var presenter = CollectionPagePresenter(likeStore, rxHooks, fileUtils, provider, store, userPreferences, exporter, collection, router)

    private val m1: Movie = Movie()
    private val m2: Movie = Movie()
    private val m3: Movie = Movie()
    private val m4: Movie = Movie()

    private val collectionEntry = MovieCollectionEntry.create(collection, m1)

    init {
        m1.imdbId = "tt1"
        m1.title = "movie 1"
        m2.year = "2001"

        m2.imdbId = "tt2"
        m2.title = "movie 2"
        m2.year = "1999"

        m3.imdbId = "tt3"
        m3.title = "movie 3"

        movies.add(m1)
        movies.add(m2)

        collection.id = 124
    }

    @Before
    fun setupMocks() {
        whenever(store.removeMovieFromCollection(eq(collection), eq(m1))).thenReturn(Observable.just(true))
        whenever(store.removeMovieFromCollection(eq(collection), eq(m2))).thenReturn(Observable.just(true))
        whenever(store.removeMovieFromCollection(collection, m3)).thenReturn(Observable.just(false))
        whenever(store.removeMovieFromCollection(collection, m4)).thenReturn(Observable.error(Throwable("null id for movie")))

        whenever(store.addMovieToCollection(eq(collection), eq(m1))).thenReturn(Observable.just(collectionEntry))
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

    @Test
    fun `load collection`() {
        presenter.attachView(view)
        argumentCaptor<BaseMovieListPage.State.Success>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(2, firstValue.movies.size)
            assertEquals("tt1", firstValue.movies[0].imdbId)
            assertEquals("tt2", firstValue.movies[1].imdbId)
            assertEquals(2, presenter.getDataForTest()?.size)
        }
    }

    @Test
    fun `load empty collection`() {
        presenter.attachView(view)
        whenever(provider.getCollections()).thenReturn(Observable.just(ArrayList()))
        argumentCaptor<BaseMovieListPage.State.Empty>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
        }
    }

    @Test
    fun `load collection error`() {
        presenter.attachView(view)
        whenever(provider.getCollections()).thenReturn(Observable.error(Throwable("Load collection error")))
        argumentCaptor<BaseMovieListPage.State.Error>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
        }
    }

    @Test
    fun `remove movie from collection`() {
        presenter.attachView(view)
        presenter.removeMovie(m1)
        verify(store).removeMovieFromCollection(eq(collection), eq(m1))
        argumentCaptor<CollectionPage.OpState.Removed>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(m1, firstValue.movie)
            assertEquals(0, firstValue.position)
            assertEquals(movies.size - 1, presenter.getDataForTest()?.size)
        }
    }

    @Test
    fun `unable to remove movie from collection`() {
        presenter.attachView(view)
        presenter.removeMovie(m3)
        verify(store).removeMovieFromCollection(eq(collection), eq(m3))
        argumentCaptor<CollectionPage.OpState.RemoveError>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(m3, firstValue.movie)
            assertEquals(movies.size, presenter.getDataForTest()?.size)
        }
    }

    @Test
    fun `error in removing movie from collection`() {
        presenter.attachView(view)
        presenter.removeMovie(m4)
        verify(store).removeMovieFromCollection(eq(collection), eq(m4))
        argumentCaptor<CollectionPage.OpState.RemoveError>().apply {
            verify(view).updateState(capture())
            assertEquals(1, allValues.size)
            assertEquals(m4, firstValue.movie)
            assertEquals(movies.size, presenter.getDataForTest()?.size)
        }
    }

    @Test
    fun `remove all movies from collection`() {
        presenter.attachView(view)

        presenter.removeMovie(m1)
        assertEquals(1, presenter.getDataForTest()?.size)
        presenter.removeMovie(m2)
        assertEquals(0, presenter.getDataForTest()?.size)

        verify(store).removeMovieFromCollection(eq(collection), eq(m1))
        verify(store).removeMovieFromCollection(eq(collection), eq(m2))


        argumentCaptor<CollectionPage.OpState.Removed>().apply {
            verify(view, times(2)).updateState(capture())
            assertEquals(2, allValues.size)
            assertEquals(m1, firstValue.movie)
            assertEquals(m2, secondValue.movie)
            assertEquals(0, presenter.getDataForTest()?.size)
        }

        argumentCaptor<BaseMovieListPage.State>().apply {
            // for initial loading and then empty state
            verify(view, times(2)).updateState(capture())
            assertEquals(2, allValues.size)
        }
    }

    @Test
    fun `undo remove movie from collection`() {
        presenter.attachView(view)
        presenter.removeMovie(m1)

        var removed: Movie? = null
        var index: Int = -1

        argumentCaptor<CollectionPage.OpState.Removed>().apply {
            verify(view).updateState(capture())
            removed = firstValue.movie
            index = firstValue.position
        }

        assertNotNull(removed, "Movie should not be null")

        removed?.let {
            presenter.undoRemove(it, index)
            argumentCaptor<CollectionPage.OpState.Added>().apply {
                // 2 because we removed it once
                verify(view, times(2)).updateState(capture())
                assertEquals(m1, secondValue.movie)
                assertEquals(index, secondValue.position)
                assertEquals(movies.size, presenter.getDataForTest()?.size)
            }
        }
    }

    @Test
    fun `sort collection`() {
        presenter.attachView(view)
        presenter.sort(Sort.YEAR)
        assertEquals(Sort.YEAR, presenter.getSort())
        assertEquals(movies.size, presenter.getDataForTest()?.size)
        assertEquals(movies.reversed(), presenter.getDataForTest())

        // sort preference updated
        verify(userPreferences).setLatestCollectionSort(collection.id, Sort.YEAR)

        // verify chain of events
        argumentCaptor<BaseMovieListPage.State.Success>().apply {
            // initial set data and then sort
            verify(view, times(2)).updateState(capture())
            assertEquals(movies.size, secondValue.movies.size)
            assertEquals(m1, secondValue.movies[1])
        }
    }

    @Test
    fun `sort empty collection`() {
        whenever(provider.getMoviesForCollection(any())).thenReturn(Observable.just(ArrayList()))
        presenter.attachView(view)
        presenter.sort(Sort.YEAR)
        assertEquals(Sort.YEAR, presenter.getSort())
        assertEquals(0, presenter.getDataForTest()?.size)

        // sort preference updated
        verify(userPreferences).setLatestCollectionSort(collection.id, Sort.YEAR)

        // verify chain of events
        argumentCaptor<BaseMovieListPage.State.Empty>().apply {
            // initial set data and then sort
            verify(view, times(2)).updateState(capture())
        }
    }

    @Test
    fun `same sort does not do anything`() {
        presenter.attachView(view)
        presenter.sort(Sort.ALPHABETICAL)

        // sort preference never updated
        verify(userPreferences, never()).setLatestCollectionSort(any(), any())

        // verify chain of events
        argumentCaptor<BaseMovieListPage.State.Empty>().apply {
            // initial set data and sort naver happens
            verify(view, times(1)).updateState(capture())
        }
    }

    @Test
    fun `repeat sort type after setting different sort`() {
        presenter.attachView(view)
        presenter.sort(Sort.YEAR)
        assertEquals(Sort.YEAR, presenter.getSort())
        assertEquals(movies.size, presenter.getDataForTest()?.size)
        assertEquals(movies.reversed(), presenter.getDataForTest())

        presenter.sort(Sort.YEAR)
        assertEquals(Sort.YEAR, presenter.getSort())
        assertEquals(movies.size, presenter.getDataForTest()?.size)

        // sort preference updated but only once
        verify(userPreferences).setLatestCollectionSort(collection.id, Sort.YEAR)

        // verify chain of events
        argumentCaptor<BaseMovieListPage.State.Success>().apply {
            // initial set data and then sort the first time but does not happen the second time
            verify(view, times(2)).updateState(capture())
            assertEquals(movies.size, secondValue.movies.size)
            assertEquals(m1, secondValue.movies[1])
        }
    }

    @Test
    fun `alternate sort collection`() {
        presenter.attachView(view)
        presenter.sort(Sort.YEAR)
        assertEquals(Sort.YEAR, presenter.getSort())
        assertEquals(movies.size, presenter.getDataForTest()?.size)
        assertEquals(movies.reversed(), presenter.getDataForTest())

        presenter.sort(Sort.ALPHABETICAL)
        assertEquals(Sort.ALPHABETICAL, presenter.getSort())
        assertEquals(movies.size, presenter.getDataForTest()?.size)
        assertEquals(movies, presenter.getDataForTest())

        // sort preference updated
        verify(userPreferences).setLatestCollectionSort(collection.id, Sort.YEAR)
        verify(userPreferences).setLatestCollectionSort(collection.id, Sort.ALPHABETICAL)

        // verify chain of events
        argumentCaptor<BaseMovieListPage.State.Success>().apply {
            // initial set data and then sort the first time and second time also
            verify(view, times(3)).updateState(capture())
            assertEquals(movies.size, secondValue.movies.size)
            assertEquals(m1, secondValue.movies[1])
            assertEquals(m1, thirdValue.movies[0])
        }
    }

}