package com.fenchtose.movieratings.model.offline.import

import android.net.Uri
import io.reactivex.Observable

interface DataImporter {

    fun observe(): Observable<Progress>
    fun import(uri: Uri, config: Config)
    fun release()
    fun report(uri: Uri): Observable<Report>

    sealed class Progress {
        class Started: Progress()
        class Error(val error: String): Progress()
        class Success: Progress()
    }

    data class Config(val favs: Boolean, val collections: Boolean, val recentlyBrowsed: Boolean)

    data class Report(val name: String?, val version: String?, val favs: Boolean,
                      val collections: Boolean, val recentlyBrowsed: Boolean, val movies: Boolean)
}