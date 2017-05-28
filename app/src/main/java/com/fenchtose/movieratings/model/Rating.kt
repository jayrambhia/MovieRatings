package com.fenchtose.movieratings.model

import com.google.gson.annotations.SerializedName

class Rating(@SerializedName("Source") val source: String, @SerializedName("Value") val value: String) {
    override fun toString(): String {
        return "Rating(source='$source', value='$value')"
    }
}