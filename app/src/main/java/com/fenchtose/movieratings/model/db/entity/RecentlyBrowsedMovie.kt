package com.fenchtose.movieratings.model.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Relation
import com.fenchtose.movieratings.model.db.entity.Movie

class RecentlyBrowsedMovie {
    @ColumnInfo(name = "IMDBID")
    var id: String = ""

    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Long = 0

    @Relation(parentColumn = "IMDBID", entityColumn = "IMDBID")
    var movies: List<Movie>? = null

    @Ignore
    var movie: Movie? = null

}