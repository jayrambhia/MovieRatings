package com.fenchtose.movieratings.base.router

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class ResultBus {
    companion object {
        private val map: HashMap<String, BehaviorSubject<Result<Any>>> = HashMap()

        fun subscribe(result: String) : Observable<Result<Any>> {
            val subject = map[result]
            subject?.let {
                return subject
            }

            return add(result)
        }

        fun setResult(result: String, value: Result<Any>) {
            val subject = map[result]
            subject?.let {
                subject.onNext(value)
            }

            add(result).onNext(value)
        }

        fun clearResult(result: String) {
            val subject = map.remove(result)
            subject?.let {
                subject.onComplete()
            }
        }

        fun release() {
            map.forEach { _, subject -> subject.onComplete() }
            map.clear()
        }

        private fun add(result: String) : BehaviorSubject<Result<Any>> {
            val subject = BehaviorSubject.create<Result<Any>>()
            map.put(result, subject)
            return subject
        }
    }

    interface Result<T> {

        fun getResult(): T

        companion object Result {
            fun create(value: Any): ResultBus.Result<Any> {
                return object : ResultBus.Result<Any> {
                    override fun getResult(): Any {
                        return value
                    }
                }
            }
        }
    }
}