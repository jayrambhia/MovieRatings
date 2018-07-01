package com.fenchtose.movieratings.model.entity

import com.google.gson.annotations.SerializedName

class SearchResult {
    @SerializedName("Search")
    var results: ArrayList<Movie> = ArrayList()

    @SerializedName("totalResults")
    var total: Int = 0

    @SerializedName("Response")
    var success: Boolean = false
}