package com.fenchtose.movieratings.util

import com.fenchtose.movieratings.BuildConfig

class Constants {
    companion object {
        val OMDB_ENDPOINT = "http://www.omdbapi.com/"
        val USE_DUMMY_API = BuildConfig.USE_DUMMY_API && BuildConfig.DEBUG
    }
}