package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.R

class Constants {
    companion object {
        const val OMDB_ENDPOINT = "https://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.DEBUG && BuildConfig.OMDB_API_KEY.isEmpty()
        const val PACKAGE_NETFLIX = "com.netflix.mediaclient"
        const val PACKAGE_PRIMEVIDEO = "com.amazon.avod.thirdpartyclient"
        const val PACKAGE_PLAY_MOVIES_TV = "com.google.android.videos"
        const val PACKAGE_HOTSTAR = "in.startv.hotstar"
        const val PACKAGE_YOUTUBE = "com.google.android.youtube"
        const val PACKAGE_BBC_IPLAYER = "bbc.iplayer.android"
        const val PACKAGE_JIO_TV = "com.jio.jioplay.tv"
        const val PACKAGE_JIO_CINEMA = "com.jio.media.ondemand"
        const val PACKAGE_REDBOX = "com.redbox.android.activity"
        const val PACKAGE_DISNEY = "com.disney.disneyplus"

        const val APP_SHARE_URL = "http://bit.ly/movieRatings"

        const val EXPORT_MOVIES = "movies"
        const val EXPORT_COLLECTIONS = "collections"
        const val EXPORT_LIKES = "likes"
        const val EXPORT_APP = "app"
        const val EXPORT_VERSION = "version"
        const val EXPORT_RECENTLY_BROWSED = "recently_browsed"
        const val EXPORT_APP_NAME = "Flutter"

        val supportedApps = hashMapOf(
            Pair(PACKAGE_NETFLIX, R.string.settings_netflix),
            Pair(PACKAGE_PRIMEVIDEO, R.string.settings_primevideo),
            Pair(PACKAGE_PLAY_MOVIES_TV, R.string.settings_playmovies),
            Pair(PACKAGE_BBC_IPLAYER, R.string.settings_bbc_iplayer),
//            Pair(PACKAGE_HOTSTAR, R.string.settings_hotstar),
            Pair(PACKAGE_JIO_TV, R.string.settings_jio_tv),
            Pair(PACKAGE_JIO_CINEMA, R.string.settings_jio_cinema),
            Pair(PACKAGE_REDBOX, R.string.settings_redbox),
            Pair(PACKAGE_DISNEY, R.string.settings_disney)
        )

        const val SUPPORT_CHANNEL_ID = "support"
        const val SUPPORT_APP_NOTIFICATION_ID = 21
        const val REVIEW_APP_NOTIFICATION_ID = 22

        const val RATING_TYPE_SERIES = "tvSeries"

    }

    enum class TitleType(val type: String) {
        MOVIE("movie"),
        SERIES("series"),
        EPISODE("episode"),
        INVALID("invalid")
    }
}