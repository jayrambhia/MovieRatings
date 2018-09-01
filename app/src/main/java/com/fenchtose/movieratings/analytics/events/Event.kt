package com.fenchtose.movieratings.analytics.events

import com.fenchtose.movieratings.MovieRatingsApplication

interface Event {
    fun track() {
        MovieRatingsApplication.analyticsDispatcher.sendEvent(this)
    }
}

class GaEvent(val category: String, val action: String, val label: String): Event {

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

    fun withAction(action: String): GaEvent {
        return GaEvent(category, action, label)
    }
}

class ScreenView(val name: String): Event