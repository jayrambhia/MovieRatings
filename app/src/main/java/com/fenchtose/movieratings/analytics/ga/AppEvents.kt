package com.fenchtose.movieratings.analytics.ga

import com.fenchtose.movieratings.analytics.events.FaEvent
import com.fenchtose.movieratings.model.entity.Sort

class AppEvents {
    companion object {

        fun search(source: String) = FaEvent("search").params("source", source)

        fun searchMore(page: Int, source: String) =
            FaEvent("search_more").params("source", source).params("page", page)

        fun clearSearch(source: String) = FaEvent("search_clear").params("source", source)
        fun openMovie(source: String) = FaEvent("open_movie").params("source", source)
        fun openSettings(source: String) = FaEvent("open_settings").params("source", source)
        fun reportBug(source: String) = FaEvent("open_bug_report").params("source", source)
        fun openPrivacyPolicy(source: String) =
            FaEvent("privacy_policy").params("source", source)

        fun rateApp(source: String) = FaEvent("rate_policy").params("source", source)
        fun shareApp(source: String) = FaEvent("share_app").params("source", source)
        fun openSupportApp(source: String) = FaEvent("open_support_app").params("source", source)
        fun like(source: String?, status: Boolean) =
            FaEvent(if (status) "like" else "unlike").params("source", source ?: "unknown")

        fun selectTrendingTab(tab: String) = FaEvent("select_trending").params("tab", tab)
        fun sort(order: Sort, source: String?) =
            FaEvent("sort").params("order", order).params("source", source ?: "unknown")

        fun openCollection(source: String?) =
            FaEvent("collection_open").params("source", source ?: "unknown")

        fun addToCollection(source: String?) =
            FaEvent("collection_add").params("source", source ?: "unknown")

        fun togglePlot(action: String, source: String?) =
            FaEvent("toggle_plot").params("action", action).params("source", source ?: "unknown")

        fun openImdb(source: String?) = FaEvent("imdb_open").params("source", source ?: "unknown")
        fun startPurchase(sku: String) = FaEvent("purchase_start").params("sku", sku)
        fun completePurchase(sku: String) = FaEvent("purchase_complete").params("sku", sku)

        fun ratingNotFound(server: String) = FaEvent("rating_not_found").params("server", server)
        fun showRating(source: String) = FaEvent("rating_shown").params("source", source)
        fun sendNotification(source: String, type: String) =
            FaEvent("notification_sent").params("source", source).params("type", type)
        fun notificationBlocked(type: String) = FaEvent("notification_blocked").params("type", type)
        fun openNotification(type: String) = FaEvent("notification_opened").params("type", type)
        fun openMovieFromRatings(source: String) = FaEvent("rating_open").params("source", source)

        fun showBanner(banner: String) = FaEvent("banner_show").params("banner", banner)
        fun dismissBanner(banner: String) = FaEvent("banner_dismiss").params("banner", banner)
        fun bannerTapped(banner: String) = FaEvent("banner_tapped").params("banner", banner)

        val ACTIVATE_FLUTTER = FaEvent("activate_flutter")

        val TAP_CREATE_COLLECTION = FaEvent("collection_new")
        val CREATE_COLLECTION = FaEvent("collection_create")
        val SHARE_COLLECTIONS = FaEvent("collection_share_multi")
        val SELECT_COLLECTION = FaEvent("collection_select")
        val DELETE_COLLECTION = FaEvent("collection_delete")

        val REMOVE_MOVIE = FaEvent("collection_remove")
        val SHARE_COLLECTION = FaEvent("collection_share")

        val SELECT_SEASON = FaEvent("season_select")
        val OPEN_EPISODE = FaEvent("episode_open")
        val SELECT_EPISODE = FaEvent("episode_select")

        val DISMISS_RATING = FaEvent("rating_dismiss")
        val SPEAK_RATING = FaEvent("rating_tts")
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
        const val DEBUGGING = "debugging"
    }
}

class AppScreens {
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