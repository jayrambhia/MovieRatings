package com.fenchtose.movieratings.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DISPLAYED_RATINGS")
data class DisplayedRating(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "IMDBID")
    val movieId: String,

    @ColumnInfo(name = "TIMESTAMP")
    val timestamp: Long,

    @ColumnInfo(name = "APP_PACKAGE")
    val appPackage: String
    )