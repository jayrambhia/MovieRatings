package com.fenchtose.movieratings.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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