package com.fenchtose.movieratings.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "EPISODES", indices = arrayOf(Index("IMDBID", unique=true)))
class Episode {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "TITLE")
    @SerializedName("Title")
    var title: String = ""

    @ColumnInfo(name = "RELEASED")
    @SerializedName("Released")
    var released: String = ""

    @ColumnInfo(name = "EPISODE")
    @SerializedName("Episode")
    var episode: Int = -1

    @ColumnInfo(name = "IMDBRATING")
    @SerializedName("imdbRating")
    var imdbRating: String = ""

    @ColumnInfo(name = "IMDBID")
    @SerializedName("imdbID")
    var imdbId: String = ""

    @ColumnInfo(name = "SERIESIMDBID")
    var seriesId: String = ""

    @ColumnInfo(name = "SEASON")
    var season: Int = -1

    override fun toString(): String {
        return "Episode(id=$id, title='$title', released='$released', episode=$episode, imdbRating='$imdbRating', imdbId='$imdbId', seriesId='$seriesId')"
    }


}