package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.R

class Constants {
    companion object {
        val OMDB_ENDPOINT = "https://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.DEBUG && BuildConfig.OMDB_API_KEY.isEmpty()
        val PACKAGE_NETFLIX = "com.netflix.mediaclient"
        val PACKAGE_PRIMEVIDEO = "com.amazon.avod.thirdpartyclient"
        val PACKAGE_PLAY_MOVIES_TV = "com.google.android.videos"
        val PACKAGE_HOTSTAR = "in.startv.hotstar"
        val PACKAGE_YOUTUBE = "com.google.android.youtube"
        val PACKAGE_BBC_IPLAYER = "bbc.iplayer.android"
        val PACKAGE_JIO_TV = "com.jio.jioplay.tv"
        val PACKAGE_JIO_CINEMA = "com.jio.media.ondemand"
        val APP_SHARE_URL = "https://goo.gl/y3HXVi"

        val EXPORT_MOVIES = "movies"
        val EXPORT_COLLECTIONS = "collections"
        val EXPORT_LIKES = "likes"
        val EXPORT_APP = "app"
        val EXPORT_VERSION = "version"
        val EXPORT_RECENTLY_BROWSED = "recently_browsed"
        val EXPORT_APP_NAME = "Flutter"

        val supportedApps = hashMapOf(
                Pair(PACKAGE_NETFLIX, R.string.settings_netflix),
                Pair(PACKAGE_PRIMEVIDEO, R.string.settings_primevideo),
                Pair(PACKAGE_PLAY_MOVIES_TV, R.string.settings_playmovies),
                Pair(PACKAGE_BBC_IPLAYER, R.string.settings_bbc_iplayer),
                Pair(PACKAGE_HOTSTAR, R.string.settings_hotstar),
                Pair(PACKAGE_JIO_TV, R.string.settings_jio_tv),
                Pair(PACKAGE_JIO_CINEMA, R.string.settings_jio_cinema)
        )

    }

    enum class TitleType(val type: String) {
        MOVIE("movie"),
        SERIES("series"),
        EPISODE("episode"),
        INVALID("invalid")
    }
}