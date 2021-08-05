package com.fenchtose.movieratings.model.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.fenchtose.movieratings.model.entity.Sort

class SettingsPreferences(context: Context): UserPreferences {

    private val PREF_NAME = "settings_pref"
    private val DURATION_KEY = "toast_duration"
    private val LIKES_SORT_KEY = "likes_sort"
    private val COLLECTION_SORT_KEY = "collection_sort_"
    private val RATING_BUBBLE_COLOR_KEY = "rating_bubble_color"
    private val RATING_BUBBLE_Y_POS_KEY = "rating_bubble_y_pos"
    private val RATING_BUBBLE_X_POS_KEY = "rating_bubble_x_is_left"

    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    override fun isAppEnabled(app: String): Boolean {
        return preferences.getBoolean(app, true)
    }

    override fun isSettingEnabled(app: String): Boolean {
        return preferences.getBoolean(app, false)
    }

    override fun setEnabled(app: String, status: Boolean) {
        preferences.edit().putBoolean(app, status).apply()
    }

    override fun getRatingDisplayDuration(): Int {
        return preferences.getInt(DURATION_KEY, 5000)
    }

    override fun setRatingDisplayDuration(durationInMS: Int) {
        preferences.edit().putInt(DURATION_KEY, durationInMS).apply()
    }

    override fun setLatestLikeSort(type: Sort) {
        preferences.edit().putString(LIKES_SORT_KEY, type.name).apply()
    }

    override fun getLatestLikeSort(): Sort {
        return Sort.valueOf(preferences.getString(LIKES_SORT_KEY, Sort.ALPHABETICAL.name)!!)
    }

    override fun setLatestCollectionSort(collectionId: Long?, type: Sort) {
        collectionId?.let {
            preferences.edit().putString(COLLECTION_SORT_KEY + it.toString(), type.name).apply()
        }
    }

    override fun getLatestCollectionSort(collectionId: Long?): Sort {
        collectionId?.let {
            return Sort.valueOf(preferences.getString(COLLECTION_SORT_KEY + it.toString(), Sort.ALPHABETICAL.name)!!)
        }

        return Sort.ALPHABETICAL
    }

    override fun getBubbleColor(fallback: Int): Int {
        return preferences.getInt(RATING_BUBBLE_COLOR_KEY, fallback)
    }

    override fun setBubbleColor(color: Int) {
        preferences.edit().putInt(RATING_BUBBLE_COLOR_KEY, color).apply()
    }

    override fun getBubblePosition(fallbackY: Int): Pair<Int, Boolean> {
        return Pair(preferences.getInt(RATING_BUBBLE_Y_POS_KEY, fallbackY),
                preferences.getBoolean(RATING_BUBBLE_X_POS_KEY, false))
    }

    override fun setBubblePosition(y: Int, left: Boolean) {
        preferences.edit().putInt(RATING_BUBBLE_Y_POS_KEY, y).putBoolean(RATING_BUBBLE_X_POS_KEY, left).apply()
    }
}
