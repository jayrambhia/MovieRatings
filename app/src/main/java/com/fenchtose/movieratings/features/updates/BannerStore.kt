package com.fenchtose.movieratings.features.updates

import android.content.Context
import android.content.SharedPreferences
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_BATTERY_OPTIMIZATION
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_MAL
import com.fenchtose.movieratings.features.updates.BannerStore.Companion.BANNER_REDBOX
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.PackageUtils
import com.fenchtose.movieratings.util.checkBatteryOptimized
import io.reactivex.Observable

interface BannerStore {

    companion object {
        const val BANNER_MAL = "mal"
        const val BANNER_REDBOX = "redbox"
        const val BANNER_BATTERY_OPTIMIZATION = "battery_optimization"
    }

    fun load(version: Int): Observable<List<UpdateItem>>
    fun dismiss(banner: UpdateItem)
}

private fun allBanners(): List<UpdateItem> {
    return listOf(
        UpdateItem(
            id = BANNER_MAL,
            maxVersion = 308,
            icon = R.drawable.ic_new_releases_black_24dp,
            description = "Based on the popular demand we have added 'My Anime List (MAL)' as one of the sources to get ratings of anime. It's available now.",
            positiveCtaText = "Configure settings"
        ),
        UpdateItem(
            id = BANNER_REDBOX,
            maxVersion = 309,
            icon = R.drawable.ic_new_releases_black_24dp,
            description = "As per user request, we have added support for the Redbox app.",
            positiveCtaText = "Configure settings",
            filter = {
                PackageUtils.hasInstalled(it, Constants.PACKAGE_REDBOX)
            }
        ),
        UpdateItem(
            id = BANNER_BATTERY_OPTIMIZATION,
            maxVersion = -1,
            icon = R.drawable.ic_battery_charging_90_black_24dp,
            description = "Add this app to battery optimization whitelist in order to have an uninterrupted experience.",
            positiveCtaText = "Know more",
            filter = {
                checkBatteryOptimized(it)
            }
        )
    )
}

class PreferencesBannerStore(private val context: Context) : BannerStore {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("updates_banners", Context.MODE_PRIVATE)
    private val KEY_PREFIX = "banner_"
    private val KEY_LAST_INTERACTED = "banner_last_interacted"
    private val THRESHOLD = 60 * 3600 // 1 hour

    override fun load(version: Int): Observable<List<UpdateItem>> {
        return Observable.defer {
            // Do not show back to back banners. Keep threshold of 1 hour.
            val lastShown = preferences.getLong(KEY_LAST_INTERACTED, 0)
            Observable
                .just(
                    (if (System.currentTimeMillis() / 1000 - lastShown > THRESHOLD) allBanners() else listOf())
                        .filter { it.maxVersion == -1 || it.maxVersion >= version }
                        .filter { !isDismissed(it.id) }
                        .filter { it.filter(context) }
                )
        }
    }

    override fun dismiss(banner: UpdateItem) {
        preferences.edit().putLong(KEY_LAST_INTERACTED, System.currentTimeMillis() / 1000).apply()
        preferences.edit().putBoolean(KEY_PREFIX + banner.id, true).apply()
    }

    private fun isDismissed(id: String): Boolean {
        return preferences.getBoolean(KEY_PREFIX + id, false)
    }
}