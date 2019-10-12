package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.analytics.events.Event
import com.fenchtose.movieratings.analytics.events.FaEvent
import com.fenchtose.movieratings.analytics.events.ScreenView
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseEventDispatcher(private val fa: FirebaseAnalytics) : EventDispatcher {
    override fun sendEvent(event: Event) {
        when (event) {
            is ScreenView -> fa.setCurrentScreen(event.activity, event.name, event.classname)
            is FaEvent -> fa.logEvent(event.name, event.bundle())
        }
    }
}