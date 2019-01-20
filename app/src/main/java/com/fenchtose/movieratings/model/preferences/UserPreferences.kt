package com.fenchtose.movieratings.model.preferences

import android.support.annotation.ColorInt
import com.fenchtose.movieratings.model.entity.Sort

interface UserPreferences {

    companion object {
        const val SAVE_HISTORY = "save_history"
        const val USE_TTS = "use_tts"
        const val TTS_AVAILABLE = "tts_available"
        const val SHOW_ACTIVATE_FLUTTER = "show_activate_flutter"
        const val USE_YEAR = "use_year"
        const val SHOW_RECENT_RATING = "recent_rating"
        const val CHECK_ANIME = "check_anime"
        const val ONBOARDING_SHOWN = "onboarding_shown"
        const val LOCALE_INFO_SHOWN = "locale_info_shown"
        const val SHOW_SUPPORT_APP_PROMPT = "show_support_app"
        const val SHOW_RATE_APP_PROMPT = "show_rate_app"
        const val USE_FLUTTER_API = "use_flutter_api"
        const val OPEN_MOVIE_IN_APP = "open_movie_in_app"
        const val RATING_DETAILS = "rating_details"
    }

    /**
     * returns if particular app is enabled or not. Default is true.
     */
    fun isAppEnabled(app: String): Boolean

    /**
     * returns if particular setting is enabled or not. Default is false.
     */
    fun isSettingEnabled(app: String): Boolean

    fun setEnabled(app: String, status: Boolean)

    fun getRatingDisplayDuration():Int
    fun setRatingDisplayDuration(durationInMS: Int)

    fun setLatestLikeSort(type: Sort)
    fun getLatestLikeSort(): Sort

    fun setLatestCollectionSort(collectionId: Long?, type: Sort)
    fun getLatestCollectionSort(collectionId: Long?): Sort

    fun getBubbleColor(@ColorInt fallback: Int): Int
    fun setBubbleColor(@ColorInt color: Int)

    fun getBubblePosition(fallbackY: Int): Pair<Int, Boolean>
    fun setBubblePosition(y: Int, left: Boolean)
}