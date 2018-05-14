package com.fenchtose.movieratings.model.offline.export

import io.reactivex.Observable

interface DataExporter<T> {

    fun observe(): Observable<Progress<T>>
    fun export(config: Config)
    fun release()

    sealed class Progress<T> {
        class Started<T>: Progress<T>()
        class Error<T>: Progress<T>()
        class Success<T>(val data: T): Progress<T>() {
            override fun toString(): String {
                return "success: filename: $data"
            }
        }
    }

    data class Config(val favs: Boolean, val collections: Boolean, val recentlyBrowsed: Boolean)
}