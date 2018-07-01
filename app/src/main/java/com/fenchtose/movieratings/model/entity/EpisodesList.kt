package com.fenchtose.movieratings.model.entity

import com.google.gson.annotations.SerializedName

class EpisodesList {
    @SerializedName("Title")
    var title: String = ""

    @SerializedName("Season")
    var season: Int = -1

    @SerializedName("totalSeasons")
    var totalSeasons: Int = -1

    @SerializedName("Episodes")
    var episodes: ArrayList<Episode> = ArrayList()

    @SerializedName("Response")
    var success: Boolean = false

    override fun toString(): String {
        return "EpisodesList(title='$title', season=$season, totalSeasons=$totalSeasons, episodes=$episodes, success=$success)"
    }


}