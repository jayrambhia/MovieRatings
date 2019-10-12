package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.analytics.events.GaEvent
import com.fenchtose.movieratings.analytics.events.ScreenView
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*

class FirebaseEventDispatcher(private val fa: FirebaseAnalytics) : EventDispatcher {
    override fun sendEvent(event: Event) {
        when (event) {
            is ScreenView -> fa.setCurrentScreen(event.activity, event.name, event.classname)
            is GaEvent -> event.eventToName()?.let { fa.logEvent(it, null) }
        }
    }

    private fun GaEvent.eventToName(): String? {
        if (isInvalid(action) || isInvalid(category) || isInvalid(label)) {
            if (BuildConfig.DEBUG) {
                throw InvalidPropertiesFormatException("invalid properties for GA events: $this")
            }
            return null
        }

        return "${category}_${action}_${label}"
    }

    private fun isInvalid(content: String?): Boolean {
        return content == null || content.isBlank() || content.contains("%s") || content.contains("%d")
    }
}