package com.fenchtose.movieratings.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

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