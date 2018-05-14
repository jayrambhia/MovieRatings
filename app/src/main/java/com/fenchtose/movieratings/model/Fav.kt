package com.fenchtose.movieratings.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "FAVS")
class Fav {
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "IMDBID")
    var id: String = ""

    @SerializedName("liked")
    @ColumnInfo(name = "IS_FAV")
    var liked: Boolean = false
}