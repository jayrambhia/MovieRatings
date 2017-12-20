package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.analytics.events.Event

class FakeDispatcher: EventDispatcher {

    override fun sendEvent(event: Event) {
        // ignore
    }
}