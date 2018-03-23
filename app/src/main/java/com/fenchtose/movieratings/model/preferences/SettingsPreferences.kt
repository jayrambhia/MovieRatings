package com.fenchtose.movieratings.model.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.fenchtose.movieratings.model.Sort

class SettingsPreferences(context: Context): UserPreferences {

    private val PREF_NAME = "settings_pref"
    private val DURATION_KEY = "toast_duration"
    private val LIKES_SORT_KEY = "likes_sort"
    private val COLLECTION_SORT_KEY = "collection_sort_"

    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    override fun isAppEnabled(app: String): Boolean {
        return preferences.getBoolean(app, true)
    }

    override fun setAppEnabled(app: String, status: Boolean) {
        preferences.edit().putBoolean(app, status).apply()
    }

    override fun getToastDuration(): Int {
        return preferences.getInt(DURATION_KEY, 2000)
    }

    override fun setToastDuration(durationInMS: Int) {
        preferences.edit().putInt(DURATION_KEY, durationInMS).apply()
    }

    override fun setLatestLikeSort(type: Sort) {
        preferences.edit().putString(LIKES_SORT_KEY, type.name).apply()
    }

    override fun getLatestLikeSort(): Sort {
        return Sort.valueOf(preferences.getString(LIKES_SORT_KEY, Sort.ALPHABETICAL.name))
    }

    override fun setLatestCollectionSort(collectionId: Long?, type: Sort) {
        collectionId?.let {
            preferences.edit().putString(COLLECTION_SORT_KEY + it.toString(), type.name).apply()
        }
    }

    override fun getLatestCollectionSort(collectionId: Long?): Sort {
        collectionId?.let {
            return Sort.valueOf(preferences.getString(COLLECTION_SORT_KEY + it.toString(), Sort.ALPHABETICAL.name))
        }

        return Sort.ALPHABETICAL
    }
}
