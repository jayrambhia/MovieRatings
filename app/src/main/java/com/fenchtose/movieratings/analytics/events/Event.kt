package com.fenchtose.movieratings.analytics.events

import android.os.Bundle
import com.fenchtose.movieratings.MovieRatingsApplication

interface Event {
    fun track() {
        MovieRatingsApplication.analyticsDispatcher.sendEvent(this)
    }
}

data class GaEvent(val category: String, val action: String, val label: String,
              val nonInteractive: Boolean = false): Event {

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

    fun toBundle(): Bundle {
        return Bundle().apply {
            putString("category", category)
            putString("action", action)
            putString("label", label)
        }
    }
}

fun Bundle.toGaEvent(): GaEvent? {
    if (containsKey("action") && containsKey("label") && containsKey("category")) {
        val action = getString("action", "")
        val category = getString("category", "")
        val label = getString("label", "")
        if (!action.isEmpty() && !category.isEmpty() && !label.isEmpty()) {
            return GaEvent(category, action, label)
        }
    }

    return null
}

class ScreenView(val name: String): Event