package com.fenchtose.movieratings.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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