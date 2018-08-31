package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.analytics.events.GaEvent
import com.fenchtose.movieratings.analytics.events.ScreenView
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import java.util.*

class GaEventDispatcher(private val tracker: Tracker): EventDispatcher {

    override fun sendEvent(event: Event) {
        if (event is GaEvent) {
            if (isInvalid(event.action) || isInvalid(event.category) || isInvalid(event.label)) {
                if (BuildConfig.DEBUG) {
                    throw InvalidPropertiesFormatException("invalid properties for GA events")
                }
            } else {
                tracker.send(HitBuilders.EventBuilder()
                        .setAction(event.action)
                        .setCategory(event.category)
                        .setLabel(event.label)
                        .build())
            }
        } else if (event is ScreenView) {
            tracker.setScreenName(event.name)
            tracker.send(HitBuilders.ScreenViewBuilder().build())
        }
    }

    private fun isInvalid(content: String?): Boolean {
        return content == null || content.isBlank() || content.contains("%s") || content.contains("%d")
    }
}