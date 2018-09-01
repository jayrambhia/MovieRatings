package com.fenchtose.movieratings.analytics.ga

import com.fenchtose.movieratings.analytics.events.GaEvent

class GaEvents {
    companion object {
        val SEARCH = GaEvent("%s", "search", "%s")
        val SEARCH_MORE = GaEvent("%s", "search more", "page: %d")
        val CLEAR_SEARCH = GaEvent("%s", "tap", "clear")
        val OPEN_MOVIE = GaEvent("%s", "open movie", "open movie")
        val OPEN_SETTINGS = GaEvent("%s", "tap", "settings")
        val TAP_RATE_APP = GaEvent("%s", "tap", "rate app")
        val TAP_SHARE_APP = GaEvent("%s", "tap", "share app")
        val TAP_ACTIVATE_FLUTTER = GaEvent("app", "tap", "activate flutter")
        val OPEN_SUPPORT_APP = GaEvent("%s", "open", "support app")

        val LIKE_MOVIE = GaEvent("%s", "toggle", "like")
        val OPEN_LIKED_PAGE = GaEvent("search", "open", "likes")
        val OPEN_RECENTLY_BROWSED_PAGE = GaEvent("search", "open", "recently browsed")
        val OPEN_INFO_PAGE = GaEvent("search", "open", "info")
        val OPEN_COLLECTIONS_PAGE = GaEvent("search", "open", "collections")

        val SELECT_TRENDING_TAB = GaEvent(GaCategory.TRENDING, "select", "tab: %s")

        val UNDO_UNLIKE_MOVIE = GaEvent(GaCategory.LIKES, "undo", "unlike")
        val SORT = GaEvent("%s", "sort", "order: %s")

        val TAP_CREATE_COLLECTION = GaEvent(GaCategory.COLLECTION_LIST, "tap", "create collection")
        val CREATE_COLLECTION = GaEvent(GaCategory.COLLECTION_LIST, "create", "collection")
        val TAP_SHARE_COLLECTIONS = GaEvent(GaCategory.COLLECTION_LIST, "tap", "share collections")
        val SHARE_COLLECTIONS = GaEvent(GaCategory.COLLECTION_LIST, "share", "collections")
        val OPEN_COLLECTION = GaEvent("%s", "open", "collection")
        val SELECT_COLLECTION = GaEvent(GaCategory.COLLECTION_LIST, "select", "collection")
        val TAP_DELETE_COLLECTION = GaEvent(GaCategory.COLLECTION_LIST, "tap", "delete collection")
        val DELETE_COLLECTION = GaEvent(GaCategory.COLLECTION_LIST, "delete", "collection")

        val TAP_REMOVE_MOVIE = GaEvent(GaCategory.COLLECTION, "tap", "remove movie")
        val REMOVE_MOVIE = GaEvent(GaCategory.COLLECTION, "delete", "movie")
        val TAP_ADD_TO_COLLECTION = GaEvent("%s", "tap", "add to collection")
        val ADD_TO_COLLECTION = GaEvent("%s", "add", "add to collection")
        val TAP_SHARE_COLLECTION = GaEvent(GaCategory.COLLECTION, "tap", "share collection")
        val SHARE_COLLECTION = GaEvent(GaCategory.COLLECTION, "share", "collection")
        val TAP_SEARCH_FOR_COLLECTION = GaEvent(GaCategory.COLLECTION, "tap", "search to add to collection")

        val EXPAND_PLOT = GaEvent("%s", "expand", "plot")
        val COLLAPSE_PLOT = GaEvent("%s", "collapse", "plot")

        val TAP_SEASON_SELECTOR = GaEvent(GaCategory.MOVIE, "tap", "season select")
        val SELECT_SEASON = GaEvent(GaCategory.MOVIE, "select", "season")
        val OPEN_EPISODE = GaEvent(GaCategory.MOVIE, "open", "episode")
        val SELECT_EPISODE = GaEvent(GaCategory.SEASON, "select", "episode")
        val OPEN_IMDB = GaEvent("%s", "open", "imdb")

        val TAP_PURCHASE = GaEvent(GaCategory.SUPPORT_APP, "tap", "purchase: %s")
        val PURCHASED = GaEvent(GaCategory.SUPPORT_APP, "purchase", "sku: %s")

        val GET_RATINGS = GaEvent(GaCategory.SERVICE, "get rating", "app: %s")
        val GET_RATINGS_ONLINE = GaEvent(GaCategory.SERVICE, "get rating online", "server: %s")
    }
}

class GaCategory {
    companion object {
        const val ACCESSIBILITY = "accessibility"
        const val APP_INFO = "app info"
        const val COLLECTION = "collection"
        const val COLLECTION_LIST = "collection list"
        const val COLLECTION_SEARCH = "collection search"
        const val EPISODE = "episode"
        const val LIKES = "likes"
        const val MOVIE = "movie"
        const val RECENTLY_BROWSED = "recently browsed"
        const val SEARCH = "search"
        const val SEASON = "season"
        const val SERVICE = "service"
        const val SETTINGS = "settings"
        const val SUPPORT_APP = "support app"
        const val TRENDING = "trending"
    }
}

class GaScreens {
    companion object {
        const val ACCESS_INFO = "access info"
        const val APP_INFO = "app info"
        const val COLLECTION = "collection"
        const val COLLECTION_LIST = "collection list"
        const val COLLECTION_SEARCH = "collection search"
        const val IMPORT_DATA = "import data"
        const val LIKES = "likes"
        const val MOVIE = "movie"
        const val RECENTLY_BROWSED = "recently browsed"
        const val SEARCH = "search"
        const val SEASON = "season"
        const val SETTINGS = "settings"
        const val SETTINGS_APPS_SECTION = "settings apps section"
        const val SETTINGS_DATA_SECTION = "settings data section"
        const val SETTINGS_MISC_SECTION = "settings misc section"
        const val SETTINGS_RATING_SECTION = "settings rating section"
        const val SETTINGS_TTS_SECTION = "settings tts section"
        const val SUPPORT_APP = "support app"
        const val TRENDING = "trending"
    }
}