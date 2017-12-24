package com.fenchtose.movieratings.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "RECENTLY_BROWSED")
class RecentlyBrowsed {
    @PrimaryKey
    @ColumnInfo(name = "IMDBID")
    var id: String = ""

    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Long = 0
}