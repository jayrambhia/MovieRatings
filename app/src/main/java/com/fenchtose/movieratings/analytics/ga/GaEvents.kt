package com.fenchtose.movieratings.analytics.ga

import com.fenchtose.movieratings.analytics.events.GaEvent

class GaEvents {
    companion object {
        val SEARCH = GaEvent("%s", "search", "%s")
        val SEARCH_MORE = GaEvent("%s", "search more", "page: %d")
        val CLEAR_SEARCH = GaEvent("%s", "tap", "clear")
        val OPEN_MOVIE = GaEvent("%s", "open movie", "title: %s")
        val OPEN_SETTINGS = GaEvent("%s", "tap", "settings")
        val TAP_RATE_APP = GaEvent("%s", "tap", "rate app")
        val TAP_SHARE_APP = GaEvent("%s", "tap", "share app")
        val TAP_ACTIVATE_FLUTTER = GaEvent("app", "tap", "activate flutter")
        val TAP_SUPPORT_APP = GaEvent("%s", "tap", "support app")

        val LIKE_MOVIE = GaEvent("%s", "toggle", "like: %s")
        val OPEN_LIKED_PAGE = GaEvent("search", "tap", "likes")
        val OPEN_HISTORY_PAGE = GaEvent("search", "tap", "history")
        val OPEN_INFO_PAGE = GaEvent("search", "tap", "info")
        val OPEN_COLLECTIONS_PAGE = GaEvent("search", "tap", "collections")
    }
}

class GaCategory {
    companion object {
        val SEARCH = "search"
        val COLLECTION_SEARCH = "collection search"
    }
}