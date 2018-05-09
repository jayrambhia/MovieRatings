package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig

class Constants {
    companion object {
        val OMDB_ENDPOINT = "https://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.DEBUG && BuildConfig.OMDB_API_KEY.isEmpty()
        val PACKAGE_NETFLIX = "com.netflix.mediaclient"
        val PACKAGE_PRIMEVIDEO = "com.amazon.avod.thirdpartyclient"
        val PACKAGE_PLAY_MOVIES_TV = "com.google.android.videos"
        val APP_SHARE_URL = "https://goo.gl/y3HXVi"

    }

    enum class TitleType(val type: String) {
        MOVIE("movie"),
        SERIES("series"),
        EPISODE("episode"),
        INVALID("invalid")
    }
}