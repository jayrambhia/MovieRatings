package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.analytics.events.Event

interface EventDispatcher {
    fun sendEvent(event: Event)
}