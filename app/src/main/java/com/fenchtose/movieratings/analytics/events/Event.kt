package com.fenchtose.movieratings.analytics.events

import com.google.gson.JsonObject

class Event(val name: String) {

    val data: JsonObject = JsonObject()

    fun putAttribute(key: String, value: String): Event {
        data.addProperty(key, value)
        return this
    }

    fun putAttriubte(key: String, value: Number): Event {
        data.addProperty(key, value)
        return this
    }

}