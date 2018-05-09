package com.fenchtose.movieratings.util

import android.util.Log


class FixTitleUtils {
    companion object {

        val PRIMEVIDEO_PATTERN = "[^a-zA-Z0-9 ]".toRegex()
        val NETFLIX_YEAR_PATTERN = "[1-9]\\d{3}".toRegex()
        val PLAY_MOVIES_YEAR_TIME_PATTERN = "[1-9]\\d{3},\\s\\d+\\s\\w+".toRegex()

        fun fixPrimeVideoTitle(title: String): String {
            return title.split(PRIMEVIDEO_PATTERN)[0]
        }

        fun fixNetflixYear(year: String): String? {
            return NETFLIX_YEAR_PATTERN.find(year)?.value
        }

        fun fixPrimeVideoYear(year: String): String? {
            return NETFLIX_YEAR_PATTERN.find(year)?.value
        }

        fun fixPlayMoviesYear(year: String): String? {
            return NETFLIX_YEAR_PATTERN.find(year)?.value
        }

        fun matchesPlayMoviesYear(year: String): Boolean {
            return PLAY_MOVIES_YEAR_TIME_PATTERN.matches(year)
        }
    }
}