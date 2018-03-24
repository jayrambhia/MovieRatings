package com.fenchtose.movieratings.util

class FixTitleUtils {
    companion object {

        val PRIMEVIDEO_PATTERN = "[^a-zA-Z0-9 ]".toRegex()

        fun fixPrimeVideoTitle(title: String): String {
            return title.split(PRIMEVIDEO_PATTERN)[0]
        }
    }
}