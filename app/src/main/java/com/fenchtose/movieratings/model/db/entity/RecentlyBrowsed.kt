package com.fenchtose.movieratings.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "RECENTLY_BROWSED")
class RecentlyBrowsed {
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "IMDBID")
    var id: String = ""

    @SerializedName("timestamp")
    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Long = 0
}