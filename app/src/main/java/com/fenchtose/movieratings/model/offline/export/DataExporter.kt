package com.fenchtose.movieratings.model.offline.export

import io.reactivex.Observable

interface DataExporter<T> {

    fun observe(): Observable<Progress<T>>
    fun export(key: String, output: T, config: Config)
    fun release()
    fun exportCollection(key: String, output: T, collectionId: Long)

    sealed class Progress<T>(val key: String) {
        class Started<T>(key: String): Progress<T>(key)
        class Error<T>(key: String): Progress<T>(key)
        class Success<T>(key: String, val output: T): Progress<T>(key) {
            override fun toString(): String {
                return "success: output: $output"
            }
        }
    }

    data class Config(val favs: Boolean, val collections: Boolean, val recentlyBrowsed: Boolean)
}