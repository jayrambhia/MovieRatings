package com.fenchtose.movieratings.model.offline.export

import io.reactivex.Observable

interface DataExporter<T> {

    fun observe(): Observable<Progress<T>>
    fun export(output: T, config: Config)
    fun release()
    fun exportCollection(output: T, collectionId: Long)

    sealed class Progress<T> {
        class Started<T>: Progress<T>()
        class Error<T>: Progress<T>()
        class Success<T>(val output: T): Progress<T>() {
            override fun toString(): String {
                return "success: output: $output"
            }
        }
    }

    data class Config(val favs: Boolean, val collections: Boolean, val recentlyBrowsed: Boolean)
}