package com.fenchtose.movieratings.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Relation

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