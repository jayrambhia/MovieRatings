package com.fenchtose.movieratings.model.offline.export

import io.reactivex.Observable

interface DataExporter<T> {

    fun observe(): Observable<Progress<T>>
    fun export(config: Config)
    fun release()

    sealed class Progress<T> {
        class Started<T>: Progress<T>()
        class Error<T>: Progress<T>()
        class Success<T>(val t: T): Progress<T>() {
            override fun toString(): String {
                return "success: filename: $t"
            }
        }
    }

    data class Config(val includeHistory: Boolean)
}