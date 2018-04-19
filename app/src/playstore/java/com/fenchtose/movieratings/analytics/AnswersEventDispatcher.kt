package com.fenchtose.movieratings.analytics

//import com.crashlytics.android.answers.Answers
//import com.crashlytics.android.answers.CustomEvent
import com.fenchtose.movieratings.analytics.events.Event

class AnswersEventDispatcher: EventDispatcher {

    override fun sendEvent(event: Event) {
//        Answers.getInstance().logCustom(convert(event))
    }

    /*private fun convert(event: Event): CustomEvent {
        val customEvent = CustomEvent(event.name)
        val it = event.data.entrySet().iterator()
        it.forEach {
            if (it.value.asJsonPrimitive.isNumber) {
                customEvent.putCustomAttribute(it.key, it.value.asNumber)
            } else if (it.value.asJsonPrimitive.isString) {
                customEvent.putCustomAttribute(it.key, it.value.asString)
            }
        }

        return customEvent
    }*/
}