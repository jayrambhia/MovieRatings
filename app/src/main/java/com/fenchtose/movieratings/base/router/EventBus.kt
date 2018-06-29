package com.fenchtose.movieratings.base.router

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class EventBus {
    companion object {

        val publisher: PublishSubject<Any> = PublishSubject.create()

        inline fun <reified T> subscribe(): Observable<T> {
            return publisher.filter {
                it is T
            }.map {
                it as T
            }
        }

        fun send(event: Any) {
            publisher.onNext(event)
        }

    }
}