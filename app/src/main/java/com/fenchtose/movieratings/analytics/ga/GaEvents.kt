package com.fenchtose.movieratings.analytics.ga

import com.fenchtose.movieratings.analytics.events.GaEvent

class GaEvents {
    companion object {
        val SEARCH = GaEvent("%s", "search", "%s")
        val SEARCH_MORE = GaEvent("%s", "search more", "page: %d")
        val CLEAR_SEARCH = GaEvent("%s", "tap", "clear")
        val OPEN_MOVIE = GaEvent("%s", "open movie", "open movie")
        val OPEN_SETTINGS = GaEvent("%s", "tap", "settings")
        val REPORT_BUG = GaEvent("%s", "tap", "report bug")
        val OPEN_PRIVACY_POLICY = GaEvent("%s", "tap", "privacy policy")
        val TAP_RATE_APP = GaEvent("%s", "tap", "rate app")
        val TAP_SHARE_APP = GaEvent("%s", "tap", "share app")
        val TAP_ACTIVATE_FLUTTER = GaEvent("app", "tap", "activate flutter")
        val OPEN_SUPPORT_APP = GaEvent("%s", "open", "support app")
        val TAP_TRENDING_PAGE = GaEvent("search", "tap", "trending")

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

        val GET_RATINGS = GaEvent(GaCategory.SERVICE, "get rating", "app: %s", true)
        val GET_RATINGS_ONLINE = GaEvent(GaCategory.SERVICE, "get rating online", "server: %s", true)
        val RATING_NOT_FOUND = GaEvent(GaCategory.SERVICE, "rating not found", "server: %s", true)
        val SHOW_RATINGS = GaEvent(GaCategory.SERVICE, "show rating", "%s", true)
        val SEND_NOTIFICATION = GaEvent("%s", "send notification", "%s", true)
        val NOTIFICATION_BLOCKED = GaEvent(GaCategory.SERVICE, "notification blocked", "%s", true)
        val OPEN_NOTIFICATION = GaEvent(GaCategory.SERVICE, "open notification", "%s")
        val DISMISS_RATING = GaEvent(GaCategory.SERVICE, "dismiss", "rating")
        val RATING_OPEN_MOVIE = GaEvent(GaCategory.SERVICE, "open movie", "%s")
        val RATING_OPEN_404 = GaEvent(GaCategory.SERVICE, "open 404", "movie not found")

        val SPEAK_RATING = GaEvent(GaCategory.SERVICE, "tts", "rating", true)

        val SELECT_BOTTOM_TAB = GaEvent(GaCategory.BOTTOM_NAVIGATION, "select", "tab: %s")

        val UPDATE_BANNER_SHOWN = GaEvent(GaCategory.UPDATE_BANNER, "shown", "banner: %s")
        val UPDATE_BANNER_DISMISS = GaEvent(GaCategory.UPDATE_BANNER, "dismiss", "banner: %s")
        val UPDATE_BANNER_CTA = GaEvent(GaCategory.UPDATE_BANNER, "cta", "banner: %s")
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
        const val BOTTOM_NAVIGATION = "bottom navigation"
        const val DEBUGGING = "debugging"
        const val UPDATE_BANNER = "update banner"
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
        const val DEBUGGING = "debugging"
        const val BATTERY_OPTIMIZATION_INFO = "battery optimization info"
        const val SETTINGS_NOTIFICATION_SECTION = "settings notification section"
    }
}

class GaLabels {
    companion object {
        const val NOTIFICATION_SUPPORT_APP = "support app"
        const val NOTIFICATION_RATE_APP = "rate app"
        const val TOAST = "toast"
        const val BUBBLE_BIG = "bubble big"
        const val BUBBLE_SMALL = "bubble small"
        const val OMDB_API = "omdb"
        const val FLUTTER_API = "flutter"

        const val ITEM_SEARCH = "search"
        const val ITEM_PERSONAL = "personal"
        const val ITEM_COLLECTIONS = "collections"
        const val ITEM_INFO = "info"
        const val RATING_404 = "rating 404"
    }
}