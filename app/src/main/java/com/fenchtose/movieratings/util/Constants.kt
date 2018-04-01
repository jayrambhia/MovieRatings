package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig

class Constants {
    companion object {
        val OMDB_ENDPOINT = "http://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.DEBUG && BuildConfig.OMDB_API_KEY.isEmpty()
        val PACKAGE_NETFLIX = "com.netflix.mediaclient"
        val PACKAGE_PRIMEVIDEO = "com.amazon.avod.thirdpartyclient"

    }

    enum class TitleType(val type: String) {
        MOVIE("movie"),
        SERIES("series"),
        EPISODE("episode"),
        INVALID("invalid")
    }
}