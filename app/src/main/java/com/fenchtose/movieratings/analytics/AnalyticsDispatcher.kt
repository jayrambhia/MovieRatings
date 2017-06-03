package com.fenchtose.movieratings.analytics

import com.fenchtose.movieratings.analytics.events.Event

class AnalyticsDispatcher: EventDispatcher {

    private val map = HashMap<String, EventDispatcher>()
    private val dispatchers = ArrayList<EventDispatcher>()

    fun attachDispatcher(key: String, dispatcher: EventDispatcher) {
        removeDispatcher(key)
        map[key] = dispatcher
        dispatchers.add(dispatcher)
    }

    fun removeDispatcher(key: String) {
        val existingDispatcher = map[key]
        existingDispatcher?.let {
            remove(existingDispatcher)
        }
    }

    private fun remove(dispatcher: EventDispatcher) {
        dispatchers.remove(dispatcher)
    }

    override fun sendEvent(event: Event) {
        dispatchers.forEach {
            it.sendEvent(event)
        }
    }
}