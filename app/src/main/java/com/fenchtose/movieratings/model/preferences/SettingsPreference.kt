package com.fenchtose.movieratings.model.preferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SettingsPreference(context: Context) {

    private val PREF_NAME = "settings_pref"

    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    companion object {
        val NETFLIX = "netflix"
    }

    fun isAppEnabled(app: String): Boolean {
        return preferences.getBoolean(app, true)
    }

    fun setAppEnabled(app: String, status: Boolean) {
        preferences.edit().putBoolean(app, status).apply()
    }

}
