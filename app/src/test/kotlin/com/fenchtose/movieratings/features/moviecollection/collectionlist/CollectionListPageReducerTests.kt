package com.fenchtose.movieratings.features.moviecollection.collectionlist

import com.fenchtose.movieratings.model.db.movieCollection.UpdateCollectionOp
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CollectionListPageReducerTests {

    val emptyState = CollectionListPageState()
    val movies = listOf(
            Movie("1", "test 1", "year 1", "Movie", "poster 1"),
            Movie("2", "test 2", "year 2", "Movie", "poster 2"),
            Movie("3", "test 3", "year 3", "Movie", ""),
            Movie("4", "test 4", "year 4", "TV Series", "poster 4")
    )

    val collections = listOf(
            MovieCollection(1, "collection 1", listOf()),
            MovieCollection(2, "collection 2", listOf(movies[0], movies[2]))
    )


    @Before
    fun setup() {
    }

    @Test
    fun `dispatch init action`() {
        val updated = emptyState.reduce(InitAction)
        assertEquals(emptyState, updated, "Init action should create an empty state")
    }

    @Test
    fun `dispatch update action`() {
        val progress = arrayOf(Progress.Loading, Progress.Error)
        progress.forEach {
            val updated = emptyState.reduce(UpdateProgress(it))
            assertEquals(it, updated.progress, "progress value is incorrect")
        }
    }

    @Test
    fun `dispatch content loaded action`() {
        val updated = emptyState.reduce(UpdateProgress(Progress.Loading)).reduce(CollectionsLoaded(collections))
        assertEquals(Progress.Loaded, updated.progress, "Progress value is not correct")
        assertEquals(collections, updated.collections, "collections do not match")
    }

    @Test
    fun `dispatch clear action`() {
        val updated = emptyState.reduce(UpdateProgress(Progress.Loading)).reduce(CollectionsLoaded(collections))
                .copy(shareSuccess = true).reduce(ClearAction)
        assertEquals(emptyState, updated, "ClearAction should create new empty state")
    }

    @Test
    fun `state is unchanged when UpdateCollectionOp is dispatched - state is not active`() {
        val updated = emptyState.reduce(UpdateCollectionOp(CollectionOp.Created("test", MovieCollection.invalid())))
        assertEquals(emptyState, updated, "inactive state does not react to UpdateCollectionOp")
        assert(emptyState === updated)
    }

    @Test
    fun `create new collection`() {
        val collection = MovieCollection(3, "test", listOf())
        val updated = emptyState.reduce(CollectionsLoaded(collections)).reduce(UpdateCollectionOp(CollectionOp.Created("test", collection)))
        assertEquals(3, updated.collections.size)
        assert(updated.collectionOp is CollectionOp.Created)
        assert(updated.collections.hasCollection(collection.name) != -1)
    }

    @Test
    fun `delete collection`() {
        val updated = emptyState.reduce(CollectionsLoaded(collections)).reduce(UpdateCollectionOp(CollectionOp.Deleted("collection 1")))
        assertEquals(1, updated.collections.size)
        assert(updated.collectionOp is CollectionOp.Deleted)
        assert(updated.collections.hasCollection("collection 1") == -1)
    }

    @Test
    fun `clear collection op`() {
        val updated = emptyState.reduce(CollectionsLoaded(collections))
                .reduce(UpdateCollectionOp(CollectionOp.Deleted("collection 1")))
                .reduce(ClearCollectionOp)
        assert(updated.collectionOp == null)
    }
}