package com.fenchtose.movieratings.model.entity

import com.google.gson.annotations.SerializedName

class Rating(@SerializedName("Source") val source: String, @SerializedName("Value") val value: String) {
    override fun toString(): String {
        return "Rating(source='$source', value='$value')"
    }

    companion object {
        fun empty(): Rating {
            return Rating("", "")
        }
    }
}