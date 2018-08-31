package com.fenchtose.movieratings.analytics.events

import com.fenchtose.movieratings.MovieRatingsApplication
import com.google.gson.JsonObject

open class Event(val name: String) {

    val data: JsonObject = JsonObject()

    fun putAttribute(key: String, value: String): Event {
        data.addProperty(key, value)
        return this
    }

    fun putAttriubte(key: String, value: Number): Event {
        data.addProperty(key, value)
        return this
    }

    fun track() {
        MovieRatingsApplication.analyticsDispatcher.sendEvent(this)
    }

}

class GaEvent(val category: String, val action: String, val label: String): Event(label) {

    fun withLabel(label: String): GaEvent {
        return GaEvent(category, action, label)
    }

    fun withLabelArg(arg: String): GaEvent {
        return GaEvent(category, action, String.format(label, arg))
    }

    fun withLabelArg(arg: Int): GaEvent {
        return GaEvent(category, action, String.format(label, arg))
    }

    fun withCategory(category: String?): GaEvent {
        return GaEvent(category?: "", action, label)
    }
}