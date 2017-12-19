package com.fenchtose.movieratings.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "FAVS")
class Fav {
    @PrimaryKey
    @ColumnInfo(name = "IMDBID")
    var id: String = ""

    @ColumnInfo(name = "IS_FAV")
    var liked: Boolean = false
}