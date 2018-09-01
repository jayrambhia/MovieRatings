package com.fenchtose.movieratings.model.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Relation
import com.fenchtose.movieratings.model.db.entity.Movie

class CollectedMovie {
    @ColumnInfo(name = "COLLECTION_ID")
    var collectionId: String = ""

    @ColumnInfo(name = "IMDBID")
    var movieId: String = ""

    @Relation(parentColumn = "IMDBID", entityColumn = "IMDBID")
    var movies: List<Movie>? = null

    @Ignore
    var movie: Movie? = null

    @ColumnInfo(name = "CREATED_AT")
    var createdAt: Long = 0

    @ColumnInfo(name = "UPDATED_AT")
    var updatedAt: Long = 0

    @ColumnInfo(name = "IS_DELETED")
    var deleted: Int = 0
}