package com.fenchtose.movieratings.analytics.events

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.util.emptyAsNull

interface Event {
    fun track() {
        MovieRatingsApplication.analyticsDispatcher.sendEvent(this)
    }
}

class FaEvent(val name: String, val params: HashMap<String, Any> = hashMapOf()) : Event {
    fun params(key: String, value: Any) : FaEvent {
        params[key] = value
        return this
    }

    fun withBundle(bundle: Bundle): FaEvent {
        bundle.keySet().forEach { key ->
            bundle[key]?.let {
                params[key] = it
            }
        }
        return this
    }

    fun bundle(): Bundle {
        return Bundle().apply {
            params.entries.forEach { entry ->
                val value = entry.value
                when (value) {
                    is Int -> putInt(entry.key, value)
                    is String -> putString(entry.key, value)
                    is Float -> putFloat(entry.key, value)
                    is Long -> putLong(entry.key, value)
                    is Boolean -> putBoolean(entry.key, value)
                    else -> {
                        if (BuildConfig.DEBUG) {
                            Log.e(
                                "FaEvent",
                                "params not supported: ${entry.key} => ${entry.value}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun parcel(): Bundle {
        return Bundle().apply {
            putString("name", name)
            putBundle("data", bundle())
        }
    }
}

fun Bundle.toFaEvent(): FaEvent? {
    val name = getString("name", "").emptyAsNull() ?: return null
    return FaEvent(name).withBundle(getBundle("data") ?: Bundle())
}

class ScreenView(val activity: Activity, val name: String, val classname: String): Event