package com.fenchtose.movieratings.model

import android.arch.persistence.room.*

@Entity(tableName = "COLLECTION_ENTRIES")
class MovieCollectionEntry {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "COLLECTION_ID")
    var collectionId: Long = -1

    @ColumnInfo(name = "IMDBID")
    var movieId: String = ""

    @Transient
    @ColumnInfo(name = "CREATED_AT")
    var createdAt: Long = 0

    @Transient
    @ColumnInfo(name = "UPDATED_AT")
    var updatedAt: Long = 0

    @Transient
    @ColumnInfo(name = "IS_DELETED")
    var deleted: Int = 0

    companion object {
        fun create(collection: MovieCollection, movie: Movie): MovieCollectionEntry {
            return create(collection.id, movie.imdbId)
        }

        fun create(collectionId: Long, movieId: String): MovieCollectionEntry {
            val entry = MovieCollectionEntry()
            entry.collectionId = collectionId
            entry.movieId = movieId
            entry.createdAt = System.currentTimeMillis()
            entry.updatedAt = entry.createdAt
            return entry
        }
    }
}