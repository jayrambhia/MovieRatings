package com.fenchtose.movieratings.model.db.entity

import androidx.room.*
import com.google.gson.annotations.SerializedName

@Entity(tableName = "COLLECTION_ENTRIES")
class MovieCollectionEntry {

    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @SerializedName("collection_id")
    @ColumnInfo(name = "COLLECTION_ID")
    var collectionId: Long = -1

    @SerializedName("movie_id")
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