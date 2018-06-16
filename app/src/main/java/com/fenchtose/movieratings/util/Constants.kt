package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig

class Constants {
    companion object {
        val OMDB_ENDPOINT = "https://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.DEBUG && BuildConfig.OMDB_API_KEY.isEmpty()
        val PACKAGE_NETFLIX = "com.netflix.mediaclient"
        val PACKAGE_PRIMEVIDEO = "com.amazon.avod.thirdpartyclient"
        val PACKAGE_PLAY_MOVIES_TV = "com.google.android.videos"
        val PACKAGE_HOTSTAR = "in.startv.hotstar"
        val PACKAGE_YOUTUBE = "com.google.android.youtube"
        val APP_SHARE_URL = "https://goo.gl/y3HXVi"

        val EXPORT_MOVIES = "movies"
        val EXPORT_COLLECTIONS = "collections"
        val EXPORT_LIKES = "likes"
        val EXPORT_APP = "app"
        val EXPORT_VERSION = "version"
        val EXPORT_RECENTLY_BROWSED = "recently_browsed"
        val EXPORT_APP_NAME = "Flutter"

    }

    enum class TitleType(val type: String) {
        MOVIE("movie"),
        SERIES("series"),
        EPISODE("episode"),
        INVALID("invalid")
    }
}