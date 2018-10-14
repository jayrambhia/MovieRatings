package com.fenchtose.movieratings.features.updates

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable

interface BannerStore {
    fun load(version: Int): Observable<List<UpdateItem>>
    fun dismiss(banner: UpdateItem)
}

private fun allBanners(): List<UpdateItem> {
    return listOf(
        UpdateItem(
                "mal",
                306,
                "My Anime List",
                "One of you asked us to add My Anime List as one of the sources to get movie ratings. It's available now. You can configure in the settings"
                )
    )
}

class PreferencesBannerStore(context: Context): BannerStore {

    private val preferences: SharedPreferences = context.getSharedPreferences("updates_banners", Context.MODE_PRIVATE)
    private val KEY_PREFIX = "banner_"

    override fun load(version: Int): Observable<List<UpdateItem>> {
        return Observable.defer {
            Observable.just(
                    allBanners()
                            .filter { !isDismissed(it.id) }
            )
        }
    }

    override fun dismiss(banner: UpdateItem) {
        preferences.edit().putBoolean(KEY_PREFIX, true).apply()
    }

    private fun isDismissed(id: String): Boolean {
        return preferences.getBoolean(KEY_PREFIX + id, false)
    }
}